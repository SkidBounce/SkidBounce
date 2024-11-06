/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue

object FastPlace : Module("FastPlace", Category.WORLD) {
    val speed by IntValue("Speed", 0, 0..4)
    val onlyBlocks by BooleanValue("OnlyBlocks", true)
    val facingBlocks by BooleanValue("OnlyWhenFacingBlocks", true)
}
