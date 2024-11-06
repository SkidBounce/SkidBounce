/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.utils.ClassUtils.getAllObjects
import net.ccbluex.liquidbounce.utils.MovementUtils.aboveVoid
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.extensions.resetSpeed
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB.fromBounds

object NoFall : Module("NoFall", Category.PLAYER) {
    private val noFallModes = javaClass.`package`.getAllObjects<NoFallMode>().sortedBy { it.modeName }

    val mode by ListValue("Mode", noFallModes.map { it.modeName }.toTypedArray(), "SpoofGround")

    private val noVoid by BooleanValue("NoVoid", false)

    override fun onEnable() {
        modeModule.onEnable()
    }

    override fun onDisable() {
        modeModule.onDisable()
    }

    override fun onToggle(state: Boolean) {
        mc.timer.resetSpeed()
        modeModule.onToggle()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer ?: return

        if (void || FreeCam.handleEvents()) return

        if (collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } || collideBlock(
                fromBounds(
                    mc.thePlayer.entityBoundingBox.maxX,
                    mc.thePlayer.entityBoundingBox.maxY,
                    mc.thePlayer.entityBoundingBox.maxZ,
                    mc.thePlayer.entityBoundingBox.minX,
                    mc.thePlayer.entityBoundingBox.minY - 0.01,
                    mc.thePlayer.entityBoundingBox.minZ
                )
            ) { it is BlockLiquid }
        ) return

        modeModule.onUpdate()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        if (void) return

        modeModule.onPacket(event)
    }

    // Ignore condition used in LAAC mode
    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        modeModule.onJump(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (void) return
        modeModule.onMotion(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (void) return
        mc.thePlayer ?: return

        if (collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid }
            || collideBlock(
                fromBounds(
                    mc.thePlayer.entityBoundingBox.maxX,
                    mc.thePlayer.entityBoundingBox.maxY,
                    mc.thePlayer.entityBoundingBox.maxZ,
                    mc.thePlayer.entityBoundingBox.minX,
                    mc.thePlayer.entityBoundingBox.minY - 0.01,
                    mc.thePlayer.entityBoundingBox.minZ
                )
            ) { it is BlockLiquid }
        ) return

        modeModule.onMove(event)
    }

    override val tag
        get() = mode

    private val void
        get() = noVoid && aboveVoid

    private val modeModule
        get() = noFallModes.find { it.modeName == mode }!!

    override val values = getValuesWithModes(noFallModes, { it.modeName }, { it.values }, { it == modeModule }, "NoVoid")
}
