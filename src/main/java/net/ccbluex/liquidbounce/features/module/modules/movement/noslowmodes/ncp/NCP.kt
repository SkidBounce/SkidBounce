/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp

import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.BlockPos.ORIGIN
import net.minecraft.util.EnumFacing.byName

/**
 * @author CCBlueX/LiquidBounce
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
class NCP : NoSlowMode("NCP", allowFood = false, allowDrink = false, allowBow = false) {
    private val invalidUsePosition by BooleanValue("InvalidUsePosition", false)
    private val nullUseItem by BooleanValue("NullUseItem", false)
    private val invalidReleasePosition by BooleanValue("InvalidReleasePosition", false)
    private val releaseDirection by ListValue("ReleaseDirection", arrayOf("Down", "Up", "North", "South", "West", "East"), "Down")

    override fun onMotion(event: MotionEvent) {
        when (event.eventState) {
            PRE -> sendPacket(
                C07PacketPlayerDigging(
                    RELEASE_USE_ITEM,
                    if (invalidReleasePosition) BlockPos(-1, -1, -1) else ORIGIN,
                    byName(releaseDirection.lowercase())
                )
            )

            POST -> sendPacket(
                C08PacketPlayerBlockPlacement(
                    if (invalidUsePosition) ORIGIN else BlockPos(-1, -1, -1),
                    255,
                    if (nullUseItem) null else mc.thePlayer.heldItem,
                    0f, 0f, 0f
                )
            )
            else -> {}
        }
    }
}
