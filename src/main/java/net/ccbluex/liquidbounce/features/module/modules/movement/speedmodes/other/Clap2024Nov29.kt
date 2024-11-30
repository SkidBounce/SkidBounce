/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.extensions.update
import org.apache.commons.lang3.BooleanUtils.xor

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Clap2024Nov29 : SpeedMode("Clap2024Nov29") {
    override fun onUpdate() {
        if (mc.thePlayer == null || !isMoving) {
            mc.thePlayer.stopXZ()
            return
        }

        mc.thePlayer.jumpMovementFactor = if (xor(mc.thePlayer.moveForward != 0f, mc.thePlayer.moveStrafing != 0f)) 0.0265f else 0.0259f

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jmp(0.4)
        } else if (mc.thePlayer.motionY < 0) {
            mc.thePlayer.motionY -= 0.078
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindJump.update()
    }
}
