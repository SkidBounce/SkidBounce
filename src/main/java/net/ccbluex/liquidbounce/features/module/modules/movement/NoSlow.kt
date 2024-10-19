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
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory.MOVEMENT
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.BlockingNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.BowNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ConsumeNoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP.shouldSwap
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP2
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other.*
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.isSplashPotion
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverUsing
import net.ccbluex.liquidbounce.value.*
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING

object NoSlow : Module("NoSlow", MOVEMENT, gameDetecting = false) {
    private val blocking by BooleanValue("Blocking", true)
    private val consume by BooleanValue("Consume", true)
    private val bow by BooleanValue("Bow", true)

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
        shouldSwap = false
        Blocks.slime_block.slipperiness = 0.8f
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

        if (!shouldSwap && noMoveCheck) return

        if (isUsingItem || shouldSwap)
            usedMode.onMotion(event)
    }

    @EventTarget
    fun onUpdate() {
        antiDesync()

        if (!noMoveCheck && isUsingItem) usedMode.onUpdate()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (usedMode.handlePacketEventOnNoMove || !noMoveCheck) usedMode.onPacket(event)
    }

    fun doNoSlow() = handleEvents() && usedNoSlow != null && usedMode.canNoSlow

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        if (!doNoSlow()) return

        event.forward = usedNoSlow?.forwardMultiplier ?: 0.2f
        event.strafe = usedNoSlow?.strafeMultiplier ?: 0.2f
    }

    private val noMoveCheck
        get() = usedNoSlow?.run { mode.allowNoMove && isMoving && onlyMove } ?: false

    private fun antiDesync() {
        if (usedMode.antiDesync && serverUsing && !isUsingItem)
            serverUsing = false
    }

    private val usedNoSlow
        get() = mc.thePlayer?.heldItem?.run {
            return@run when {
                item is ItemSword && blocking -> BlockingNoSlow
                item is ItemBow && bow -> BowNoSlow
                (item is ItemPotion && !isSplashPotion || item is ItemFood || item is ItemBucketMilk) && consume -> ConsumeNoSlow
                else -> null
            }
        }

    val usedMode: NoSlowMode
        get() = usedNoSlow?.mode ?: Vanilla

    fun isUNCPBlocking() = mc.gameSettings.keyBindUseItem.isKeyDown && usedNoSlow?.run { this == BlockingNoSlow && mode is UNCP2 } ?: false

    private val isUsingItem get() = mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && KillAura.blockStatus) || isUNCPBlocking())

    init {
        for (value in BlockingNoSlow.values) {
            value.isSupported += { blocking }
            value.name = "Blocking-${value.name}"
        }

        for (value in ConsumeNoSlow.values) {
            value.isSupported += { consume }
            value.name = "Consume-${value.name}"
        }

        for (value in BowNoSlow.values) {
            value.isSupported += { bow }
            value.name = "Bow-${value.name}"
        }
    }

    override val values: List<Value<*>> = super.values.toMutableList().apply {
        addAll(indexOfFirst { it.name == "Blocking" } + 1, BlockingNoSlow.values)
        addAll(indexOfFirst { it.name == "Consume" } + 1, ConsumeNoSlow.values)
        addAll(indexOfFirst { it.name == "Bow" } + 1, BowNoSlow.values)
    }
}
