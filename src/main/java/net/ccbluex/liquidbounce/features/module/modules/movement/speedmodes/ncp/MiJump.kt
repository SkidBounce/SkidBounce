/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

/**
 * @author CCBlueX/LiquidBounce
 */
object MiJump : SpeedMode("MiJump", true) {
    override fun onMotion(event: MotionEvent) {
        if (!isMoving) return
        if (mc.thePlayer.onGround && !mc.thePlayer.movementInput.jump) {
            mc.thePlayer.motionY += 0.1
            val multiplier = 1.8
            mc.thePlayer.motionX *= multiplier
            mc.thePlayer.motionZ *= multiplier
            val currentSpeed = speed
            val maxSpeed = 0.66
            if (currentSpeed > maxSpeed) {
                mc.thePlayer.motionX = mc.thePlayer.motionX / currentSpeed * maxSpeed
                mc.thePlayer.motionZ = mc.thePlayer.motionZ / currentSpeed * maxSpeed
            }
        }
        strafe()
    }
}
