/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.onPlayerRightClick
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

/**
 * @author CCBlueX/LiquidBounce
 */
object MineplexGround : SpeedMode("MineplexGround", true) {
    private val speed by FloatValue("Speed", 0.5f, 0.1f..1f)

    private var spoofSlot = false
    private var speed_ = 0f

    override fun onMotion(event: MotionEvent) {
        if (!isMoving || !mc.thePlayer.onGround || mc.thePlayer.heldItem == null || mc.thePlayer.isUsingItem) return
        spoofSlot = false
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null) continue

            serverSlot = i - 36
            spoofSlot = true
            break
        }
    }

    override fun onUpdate() {
        if (!isMoving || !mc.thePlayer.onGround || mc.thePlayer.isUsingItem) {
            speed_ = 0f
            return
        }
        if (!spoofSlot && mc.thePlayer.heldItem != null) {
            displayClientMessage("§cYou need one empty slot.")
            return
        }

        val blockPos = BlockPos(mc.thePlayer).down()
        val vec = Vec3(blockPos).addVector(0.4, 0.4, 0.4) + Vec3(EnumFacing.UP.directionVec)

        mc.thePlayer.onPlayerRightClick(
            blockPos,
            EnumFacing.UP,
            Vec3(vec.xCoord * 0.4f, vec.yCoord * 0.4f, vec.zCoord * 0.4f)
        )

        speed_ = (speed_ + speed / 8).coerceAtMost(speed)

        strafe(speed_)

        if (!spoofSlot) serverSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        speed_ = 0f
        serverSlot = mc.thePlayer.inventory.currentItem
    }
}
