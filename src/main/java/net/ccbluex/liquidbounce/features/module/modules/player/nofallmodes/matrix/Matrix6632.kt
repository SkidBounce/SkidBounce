/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.value.BooleanValue
import net.minecraft.network.play.client.C03PacketPlayer

import kotlin.math.abs

/**
 * @author SkidderMC/FDPClient
 */
object Matrix6632 : NoFallMode("Matrix6.6.3-2") {
    private val safe by BooleanValue("Safe", false)

    private var send = false
    private var nearGround = false

    override fun onEnable() {
        send = false
        nearGround = false
    }

    override fun onUpdate() {
        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY <= 3 && (absCollYMinusYPos >= 3 || mc.thePlayer.fallDistance - mc.thePlayer.motionY <= 2)) {
            mc.timer.timerSpeed = 1f
            return
        }

        mc.thePlayer.fallDistance = 0f
        send = true

        if (safe) {
            mc.timer.timerSpeed = 0.3f
            mc.thePlayer.motionX *= 0.5
            mc.thePlayer.motionZ *= 0.5
        } else mc.timer.timerSpeed = 0.5f
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && send) {
            send = false
            if (absCollYMinusYPos > 2) {
                event.cancelEvent()
                sendPacket(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        event.packet.x,
                        event.packet.y,
                        event.packet.z,
                        true
                    )
                )
                sendPacket(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        event.packet.x,
                        event.packet.y,
                        event.packet.z,
                        false
                    )
                )
            }
        }
    }

    private val absCollYMinusYPos // null -> too far to calc or fall pos in void
        get() = abs((FallingPlayer(mc.thePlayer).findCollision(60)?.pos?.y ?: 0) - mc.thePlayer.posY)
}
