/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.timing.TickTimer

/**
 * @author CCBlueX/LiquidBounce
 */
object Frame : SpeedMode("Frame") {
    private var motionTicks = 0
    private var move = false
    private val tickTimer = TickTimer()
    override fun onMotion(event: MotionEvent) {
        if (isMoving) {
            val speed = 4.25
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jmp()
                if (motionTicks == 1) {
                    tickTimer.reset()
                    if (move) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                        move = false
                    }
                    motionTicks = 0
                } else motionTicks = 1
            } else if (!move && motionTicks == 1 && tickTimer.hasTimePassed(5)) {
                mc.thePlayer.motionX *= speed
                mc.thePlayer.motionZ *= speed
                move = true
            }
            if (!mc.thePlayer.onGround) strafe()
            tickTimer.update()
        }
    }
}
