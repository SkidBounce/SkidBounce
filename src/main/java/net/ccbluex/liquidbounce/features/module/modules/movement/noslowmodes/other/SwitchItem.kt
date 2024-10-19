/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.isUse
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C09PacketHeldItemChange

/**
 * @author CCBlueX/LiquidBounce
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
class SwitchItem : NoSlowMode("SwitchItem", handlePacketEventOnNoMove = true) {
    private val packets by IntValue("Packets", 2, 1..9)

    private val everyTick: Boolean by object : BooleanValue("EveryTick", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            packetTimingValue.update()
        }
    }

    private val packetTimingValue = object : ListValue("PacketTiming", arrayOf("Pre", "Post", "Both"), "Pre") {
        fun update() {
            values = if (everyTick) arrayOf("Pre", "Post", "Both") else arrayOf("Pre", "Post")
            if (value !in values) value = "Pre"
        }
    }
    private val packetTiming by packetTimingValue

    private var send = false

    override fun onPacket(event: PacketEvent) {
        if (event.packet.isUse) send = true
    }

    override fun onMotion(event: MotionEvent) {
        if ((event.eventState.name == packetTiming.uppercase() || packetTiming == "Both") && (send || everyTick)) {
            send = false
            if (packets <= 0) return

            if (packets == 1) {
                sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem), false)
                return
            }

            repeat(packets - 2) {
                serverSlot = (serverSlot + 1) % 9
            }

            val next = (serverSlot + 1) % 9
            serverSlot = if (next == mc.thePlayer.inventory.currentItem) (next + 1) % 9 else next
            serverSlot = mc.thePlayer.inventory.currentItem
        }
    }
}
