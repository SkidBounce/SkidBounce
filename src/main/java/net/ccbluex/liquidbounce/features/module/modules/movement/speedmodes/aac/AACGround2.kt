/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.value.FloatValue

/**
 * @author CCBlueX/LiquidBounce
 */
object AACGround2 : SpeedMode("AACGround2", true) {
    private val timer by FloatValue("Timer", 3f, 1.1f..10f)

    override fun onUpdate() {
        if (!isMoving)
            return

        mc.timer.timerSpeed = timer
        strafe(0.02f)
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}
