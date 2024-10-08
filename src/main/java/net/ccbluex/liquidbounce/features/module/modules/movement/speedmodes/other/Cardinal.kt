/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.IceSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.cardinalAboveWaterMultiplier
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.cardinalJumpWhenIceSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.cardinalSlimeMultiplier
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.cardinalStrafeHeight
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.cardinalStrafeStrength
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.isOnGround
import net.ccbluex.liquidbounce.utils.MovementUtils.onIce
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.minecraft.init.Blocks.slime_block
import net.minecraft.init.Blocks.water

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Cardinal : SpeedMode("Cardinal") {
    override fun onMotion(event: MotionEvent) {
        if (!isMoving) {
            mc.thePlayer.stopXZ()
            return
        }

        mc.thePlayer.jumpMovementFactor = 0.026f

        if (mc.thePlayer.onGround && !(onIce && IceSpeed.state && cardinalJumpWhenIceSpeed)) {
            mc.thePlayer.jmp()
            when (mc.thePlayer.position.down().getBlock()) {
                water -> mc.thePlayer.motionY *= cardinalAboveWaterMultiplier
                slime_block -> mc.thePlayer.motionY *= cardinalSlimeMultiplier
            }
        }
        strafe(strength = if (isOnGround(cardinalStrafeHeight.toDouble())) 1f else cardinalStrafeStrength)
    }
}
