/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

/**
 * @author SkidderMC/FDPClient
 */
object AAC52 : NoWebMode("AAC5-2") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb) return
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42
            strafe(0.37)
            return
        }
        strafe(0.3)
    }
}
