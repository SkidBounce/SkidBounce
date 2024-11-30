/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other.Strafe
import net.ccbluex.liquidbounce.utils.ClassUtils.getAllObjects
import net.ccbluex.liquidbounce.utils.ClassUtils.getValues
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.Value

object Speed : Module("Speed", Category.MOVEMENT) {
    private val speedModes = javaClass.`package`.getAllObjects<SpeedMode>().sortedBy { it.modeName }

    private val moduleModes = speedModes.map { it.modeName }.toTypedArray()

    private val alwaysSprint by BooleanValue("AlwaysSprint", false)
    private val whenSneaking by BooleanValue("WhenSneaking", false)
    private val inLiquid by BooleanValue("InLiquid", false)
    private val inWeb by BooleanValue("InWeb", false)
    private val onLadder by BooleanValue("OnLadder", false)
    private val whenRiding by BooleanValue("WhenRiding", false)
    private val onFly by BooleanValue("onFly", false)

    private val normalMode by ListValue("NormalMode", moduleModes, "NCPBHop")
    private val jumpingMode by ListValue("JumpingMode", arrayOf("None") + moduleModes, "None")

    private var currentMode = normalMode
    private var wasSpeed = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer ?: return

        updateJumping()

        if (isMoving && alwaysSprint)
            mc.thePlayer.isSprinting = true

        modeModule.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        updateJumping()

        if (event.eventState != PRE)
            return

        if (isMoving && alwaysSprint)
            thePlayer.isSprinting = true

        modeModule.onMotion(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        updateJumping()
        modeModule.onMove(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        updateJumping()
        modeModule.onTick()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        updateJumping()

        modeModule.onPacket(event)
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        updateJumping()
        modeModule.onStrafe()
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        updateJumping()
        modeModule.onJump(event)
    }

    override fun onToggle(state: Boolean) {
        mc.thePlayer ?: return

        mc.timer.resetSpeed()
        mc.thePlayer.speedInAir = 0.02f
        mc.thePlayer.jumpMovementFactor = 0.02f

        modeModule.onToggle(state)
    }

    override fun onEnable() {
        mc.thePlayer ?: return
        modeModule.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer ?: return
        modeModule.onDisable()
    }

    override val tag
        get() = currentMode

    private val modeModule
        get() = speedModes.find { it.modeName == currentMode }!!

    private val modes
        get() = speedModes.filter { it.modeName in arrayOf(normalMode, jumpingMode) }

    override fun handleEvents(): Boolean {
        val shouldSpeed = mc.thePlayer != null
                && (inLiquid || !mc.thePlayer.inLiquid)
                && (whenSneaking || !mc.thePlayer.isSneaking)
                && (inWeb || !mc.thePlayer.isInWeb)
                && (onLadder || !mc.thePlayer.isOnLadder)
                && (whenRiding || !mc.thePlayer.isRiding)
                && mc.thePlayer != null
                && (onFly || !Fly.handleEvents())
                && super.handleEvents()

        if (shouldSpeed != wasSpeed) {
            onToggle(shouldSpeed)
            if (shouldSpeed) onEnable() else onDisable()
            wasSpeed = shouldSpeed
        }

        return shouldSpeed
    }

    private fun updateJumping() {
        mc.gameSettings.keyBindJump.update()
        if (jumpingMode != "None") {
            val last = modeModule
            currentMode = if (mc.gameSettings.keyBindJump.isActuallyPressed && !mc.thePlayer.inLiquid) jumpingMode else normalMode
            if (currentMode != last.modeName) {
                last.onDisable()
                modeModule.onEnable()
            }
        } else currentMode = normalMode

        if (!modeModule.allowsJumping && !mc.thePlayer.inLiquid)
            mc.gameSettings.keyBindJump.pressed = false
    }

    override val values: List<Value<*>> = super.values.toMutableList().apply {
        addAll(lastIndex, speedModes.map { mode ->
            getValues(mode).onEach {
                it.name = "${mode.modeName}-${it.name}"
                it.isSupported += { mode in modes }
            }
        }.flatten())
    }
}
