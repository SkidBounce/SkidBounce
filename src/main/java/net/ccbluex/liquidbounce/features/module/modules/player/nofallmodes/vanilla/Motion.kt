/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vanilla

import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.value.DoubleValue

/**
 * @author SkidderMC/FDPClient
 */
object Motion : NoFallMode("Motion") {
    private val motion by DoubleValue("Motion", -0.01, -5.0..5.0)

    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 3)
            mc.thePlayer.motionY = motion
    }
}
