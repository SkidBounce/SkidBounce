/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla

import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.handleVanillaKickBypass
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode

/**
 * @author CCBlueX/LiquidBounce
 */
object SmoothVanilla : FlyMode("SmoothVanilla") {
    override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = true
        handleVanillaKickBypass()
    }
}
