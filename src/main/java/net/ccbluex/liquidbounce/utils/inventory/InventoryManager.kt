/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.utils.inventory

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.minecraft.client.gui.inventory.GuiInventory

object InventoryManager: MinecraftInstance() {

	// Shared no move click values
	val noMoveValue = BooleanValue("NoMoveClicks", false)
	val noMoveAirValue = BooleanValue("NoClicksInAir", false) { noMove }
	val noMoveGroundValue = BooleanValue("NoClicksOnGround", true) { noMove }

	// Shared values between AutoArmor and InventoryCleaner
	val invOpenValue = BooleanValue("InvOpen", false)
	val simulateInventoryValue = BooleanValue("SimulateInventory", true) { !invOpen }
	val autoCloseValue = BooleanValue("AutoClose", false) { invOpen }

	val startDelayValue = IntValue("StartDelay", 0, 0..500) { invOpen || simulateInventory }
	val closeDelayValue = IntValue("CloseDelay", 0, 0..500) { if (invOpen) autoClose else simulateInventory }

	val noMove by noMoveValue
	val noMoveAir by noMoveAirValue
	val noMoveGround by noMoveGroundValue

	val invOpen by invOpenValue
	val simulateInventory by simulateInventoryValue
	val autoClose by autoCloseValue

	val startDelay by startDelayValue
	val closeDelay by closeDelayValue

	private lateinit var inventoryWorker: Job

	var hasScheduledInLastLoop = false
		set(value) {
			// If hasScheduled gets set to true any time during the searching loop, inventory can be closed when the loop finishes.
			if (value) canCloseInventory = true

			field = value
		}

	private var canCloseInventory = false

	private suspend fun manageInventory() {

		/**
		 * ChestStealer actions
		 */

		ChestStealer.stealFromChest()

		/**
		 * AutoArmor actions
		 */

		AutoArmor.equipFromHotbar()

		// Following actions require inventory / simulated inventory, ...

		// TODO: This could be at start of each action?
		// Don't wait for NoMove not to be violated, check if there is anything to equip from hotbar and such by looping again
		if (!canClickInventory() || (invOpen && mc.currentScreen !is GuiInventory))
			return

		canCloseInventory = false

		while (true) {
			hasScheduledInLastLoop = false

			AutoArmor.equipFromInventory()

			/**
			 * InventoryCleaner actions
			 */

			// Repair useful equipment by merging in the crafting grid
			InventoryCleaner.repairEquipment()

			// Compact multiple small stacks into one to free up inventory space
			InventoryCleaner.mergeStacks()

			// Sort hotbar (with useful items without even dropping bad items first)
			InventoryCleaner.sortHotbar()

			// Drop bad items to free up inventory space
			InventoryCleaner.dropGarbage()

			// Stores which action should be executed to close open inventory or simulated inventory
			// If no clicks were scheduled throughout any iteration (canCloseInventory == false), then it is null, to prevent closing inventory all the time
			closingAction ?: return

			// Prepare for closing the inventory
			delay(closeDelay.toLong())

			// Try to search through inventory one more time, only close when no actions were scheduled in current iteration
			if (!hasScheduledInLastLoop) {
				closingAction?.invoke()
				return
			}
		}
	}

	private val closingAction
		get() = when {
			// Check if any click was scheduled since inventory got open
			!canCloseInventory -> null

			// Prevent any other container guis from getting closed
			mc.thePlayer?.openContainer?.windowId != 0 -> null

			// Check if open inventory should be closed
			mc.currentScreen is GuiInventory && invOpen && autoClose ->
				({ mc.thePlayer?.closeScreen() })

			// Check if simulated inventory should be closed
			mc.currentScreen !is GuiInventory && simulateInventory && serverOpenInventory ->
				({ serverOpenInventory = false })

			else -> null
		}

	fun canClickInventory(closeWhenViolating: Boolean = false) =
		if (noMove && isMoving && if (serverOnGround) noMoveGround else noMoveAir) {

			// NoMove check is violated, close simulated inventory
			if (closeWhenViolating)
				serverOpenInventory = false

			false
		} else true // Simulated inventory will get reopen before a window click, delaying it by start delay

	fun startCoroutine() {
		inventoryWorker = CoroutineScope(Dispatchers.Default).launch {
			while (isActive) {
				runCatching {
					manageInventory()
				}.onFailure {
					// TODO: Remove when stable, probably in b86
					displayClientMessage("§cReworked coroutine inventory management had ran into an issue! Please report this: ${it.message ?: it.cause}")

					it.printStackTrace()
				}
			}
		}
	}
}
