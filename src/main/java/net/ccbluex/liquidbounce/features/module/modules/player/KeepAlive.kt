/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.init.Items
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange

object KeepAlive : Module("KeepAlive", Category.PLAYER) {

    val mode by ListValue("Mode", arrayOf("/heal", "Soup"), "/heal")

    private var runOnce = false

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isDead || thePlayer.health <= 0) {
            if (runOnce) return

            when (mode.lowercase()) {
                "/heal" -> thePlayer.sendChatMessage("/heal")
                "soup" -> {
                    val soupInHotbar = InventoryUtils.findItem(36, 44, Items.mushroom_stew)

                    if (soupInHotbar != null) {
                        sendPackets(
                            C09PacketHeldItemChange(soupInHotbar - 36),
                            C08PacketPlayerBlockPlacement(thePlayer.inventory.getStackInSlot(soupInHotbar)),
                            C09PacketHeldItemChange(thePlayer.inventory.currentItem)
                        )
                    }
                }
            }

            runOnce = true
        } else
            runOnce = false
    }
}
