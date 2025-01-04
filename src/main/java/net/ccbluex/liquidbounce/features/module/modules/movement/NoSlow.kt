/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.SlowDownEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.BowNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.DrinkNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.FoodNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.SwordNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP2
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.isSplashPotion
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverUsing
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.Value
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING

object NoSlow : Module("NoSlow", Category.MOVEMENT, gameDetecting = false) {
    private val sword by object : BooleanValue("Sword", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            if (state) {
                if (newValue) SwordNoSlow.mode.onEnable()
                else SwordNoSlow.mode.onDisable()
            }
        }
    }
    private val food by object : BooleanValue("Food", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            if (state) {
                if (newValue) FoodNoSlow.mode.onEnable()
                else FoodNoSlow.mode.onDisable()
            }
        }
    }
    private val drink by object : BooleanValue("Drink", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            if (state) {
                if (newValue) DrinkNoSlow.mode.onEnable()
                else DrinkNoSlow.mode.onDisable()
            }
        }
    }
    private val bow by object : BooleanValue("Bow", true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            if (state) {
                if (newValue) BowNoSlow.mode.onEnable()
                else BowNoSlow.mode.onDisable()
            }
        }
    }

    @JvmStatic val sneaking by BooleanValue("Sneak", true)
    private val sneakMode by ListValue("Sneak-Mode", arrayOf("Vanilla", "Switch", "MineSecure"), "Vanilla") { sneaking }
    private val onlyMoveSneak by BooleanValue("Sneak-OnlyMove", true) { sneaking && sneakMode != "Vanilla" }
    @JvmStatic val sneakForwardMultiplier by FloatValue("Sneak-ForwardMultiplier", 0.3f, 0.3f..1.0F) { sneaking }
    @JvmStatic val sneakStrafeMultiplier by FloatValue("Sneak-StrafeMultiplier", 0.3f, 0.3f..1f) { sneaking }

    @JvmStatic val soulsand by BooleanValue("SoulSand", true)
    @JvmStatic val soulsandMultiplier by FloatValue("SoulSand-Multiplier", 1f, 0.4f..1f) { soulsand }

    @JvmStatic val slime by BooleanValue("Slime", true)
    @JvmStatic val slimeYMultiplier by FloatValue("Slime-YMultiplier", 1f, 0.2f..1f) { slime }
    @JvmStatic val slimeMultiplier by FloatValue("Slime-Multiplier", 1f, 0.4f..1f) { slime }
    private val slimeFriction by FloatValue("Slime-Friction", 0.6f, 0.6f..0.8f) { slime }

    override fun onDisable() {
        usedNoSlow?.mode?.onDisable()
        Blocks.slime_block.slipperiness = 0.8f
    }

    override fun onEnable() {
        usedNoSlow?.mode?.onEnable()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        antiDesync()

        Blocks.slime_block.slipperiness = if (slime) slimeFriction else 0.8f

        if (mc.thePlayer.isSneaking && (!onlyMoveSneak || isMoving) && sneaking) {
            when (sneakMode) {
                "Switch" -> when (event.eventState) {
                    PRE -> {
                        sendPackets(
                            C0BPacketEntityAction(mc.thePlayer, START_SNEAKING),
                            C0BPacketEntityAction(mc.thePlayer, STOP_SNEAKING)
                        )
                    }

                    POST -> {
                        sendPackets(
                            C0BPacketEntityAction(mc.thePlayer, STOP_SNEAKING),
                            C0BPacketEntityAction(mc.thePlayer, START_SNEAKING)
                        )
                    }
                    else -> {}
                }

                "MineSecure" -> if (event.eventState != PRE) {
                    sendPacket(C0BPacketEntityAction(mc.thePlayer, START_SNEAKING))
                }
            }
        }

        val shouldSwap = usedNoSlow?.mode?.let { it is UNCP  && it.shouldSwap } == true

        if (!shouldSwap && noMoveCheck) return

        if (isUsingItem || shouldSwap)
            usedNoSlow?.mode?.onMotion(event)
    }

    @EventTarget
    fun onUpdate() {
        antiDesync()

        if (!noMoveCheck && isUsingItem) usedNoSlow?.mode?.onUpdate()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (usedNoSlow?.mode?.handlePacketEventOnNoMove == true || !noMoveCheck) usedNoSlow?.mode?.onPacket(event)
    }

    fun doNoSlow() = handleEvents() && usedNoSlow?.mode?.canNoSlow == true

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        if (!doNoSlow()) return

        event.forward = usedNoSlow?.forwardMultiplier ?: 0.2f
        event.strafe = usedNoSlow?.strafeMultiplier ?: 0.2f
    }

    private val noMoveCheck
        get() = usedNoSlow?.run { !isMoving && (onlyMove && mode.allowNoMove) } ?: false

    private fun antiDesync() {
        if (usedNoSlow?.run { mode.antiDesync } == true && serverUsing && !isUsingItem)
            serverUsing = false
    }

    val usedNoSlow
        get() = mc.thePlayer?.heldItem?.run {
            return@run when {
                item is ItemSword && sword -> SwordNoSlow
                item is ItemBow && bow -> BowNoSlow
                item is ItemFood && food -> FoodNoSlow
                (item is ItemPotion && !isSplashPotion || item is ItemBucketMilk) && drink -> DrinkNoSlow
                else -> null
            }
        }

    fun isUNCPBlocking() = mc.gameSettings.keyBindUseItem.isKeyDown && usedNoSlow?.run { this == SwordNoSlow && mode is UNCP2 } ?: false

    val isUsingItem get() = mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && KillAura.blockStatus) || isUNCPBlocking())

    init {
        for (value in SwordNoSlow.values) {
            value.isSupported += { sword }
            value.name = "Sword-${value.name}"
        }

        for (value in FoodNoSlow.values) {
            value.isSupported += { food }
            value.name = "Food-${value.name}"
        }

        for (value in DrinkNoSlow.values) {
            value.isSupported += { drink }
            value.name = "Drink-${value.name}"
        }

        for (value in BowNoSlow.values) {
            value.isSupported += { bow }
            value.name = "Bow-${value.name}"
        }
    }

    override val values: List<Value<*>> = super.values.toMutableList().apply {
        addAll(indexOfFirst { it.name == "Sword" } + 1, SwordNoSlow.values)
        addAll(indexOfFirst { it.name == "Food" } + 1, FoodNoSlow.values)
        addAll(indexOfFirst { it.name == "Drink" } + 1, DrinkNoSlow.values)
        addAll(indexOfFirst { it.name == "Bow" } + 1, BowNoSlow.values)
    }
}
