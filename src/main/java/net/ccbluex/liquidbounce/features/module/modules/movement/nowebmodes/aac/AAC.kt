/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode

/**
 * @author CCBlueX/LiquidBounce
 */
object AAC : NoWebMode("AAC") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb)
            return

        mc.thePlayer.jumpMovementFactor = 0.59f

        if (!mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY = 0.0
    }
}
