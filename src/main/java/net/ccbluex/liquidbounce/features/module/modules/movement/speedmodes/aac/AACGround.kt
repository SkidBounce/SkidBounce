/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

/**
 * @author CCBlueX/LiquidBounce
 */
object AACGround : SpeedMode("AACGround", true) {
    private val timer by FloatValue("Timer", 3f, 1.1f..10f)

    override fun onUpdate() {
        if (!isMoving)
            return

        mc.timer.timerSpeed = timer
        sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
    }
}
