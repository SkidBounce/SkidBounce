/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.TickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.hasItemAgePassed
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveAirValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveGroundValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.CLICK_TIMER
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow

object Refill : Module("Refill", Category.PLAYER) {
    private val delay by IntValue("Delay", 400, 10..1000)

    private val minItemAge by IntValue("MinItemAge", 400, 0..1000)

    private val mode by ListValue("Mode", arrayOf("Swap", "Merge"), "Swap")

    private val invOpen by BooleanValue("InvOpen", false)
    private val simulateInventory by BooleanValue("SimulateInventory", false) { !invOpen }

    // NoMove values are inserted here

    @EventTarget
    fun onTick(event: TickEvent) {
        if (!CLICK_TIMER.hasTimePassed(delay))
            return

        if (invOpen && mc.currentScreen !is GuiInventory)
            return

        if (!canClickInventory())
            return

        for (slot in 36..44) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(slot).stack ?: continue
            if (stack.stackSize == stack.maxStackSize || !stack.hasItemAgePassed(minItemAge)) continue

            when (mode) {
                "Swap" -> {
                    val bestOption = mc.thePlayer.inventoryContainer.inventory.withIndex()
                        .filter { (index, searchStack) ->
                            index < 36 && searchStack != null && searchStack.stackSize > stack.stackSize
                                    && (ItemStack.areItemsEqual(stack, searchStack)
                                    || searchStack.item.javaClass.isAssignableFrom(stack.item.javaClass)
                                    || stack.item.javaClass.isAssignableFrom(searchStack.item.javaClass))
                        }.maxByOrNull { it.value.stackSize }

                    if (bestOption != null) {
                        val (index, betterStack) = bestOption

                        click(index, slot - 36, 2, betterStack)
                        break
                    }
                }

                "Merge" -> {
                    val bestOption = mc.thePlayer.inventoryContainer.inventory.withIndex()
                        .filter { (index, searchStack) ->
                            index < 36 && searchStack != null && ItemStack.areItemsEqual(stack, searchStack)
                        }.minByOrNull { it.value.stackSize }

                    if (bestOption != null) {
                        val (otherSlot, otherStack) = bestOption

                        click(otherSlot, 0, 0, otherStack)
                        click(slot, 0, 0, stack)

                        // Return items that couldn't fit into hotbar slot
                        if (stack.stackSize + otherStack.stackSize > stack.maxStackSize)
                            click(otherSlot, 0, 0, otherStack)

                        break
                    }
                }
            }
        }

        if (simulateInventory && serverOpenInventory && mc.currentScreen !is GuiInventory)
            serverOpenInventory = false
    }

    fun click(slot: Int, button: Int, mode: Int, stack: ItemStack) {
        if (simulateInventory) serverOpenInventory = true

        sendPacket(
            C0EPacketClickWindow(
                mc.thePlayer.openContainer.windowId, slot, button, mode, stack,
                mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)
            )
        )
    }

    override val values = super.values.toMutableList().apply {
        addAll(indexOfFirst { it.name == "SimulateInventory" } + 1,
               listOf(
                   noMoveValue,
                   noMoveAirValue,
                   noMoveGroundValue,
               )
        )
    }
}
