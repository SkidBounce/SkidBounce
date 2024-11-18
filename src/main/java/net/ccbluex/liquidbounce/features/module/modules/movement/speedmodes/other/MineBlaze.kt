/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.jmp

/**
 * @author CCBlueX/LiquidBounce
 */
object MineBlaze : SpeedMode("MineBlaze") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (player.onGround && isMoving) {
            player.jmp()
        }

        if (player.motionY > 0.003) {
            player.motionX *= 1.0015
            player.motionZ *= 1.0015
            mc.timer.timerSpeed = 1.06f
        }
    }
}
