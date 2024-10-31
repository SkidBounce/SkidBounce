/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.hypixel

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.blink.IBlink
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author SkidderMC/FDPClient
 */
object HypixelBlink : NoFallMode("HypixelBlink"), IBlink {
    private var wasOnGround = false

    override fun onEnable() {
        blinkingClient = false
    }

    override fun onUpdate() {
        if (mc.thePlayer.onGround) wasOnGround = true
        else if (wasOnGround) {
            wasOnGround = false
            if (mc.thePlayer.motionY < 0) {
                blinkingClient = true
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (!blinkingClient)
            return

        if (event.packet is C03PacketPlayer)
            event.packet.onGround = true

        if (mc.thePlayer.onGround) {
            blinkingClient = false
        }
    }
}
