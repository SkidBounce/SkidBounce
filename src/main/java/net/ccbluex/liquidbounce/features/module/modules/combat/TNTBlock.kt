/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isActuallyPressed
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntValue
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.item.ItemSword

object TNTBlock : Module("TNTBlock", Category.COMBAT, spacedName = "TNT Block") {
    private val fuse by IntValue("Fuse", 10, 0..80)
    private val range by FloatValue("Range", 9F, 1F..20F)
    private val autoSword by BooleanValue("AutoSword", true)
    private var blocked = false

    @EventTarget
    fun onMotionUpdate(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return

        for (entity in theWorld.loadedEntityList) {
            if (entity is EntityTNTPrimed && mc.thePlayer.getDistanceToEntity(entity) <= range) {
                if (entity.fuse <= fuse) {
                    if (autoSword) {
                        var slot = -1
                        var bestDamage = 1f
                        for (i in 0..8) {
                            val itemStack = thePlayer.inventory.getStackInSlot(i)

                            if (itemStack?.item is ItemSword) {
                                val itemDamage = (itemStack.item as ItemSword).damageVsEntity + 4F

                                if (itemDamage > bestDamage) {
                                    bestDamage = itemDamage
                                    slot = i
                                }
                            }
                        }

                        if (slot != -1 && slot != thePlayer.inventory.currentItem) {
                            thePlayer.inventory.currentItem = slot
                            mc.playerController.updateController()
                        }
                    }

                    if (mc.thePlayer.heldItem?.item is ItemSword) {
                        mc.gameSettings.keyBindUseItem.pressed = true
                        blocked = true
                    }

                    return
                }
            }
        }

        if (blocked && !mc.gameSettings.keyBindUseItem.isActuallyPressed) {
            mc.gameSettings.keyBindUseItem.pressed = false
            blocked = false
        }
    }
}
