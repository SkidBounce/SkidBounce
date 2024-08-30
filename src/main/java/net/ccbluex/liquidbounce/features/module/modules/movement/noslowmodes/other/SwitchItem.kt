/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.blockingSwitchItemEveryTick
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.blockingSwitchItemPackets
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.bowSwitchItemEveryTick
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.bowSwitchItemPackets
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.consumeSwitchItemEveryTick
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.consumeSwitchItemPackets
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.noSlowItem
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow.packetTiming
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.NoSlowItem.*
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.isRelease
import net.ccbluex.liquidbounce.utils.extensions.isUse
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.minecraft.network.play.client.C09PacketHeldItemChange

/**
 * @author CCBlueX/LiquidBounce
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object SwitchItem : NoSlowMode("SwitchItem") {
    private var send = false

    override fun onPacket(event: PacketEvent) {
        when {
            event.packet.isUse -> send = true
            event.packet.isRelease
                    || !mc.thePlayer.isUsingItem
            -> send = false
        }
    }

    override fun onMotion(event: MotionEvent) {
        if (packetTiming(event.eventState)) {
            val packets = when (noSlowItem) {
                CONSUMABLE -> {
                    if (!consumeSwitchItemEveryTick && !send) return
                    consumeSwitchItemPackets
                }
                SWORD -> {
                    if (!blockingSwitchItemEveryTick && !send) return
                    blockingSwitchItemPackets
                }
                BOW -> {
                    if (!bowSwitchItemEveryTick && !send) return
                    bowSwitchItemPackets
                }
                OTHER -> throw Exception("NoSlowItem.OTHER does not map to a value. Please report this error at https://github.com/SkidBounce/SkidBounce/issues")
            }

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

            send = false
        }
    }
}
