/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.extensions.isSplashPotion
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.potion.Potion

object AutoPot : Module("AutoPot", Category.COMBAT) {

    private val health by FloatValue("Health", 15F, 1F..20F) { healPotion || regenerationPotion }
    private val delay by IntValue("Delay", 500, 500..1000)

    // Useful potion options
    private val healPotion by BooleanValue("HealPotion", true)
    private val regenerationPotion by BooleanValue("RegenPotion", true)
    private val fireResistancePotion by BooleanValue("FireResPotion", true)
    private val strengthPotion by BooleanValue("StrengthPotion", true)
    private val jumpPotion by BooleanValue("JumpPotion", true)
    private val speedPotion by BooleanValue("SpeedPotion", true)

    private val openInventory by BooleanValue("OpenInv", false)
    private val simulateInventory by BooleanValue("SimulateInventory", true) { !openInventory }

    private val groundDistance by FloatValue("GroundDistance", 2F, 0F..5F)
    private val mode by ListValue("Mode", arrayOf("Normal", "Jump", "Port"), "Normal")

    private val msTimer = MSTimer()
    private var potion = -1

    @EventTarget
    fun onMotion(motionEvent: MotionEvent) {
        if (!msTimer.hasTimePassed(delay) || mc.playerController.isInCreativeMode)
            return

        val thePlayer = mc.thePlayer ?: return

        when (motionEvent.eventState) {
            PRE -> {
                // Hotbar Potion
                val potionInHotbar = findPotion(36, 45)

                if (potionInHotbar != null) {
                    if (thePlayer.onGround) {
                        when (mode.lowercase()) {
                            "jump" -> thePlayer.jmp()
                            "port" -> thePlayer.moveEntity(0.0, 0.42, 0.0)
                        }
                    }

                    // Prevent throwing potions into the void
                    val fallingPlayer = FallingPlayer(thePlayer)

                    val collisionBlock = fallingPlayer.findCollision(20)?.pos

                    if (thePlayer.posY - (collisionBlock?.y ?: return) - 1 > groundDistance)
                        return

                    potion = potionInHotbar
                    sendPacket(C09PacketHeldItemChange(potion - 36))

                    if (thePlayer.rotationPitch <= 80F) {
                        setTargetRotation(Rotation(thePlayer.rotationYaw, nextFloat(80F, 90F)).fixedSensitivity(),
                            immediate = true
                        )
                    }
                    return
                }

                // Inventory Potion -> Hotbar Potion
                val potionInInventory = findPotion(9, 36) ?: return
                if (InventoryUtils.hasSpaceInHotbar()) {
                    if (openInventory && mc.currentScreen !is GuiInventory)
                        return

                    if (simulateInventory)
                        serverOpenInventory = true

                    mc.playerController.windowClick(0, potionInInventory, 0, 1, thePlayer)

                    if (simulateInventory && mc.currentScreen !is GuiInventory)
                        serverOpenInventory = false

                    msTimer.reset()
                }
            }

            POST -> {
                if (potion >= 0 && serverRotation.pitch >= 75F) {
                    val itemStack = thePlayer.inventoryContainer.getSlot(potion).stack

                    if (itemStack != null) {
                        sendPackets(
                            C08PacketPlayerBlockPlacement(itemStack),
                            C09PacketHeldItemChange(thePlayer.inventory.currentItem)
                        )

                        msTimer.reset()
                    }

                    potion = -1
                }
            }
            else -> {}
        }
    }

    private fun findPotion(startSlot: Int, endSlot: Int): Int? {
        val thePlayer = mc.thePlayer

        for (i in startSlot until endSlot) {
            val stack = thePlayer.inventoryContainer.getSlot(i).stack

            if (stack == null || stack.item !is ItemPotion || !stack.isSplashPotion)
                continue

            val itemPotion = stack.item as ItemPotion

            for (potionEffect in itemPotion.getEffects(stack))
                if (thePlayer.health <= health && healPotion && potionEffect.potionID == Potion.heal.id)
                    return i

            if (!thePlayer.isPotionActive(Potion.regeneration))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (thePlayer.health <= health && regenerationPotion && potionEffect.potionID == Potion.regeneration.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.fireResistance))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (fireResistancePotion && potionEffect.potionID == Potion.fireResistance.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.moveSpeed))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (speedPotion && potionEffect.potionID == Potion.moveSpeed.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.jump))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (jumpPotion && potionEffect.potionID == Potion.jump.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.damageBoost))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (strengthPotion && potionEffect.potionID == Potion.damageBoost.id)
                        return i
        }

        return null
    }

    override val tag
        get() = health.toString()

}
