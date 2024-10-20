/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.update

/**
 * @author CCBlueX/LiquidBounce
 */
object Legit : SpeedMode("Legit") {
    override fun onUpdate() {
        if (mc.thePlayer != null && isMoving) {
            mc.gameSettings.keyBindJump.pressed = true
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindJump.update()
    }
}
