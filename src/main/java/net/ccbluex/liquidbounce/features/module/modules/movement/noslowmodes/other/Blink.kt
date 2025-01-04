package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.isUsingItem
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.blink.BlinkHandler
import net.ccbluex.liquidbounce.utils.blink.IBlink
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.ccbluex.liquidbounce.value.IntValue
import net.minecraft.item.ItemSword
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong

class Blink : NoSlowMode("Blink", swordOnly = true, allowNoMove = false), IBlink {
    private val reblinkTicks by IntValue("ReblinkTicks", 10, 1..20)
    private val blinkTimer = TickTimer()

    override fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return

            is C07PacketPlayerDigging, is C02PacketUseEntity, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                blinkTimer.update()
                if (blinkingClient && blinkTimer.hasTimePassed(reblinkTicks) && BlinkHandler.packets.isNotEmpty()) {
                    release(client = true, server = false)
                    blinkTimer.reset()
                    blinkingClient = false
                } else if (!blinkTimer.hasTimePassed(reblinkTicks)) {
                    blinkingClient = true
                }
                return
            }

            // Flush on kb
            is S12PacketEntityVelocity -> {
                if (mc.thePlayer.entityId == packet.entityID) {
                    release(client = true, server = false)
                    return
                }
            }

            // Flush on explosion
            is S27PacketExplosion -> {
                if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                    release(client = true, server = false)
                    return
                }
            }

            is C03PacketPlayer -> {
                if (isMoving) {
                    if (event.eventType != EventState.POST) {
                        if (!(mc.thePlayer.heldItem?.item is ItemSword && isUsingItem)) {
                            blinkingClient = true
                            release(client = true, server = false)
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
        blinkTimer.reset()
        release(client = true, server = false)
    }
}
