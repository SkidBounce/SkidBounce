/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner.canBeRepairedWithOther
import net.ccbluex.liquidbounce.utils.CoroutineUtils.waitUntil
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.hasItemAgePassed
import net.ccbluex.liquidbounce.utils.inventory.ArmorComparator.getBestArmorSet
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.autoCloseValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.closeDelayValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.hasScheduledInLastLoop
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.invOpen
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.invOpenValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveAirValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveGroundValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.simulateInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.simulateInventoryValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.startDelay
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.startDelayValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.isFirstInventoryClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.toHotbarIndex
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.EntityLiving.getArmorPosition
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange

object AutoArmor : Module("AutoArmor", Category.COMBAT) {
    private val maxDelay: Int by object : IntValue(
        "MaxDelay",
        50,
        0..500
    ) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay)
    }
    private val minDelay by object : IntValue("MinDelay", 50, 0..500) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay)

        override fun isSupported() = maxDelay > 0
    }
    private val minItemAge by IntValue("MinItemAge", 0, 0..2000)

    // InventoryMove values are inserted here

    // When swapping armor pieces, it grabs the better one, drags and swaps it with equipped one and drops the equipped one (no time of having no armor piece equipped)
    // Has to make more clicks, works slower
    val smartSwap by BooleanValue("SmartSwap", true)

    private val hotbar by BooleanValue("Hotbar", true)

    // Sacrifices 1 tick speed for complete undetectability, needed to bypass Vulcan
    private val delayedSlotSwitch by BooleanValue("DelayedSlotSwitch", true) { hotbar }

    // Prevents AutoArmor from hotbar equipping while any screen is open
    private val notInContainers by BooleanValue("NotInContainers", false) { hotbar }

    suspend fun equipFromHotbar() {
        if (!shouldOperate(onlyHotbar = true))
            return

        val thePlayer = mc.thePlayer ?: return

        var hasClickedHotbar = false

        val stacks = thePlayer.openContainer.inventory

        val bestArmorSet = getBestArmorSet(stacks) ?: return

        for (armorType in 0..3) {
            val (index, stack) = bestArmorSet[armorType] ?: continue

            // Check if the armor piece is in the hotbar
            val hotbarIndex = index?.toHotbarIndex(stacks.size) ?: continue

            if (index in TickScheduler || armorType + 5 in TickScheduler)
                continue

            if (!stack.hasItemAgePassed(minItemAge))
                continue

            val armorPos = getArmorPosition(stack) - 1

            // Check if target armor slot isn't occupied
            if (thePlayer.inventory.armorInventory[armorPos] != null)
                continue

            hasClickedHotbar = true

            val equippingAction = {
                // Switch selected hotbar slot, right click to equip
                sendPackets(
                    C09PacketHeldItemChange(hotbarIndex),
                    C08PacketPlayerBlockPlacement(stack)
                )

                // Instantly update inventory on client-side to prevent repetitive clicking because of ping
                thePlayer.inventory.armorInventory[armorPos] = stack
                thePlayer.inventory.mainInventory[hotbarIndex] = null
            }

            if (delayedSlotSwitch)
            // Schedule for the following tick and wait for them to be sent
                TickScheduler.scheduleAndSuspend(equippingAction)
            else
            // Schedule all possible hotbar clicks for the following tick (doesn't suspend the loop)
                TickScheduler += equippingAction
        }

        // Not really needed to bypass
        delay(randomDelay(minDelay, maxDelay).toLong())

        waitUntil(TickScheduler::isEmpty)

        // Sync selected slot next tick
        if (hasClickedHotbar)
            TickScheduler += { serverSlot = thePlayer.inventory.currentItem }
    }

    suspend fun equipFromInventory() {
        if (!shouldOperate())
            return

        val thePlayer = mc.thePlayer ?: return

        for (armorType in 0..3) {
            if (!shouldOperate())
                return

            val stacks = thePlayer.openContainer.inventory

            val armorSet = getBestArmorSet(stacks) ?: continue

            // Shouldn't iterate over armor set because after waiting for nomove and invopen it could be outdated
            val (index, stack) = armorSet[armorType] ?: continue

            // Index is null when searching in chests for already equipped armor to prevent any accidental impossible interactions
            index ?: continue

            // Check if best item is already scheduled to be equipped next tick
            if (index in TickScheduler || (armorType + 5) in TickScheduler)
                continue

            if (!stack.hasItemAgePassed(minItemAge))
                continue

            // Don't equip if it can be repaired with other armor piece, wait for the repair to happen first
            // Armor piece will then get equipped right after the repair
            if (canBeRepairedWithOther(stack, stacks))
                continue

            when (stacks[armorType + 5]) {
                // Best armor is already equipped
                stack -> continue

                // No item is equipped in armor slot
                null ->
                    // Equip by shift-clicking
                    click(index, 0, 1)

                else -> {
                    if (smartSwap) {
                        // Player has worse armor equipped, drag the best armor, swap it with currently equipped armor and drop the bad armor
                        // This way there is no time of having no armor (but more clicks)

                        // Grab better armor
                        click(index, 0, 0)

                        // Swap it with currently equipped armor
                        click(armorType + 5, 0, 0)

                        // Drop worse item by dragging and dropping it
                        click(-999, 0, 0)
                    } else {
                        // Normal version

                        // Drop worse armor
                        click(armorType + 5, 0, 4)

                        // Equip better armor
                        click(index, 0, 1)
                    }
                }
            }
        }

        // Wait till all scheduled clicks were sent
        waitUntil(TickScheduler::isEmpty)
    }

    fun equipFromHotbarInChest(hotbarIndex: Int?, stack: ItemStack) {
        // AutoArmor is disabled or prohibited from equipping while in containers
        if (hotbarIndex == null || !canEquipFromChest())
            return

        sendPackets(
            C09PacketHeldItemChange(hotbarIndex),
            C08PacketPlayerBlockPlacement(stack)
        )
    }

    fun canEquipFromChest() = handleEvents() && hotbar && !notInContainers

    private suspend fun shouldOperate(onlyHotbar: Boolean = false): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
                return false

            // It is impossible to equip armor when a container is open; only try to equip by right-clicking from hotbar (if NotInContainers is disabled)
            if (mc.thePlayer?.openContainer?.windowId != 0 && (!onlyHotbar || notInContainers))
                return false

            // Player doesn't need to have inventory open or not to move, when equipping from hotbar
            if (onlyHotbar)
                return hotbar

            if (invOpen && mc.currentScreen !is GuiInventory)
                return false

            // Wait till NoMove check isn't violated
            if (canClickInventory(closeWhenViolating = true))
                return true

            // If NoMove is violated, wait a tick and check again
            // If there is no delay, very weird things happen: https://www.guilded.gg/CCBlueX/groups/1dgpg8Jz/channels/034be45e-1b72-4d5a-bee7-d6ba52ba1657/chat?messageId=94d314cd-6dc4-41c7-84a7-212c8ea1cc2a
            delay(50)
        }
    }

    private suspend fun click(slot: Int, button: Int, mode: Int, allowDuplicates: Boolean = false) {
        // Wait for NoMove or cancel click
        if (!shouldOperate())
            return

        if (simulateInventory || invOpen)
            serverOpenInventory = true

        if (isFirstInventoryClick) {
            // Have to set this manually, because it would delay all clicks until a first scheduled click was sent
            isFirstInventoryClick = false

            delay(startDelay.toLong())
        }

        TickScheduler.scheduleClick(slot, button, mode, allowDuplicates)

        hasScheduledInLastLoop = true

        delay(randomDelay(minDelay, maxDelay).toLong())
    }

    override val values = super.values.toMutableList().apply {
        addAll(indexOfFirst { it.name == "SmartSwap" },
               listOf(
                   invOpenValue,
                   simulateInventoryValue,
                   autoCloseValue,
                   startDelayValue,
                   closeDelayValue,
                   noMoveValue,
                   noMoveAirValue,
                   noMoveGroundValue,
               )
        )
    }
}
