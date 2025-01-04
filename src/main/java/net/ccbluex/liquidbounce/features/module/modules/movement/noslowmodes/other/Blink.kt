package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.isUsingItem
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.blink.IBlink
import net.minecraft.item.ItemSword
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong

class Blink : NoSlowMode("Blink", swordOnly = true, allowNoMove = false), IBlink {
    override fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return

            // Flush on doing action, getting action
            is S08PacketPlayerPosLook, is C07PacketPlayerDigging, is C02PacketUseEntity, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                if (blinkingClient) {
                    release(client = true, server = false)
                    blinkingClient = false
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
}
