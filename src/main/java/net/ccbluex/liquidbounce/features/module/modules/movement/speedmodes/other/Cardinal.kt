/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.IceSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.isOnGround
import net.ccbluex.liquidbounce.utils.MovementUtils.onIce
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.init.Blocks.slime_block
import net.minecraft.init.Blocks.water

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Cardinal : SpeedMode("Cardinal") {
    private val strafeHeight by FloatValue("StrafeHeight", 0.3f, 0.1f..1f)
    private val strafeStrength by FloatValue("StrafeStrength", 0.1f, 0f..0.5f)
    private val aboveWaterMultiplier by FloatValue("AboveWaterMultiplier", 0.87f, 0.4f..1f)
    private val slimeMultiplier by FloatValue("SlimeMultiplier", 0.7f, 0.4f..1f)
    private val jumpWhenIceSpeed by BooleanValue("JumpWhenIceSpeed", true)

    override fun onMotion(event: MotionEvent) {
        if (!isMoving) {
            mc.thePlayer.stopXZ()
            return
        }

        mc.thePlayer.jumpMovementFactor = 0.026f

        if (mc.thePlayer.onGround && !(onIce && IceSpeed.state && jumpWhenIceSpeed)) {
            mc.thePlayer.jmp()
            when (mc.thePlayer.position.down().getBlock()) {
                water -> mc.thePlayer.motionY *= aboveWaterMultiplier
                slime_block -> mc.thePlayer.motionY *= slimeMultiplier
            }
        }
        strafe(strength = if (isOnGround(strafeHeight.toDouble())) 1f else strafeStrength)
    }
}
