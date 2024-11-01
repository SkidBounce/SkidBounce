/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vanilla

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author CCBlueX/LiquidBounce
 */
object SpoofGround : NoFallMode("SpoofGround") {
    private val always by BooleanValue("Always", true)
    private val minFallDistance by FloatValue("MinFallDistance", 0f, 0f..3f) { !always }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && (always || mc.thePlayer.fallDistance > minFallDistance))
            event.packet.onGround = true
    }
}
