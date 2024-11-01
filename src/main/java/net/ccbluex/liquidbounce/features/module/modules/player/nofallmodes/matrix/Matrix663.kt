/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.resetSpeed
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

/**
 * @author Aspw-w/NightX-Client
 */
object Matrix663 : NoFallMode("Matrix6.6.3") {
    private var send = false
    private var timer = false

    override fun onEnable() {
        send = false
        timer = false
    }

    override fun onUpdate() {
        if (timer) {
            mc.timer.resetSpeed()
            timer = false
        }

        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f) {
            mc.thePlayer.fallDistance = 0f
            mc.timer.timerSpeed = 0.5f
            send = true
            timer = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && send) {
            send = false
            event.cancelEvent()
            sendPacket(
                C04PacketPlayerPosition(
                    event.packet.x,
                    event.packet.y,
                    event.packet.z,
                    true
                )
            )
            sendPacket(
                C04PacketPlayerPosition(
                    event.packet.x,
                    event.packet.y,
                    event.packet.z,
                    false
                )
            )
        }
    }
}
