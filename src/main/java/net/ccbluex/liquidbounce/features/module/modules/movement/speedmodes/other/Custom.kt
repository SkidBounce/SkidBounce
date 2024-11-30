/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue

/**
 * @author CCBlueX/LiquidBounce
 */
object Custom : SpeedMode("Custom") {
    private val speed by FloatValue("Speed", 1.6f, 0.2f..2f)
    private val y by FloatValue("Y", 0f, 0f..4f)
    private val timer by FloatValue("Timer", 1f, 0.1f..2f)
    private val strafe by BooleanValue("Strafe", true)
    private val resetXZ by BooleanValue("ResetXZ", false)
    private val resetY by BooleanValue("ResetY", false)

    override fun onMotion(event: MotionEvent) {
        if (isMoving) {
            mc.timer.timerSpeed = timer
            when {
                mc.thePlayer.onGround -> {
                    strafe(speed)
                    mc.thePlayer.motionY = y.toDouble()
                }

                strafe -> strafe(speed)
                else -> strafe()
            }
        } else {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
        }
    }
    override fun onEnable() {
        if (resetXZ) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
        }
        if (resetY) mc.thePlayer.motionY = 0.0
        super.onEnable()
    }
}
