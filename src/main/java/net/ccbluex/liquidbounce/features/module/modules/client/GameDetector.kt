/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.event.events.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.misc.StringUtils.contains
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion

object GameDetector : Module("GameDetector", Category.CLIENT, gameDetecting = false) {
    // Check if player's gamemode is Survival or Adventure
    private val gameMode by BooleanValue("GameModeCheck", true)

    // Check if player doesn't have unnatural capabilities
    private val capabilities by BooleanValue("CapabilitiesCheck", true)

    // Check if there are > 1 players in tablist
    private val tabList by BooleanValue("TabListCheck", true)

    // Check if there are > 1 teams or if friendly fire is enabled
    private val teams by BooleanValue("TeamsCheck", true)

    // Check if player doesn't have infinite invisibility effect
    private val invisibility by BooleanValue("InvisibilityCheck", true)

    // Check if player has compass inside their inventory
    private val compass by BooleanValue("CompassCheck", false)

    // Check for compass inside inventory. If false, then it should only check for selected slot
    private val checkAllSlots by BooleanValue("CheckAllSlots", true) { compass }
    private val slot by IntValue("Slot", 1, 1..9) { compass && !checkAllSlots }

    // Check for any hub-like BossBar or ArmorStand entities
    private val entity by BooleanValue("EntityCheck", false)

    // Check for strings in scoreboard that could signify that the game is waiting for players or if you are in a lobby
    // Needed on Gamster
    private val scoreboard by BooleanValue("ScoreboardCheck", false)

    private val WHITELISTED_SUBSTRINGS = arrayOf(":", "Vazio!", "§6§lRumble Box", "§5§lDivine Drop")

    private var isPlaying = false

    private val LOBBY_SUBSTRINGS = arrayOf("lobby", "hub", "waiting", "loading", "starting")

    fun isInGame() = !state || isPlaying

    @EventTarget(priority = 1)
    fun onUpdate(updateEvent: UpdateEvent) {
        isPlaying = false

        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return
        val netHandler = mc.netHandler ?: return
        val capabilities = thePlayer.capabilities

        val slots = slot - 1
        val itemSlot = mc.thePlayer.inventory.getStackInSlot(slots)

        if (gameMode && !mc.playerController.gameIsSurvivalOrAdventure())
            return

        if (GameDetector.capabilities &&
            (!capabilities.allowEdit || capabilities.allowFlying || capabilities.isFlying || capabilities.disableDamage)
        )
            return

        if (tabList && netHandler.playerInfoMap.size <= 1)
            return

        if (teams && thePlayer.team?.allowFriendlyFire == false && theWorld.scoreboard.teams.size == 1)
            return

        if (invisibility && thePlayer.getActivePotionEffect(Potion.invisibility)?.isPotionDurationMax == true)
            return

        if (compass) {
            if (checkAllSlots && mc.thePlayer.inventory.hasItemStack(ItemStack(Items.compass)))
                return

            if (!checkAllSlots && itemSlot?.item == Items.compass)
                return
        }

        if (compass && thePlayer.inventory.hasItem(Item.getItemById(345)))
            return

        if (scoreboard) {
            if (LOBBY_SUBSTRINGS in theWorld.scoreboard.getObjectiveInDisplaySlot(1)?.displayName)
                return

            if (theWorld.scoreboard.objectiveNames.any { LOBBY_SUBSTRINGS in it })
                return

            if (theWorld.scoreboard.teams.any { LOBBY_SUBSTRINGS in it.colorPrefix })
                return
        }

        if (entity) {
            for (entity in theWorld.loadedEntityList) {
                if (entity !is IBossDisplayData && entity !is EntityArmorStand)
                    continue

                val name = entity.customNameTag ?: continue

                // If an unnatural entity has been found, break the loop if its name includes a whitelisted substring
                if (WHITELISTED_SUBSTRINGS in name) break
                else return
            }
        }

        isPlaying = true
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        isPlaying = false
    }

    override fun handleEvents() = true
}
