/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.event.events.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.minecraft.init.Blocks
import net.minecraft.network.login.server.S00PacketDisconnect
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

object FlagCheck : Module("FlagCheck", Category.CLIENT, gameDetecting = true) {

    private val resetFlagCounterTicks by IntValue("ResetCounterTicks", 600, 100..1000)
    private val rubberbandCheck by BooleanValue("RubberbandCheck", false)
    private val rubberbandThreshold by FloatValue("RubberBandThreshold", 5.0f, 0.05f..10.0f) { rubberbandCheck }

    private var flagCount = 0
    private var lastYaw = 0F
    private var lastPitch = 0F

    private var blockPlacementAttempts = mutableMapOf<BlockPos, Long>()
    private var successfulPlacements = mutableSetOf<BlockPos>()

    private fun clearFlags() {
        flagCount = 0
        blockPlacementAttempts.clear()
        successfulPlacements.clear()
    }

    private var lagbackDetected = false
    private var forceRotateDetected = false

    private var lastMotionX = 0.0
    private var lastMotionY = 0.0
    private var lastMotionZ = 0.0

    private var lastPosX = 0.0
    private var lastPosY = 0.0
    private var lastPosZ = 0.0

    override fun onDisable() {
        clearFlags()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (player.ticksExisted <= 100)
            return

        if (player.isDead || (player.capabilities.isFlying && player.capabilities.disableDamage && !player.onGround))
            return

        if (packet is S08PacketPlayerPosLook) {
            val deltaYaw = calculateAngleDelta(packet.yaw, lastYaw)
            val deltaPitch = calculateAngleDelta(packet.pitch, lastPitch)

            if (deltaYaw > 90 || deltaPitch > 90) {
                forceRotateDetected = true
                flagCount++
                displayClientMessage("§dDetected §3Force-Rotate §e(${deltaYaw.roundToLong()}° | ${deltaPitch.roundToLong()}°) §b(§c${flagCount}x§b)")
            } else {
                forceRotateDetected = false
            }

            if (!forceRotateDetected) {
                lagbackDetected = true
                flagCount++
                displayClientMessage("§dDetected §3Lagback §b(§c${flagCount}x§b)")
            }

            if (mc.thePlayer.ticksExisted % 3 == 0) {
                lagbackDetected = false
            }

            lastYaw = mc.thePlayer.rotationYawHead
            lastPitch = mc.thePlayer.rotationPitch
        }

        if (packet is C08PacketPlayerBlockPlacement) {
            val blockPos = packet.position
            blockPlacementAttempts[blockPos] = System.currentTimeMillis()
            successfulPlacements.add(blockPos)
        }

        when (packet) {
            is S01PacketJoinGame, is S00PacketDisconnect -> {
                clearFlags()
            }
        }
    }

    private fun calculateAngleDelta(newAngle: Float, oldAngle: Float): Float {
        var delta = newAngle - oldAngle
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360
        return abs(delta)
    }

    /**
     * Rubberband, Invalid Health/Hunger & GhostBlock Checks
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val currentTime = System.currentTimeMillis()
        val iterator = blockPlacementAttempts.iterator()

        // GhostBlock Checks
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val blockPos = entry.key
            val timestamp = entry.value

            if (currentTime - timestamp > 500) {
                val block = world.getBlockState(blockPos).block
                if (block == Blocks.air && player.swingProgressInt > 3
                    && successfulPlacements != blockPos && !player.isBlocking
                    && !KillAura.renderBlocking && !KillAura.blockStatus) {

                    successfulPlacements.remove(blockPos)
                    flagCount++
                    displayClientMessage("§dDetected §3GhostBlock §b(§c${flagCount}x§b)")
                }
                iterator.remove()
            }
        }

        // Invalid Health/Hunger bar Checks (This is a known lagback by Intave AC)
        if (!player.isDead && (player.health <= 0.0f || player.foodStats.foodLevel <= 0)) {

            val invalidReason = mutableListOf<String>()
            if (player.health <= 0.0f) invalidReason.add("Health")
            if (player.foodStats.foodLevel <= 0) invalidReason.add("Hunger")

            if (invalidReason.isNotEmpty()) {
                flagCount++
                val reasonString = invalidReason.joinToString(" §8|§e ")
                displayClientMessage("§dDetected §3Invalid §e$reasonString §b(§c${flagCount}x§b)")
                invalidReason.clear()
            }
        }

        // Rubberband Checks
        if (!rubberbandCheck || player.ticksExisted <= 100)
            return

        if (player.isDead || (player.capabilities.isFlying && player.capabilities.disableDamage && !player.onGround))
            return

        val motionX = player.motionX
        val motionY = player.motionY
        val motionZ = player.motionZ

        val deltaX = player.posX - lastPosX
        val deltaY = player.posY - lastPosY
        val deltaZ = player.posZ - lastPosZ

        val distanceTraveled = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        val rubberbandReason = mutableListOf<String>()

        if (distanceTraveled > rubberbandThreshold) {
            rubberbandReason.add("Invalid Position")
        }

        if (abs(motionX) > rubberbandThreshold || abs(motionY) > rubberbandThreshold || abs(motionZ) > rubberbandThreshold) {
            if (!player.isCollided && !player.onGround) {
                rubberbandReason.add("Invalid Motion")
            }
        }

        if (rubberbandReason.isNotEmpty()) {
            flagCount++
            val reasonString = rubberbandReason.joinToString(" §8|§e ")
            displayClientMessage("§dDetected §3Rubberband §8(§e$reasonString§8) §b(§c${flagCount}x§b)")
            rubberbandReason.clear()
        }

        // Update last position and motion
        lastPosX = player.prevPosX
        lastPosY = player.prevPosY
        lastPosZ = player.prevPosZ

        lastMotionX = motionX
        lastMotionY = motionY
        lastMotionZ = motionZ

        // Automatically clear flags (Default: 10 minutes)
        if (player.ticksExisted % (resetFlagCounterTicks * 20) == 0) {
            clearFlags()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clearFlags()
    }
}
