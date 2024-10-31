/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vulcan

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.vulcan2Motion
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.blink.IBlink
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Vulcan2 : NoFallMode("Vulcan2"), IBlink {
    private var modify = false
    override fun onEnable() {
        modify = false
        blinkingClient = false
    }

    override fun onUpdate() {
        if (mc.thePlayer.motionY <= 0.0 && mc.thePlayer.fallDistance <= 1f && blinkingClient)
            mc.thePlayer.motionY = -vulcan2Motion.toDouble()
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && modify && blinkingClient) {
            event.packet.onGround = true
            modify = false
        }
    }

    override fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.PRE) return

        if ((mc.thePlayer.onGround || MovementUtils.aboveVoid) && blinkingClient) {
            blinkingClient = false
            return
        }

        if (mc.thePlayer.fallDistance > 2.5 && blinkingClient) {
            modify = true
            mc.thePlayer.fallDistance = 0f
        }

        if (inAir(4.0, 1.0)) {
            return
        }

        blinkingClient = true
    }

    private fun inAir(height: Double, plus: Double): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < height) {
            val bb = AxisAlignedBB(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.posX,
                mc.thePlayer.posY - off,
                mc.thePlayer.posZ
            )
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isNotEmpty()) {
                return true
            }
            off += plus.toInt()
        }
        return false
    }
}
