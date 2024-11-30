/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Strafe : SpeedMode("Strafe") {
    private val inAir by FloatValue("InAir", 1f, 0f..1f)
    private val onGround by FloatValue("OnGround", 1f, 0f..1f)
    private val stopWhenNoInput by BooleanValue("StopWhenNoInput", true)

    override fun onStrafe() {
        mc.thePlayer ?: return

        Legit.onUpdate()

        strafe(strength = if (mc.thePlayer.onGround) onGround else inAir, stopWhenNoInput = stopWhenNoInput)
    }

    override fun onDisable() {
        Legit.onDisable()
    }
}
