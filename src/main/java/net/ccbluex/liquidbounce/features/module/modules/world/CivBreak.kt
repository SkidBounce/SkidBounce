/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.events.ClickBlockEvent
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.Render3DEvent
import net.ccbluex.liquidbounce.event.events.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory.WORLD
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.faceBlock
import net.ccbluex.liquidbounce.utils.RotationUtils.limitAngleChange
import net.ccbluex.liquidbounce.utils.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.extensions.rotation
import net.ccbluex.liquidbounce.utils.extensions.swing
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.SwingValue
import net.minecraft.init.Blocks.air
import net.minecraft.init.Blocks.bedrock
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.*
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color

object CivBreak : Module("CivBreak", WORLD) {

    private val range by FloatValue("Range", 5F, 1F..6F)
    private val swing by SwingValue()

    private val rotations by BooleanValue("Rotations", true)
    private val strafe by ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Off") { rotations }
    private val smootherMode by ListValue("SmootherMode", arrayOf("Linear", "Relative"), "Relative") { rotations }

    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 120f, 0f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minTurnSpeed)

        override fun isSupported() = rotations
    }
    private val maxTurnSpeed by maxTurnSpeedValue

    private val minTurnSpeed by object : FloatValue("MinTurnSpeed", 80f, 0f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxTurnSpeed)

        override fun isSupported() = !maxTurnSpeedValue.isMinimal && rotations
    }

    private val angleThresholdUntilReset by FloatValue("AngleThresholdUntilReset", 5f, 0.1f..180f) { rotations }
    private val grim by BooleanValue("Grim", false)

    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null

    @EventTarget
    fun onBlockClick(event: ClickBlockEvent) {
        if (event.clickedBlock?.let { getBlock(it) } == bedrock) {
            return
        }

        blockPos = event.clickedBlock ?: return
        enumFacing = event.enumFacing ?: return

        // Break
        sendPacket(C07PacketPlayerDigging(START_DESTROY_BLOCK, blockPos, enumFacing))
        if (grim) sendPacket(C07PacketPlayerDigging(ABORT_DESTROY_BLOCK, blockPos!!.down(Int.MIN_VALUE), enumFacing))
        sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, enumFacing))
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        if (event.eventState != EventState.POST) {
            return
        }

        val pos = blockPos ?: return
        val isAirBlock = getBlock(pos) == air

        if (isAirBlock || getCenterDistance(pos) > range) {
            blockPos = null
            return
        }

        if (rotations) {
            val spot = faceBlock(pos) ?: return

            val limitedRotation = limitAngleChange(
                currentRotation ?: player.rotation,
                spot.rotation,
                nextFloat(minTurnSpeed, maxTurnSpeed),
                smootherMode
            )

            setTargetRotation(
                limitedRotation,
                strafe = strafe != "Off",
                strict = strafe == "Strict",
                resetSpeed = minTurnSpeed to maxTurnSpeed,
                angleThresholdForReset = angleThresholdUntilReset,
                smootherMode = this.smootherMode
            )
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        blockPos ?: return
        enumFacing ?: return

        mc.thePlayer.swing(swing)

        // Break
        if (!grim) sendPacket(C07PacketPlayerDigging(START_DESTROY_BLOCK, blockPos, enumFacing))
        sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, enumFacing))

        mc.playerController.clickBlock(blockPos, enumFacing)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        drawBlockBox(blockPos ?: return, Color.RED, true)
    }
}
