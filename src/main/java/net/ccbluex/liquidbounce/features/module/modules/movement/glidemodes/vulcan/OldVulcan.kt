/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.glidemodes.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.glidemodes.GlideMode

/**
 * @author CCBlueX/LiquidBounce
 */
object OldVulcan : GlideMode("OldVulcan") {
    override fun onUpdate() {
        if (!mc.thePlayer.onGround && mc.thePlayer.fallDistance > 0) {
            mc.thePlayer.motionY =
                if (mc.thePlayer.ticksExisted % 2 == 0) -0.1 else -0.16
            mc.thePlayer.jumpMovementFactor = 0.0265f
        }
    }
}
