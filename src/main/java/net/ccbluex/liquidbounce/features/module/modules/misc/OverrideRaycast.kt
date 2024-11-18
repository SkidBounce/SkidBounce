/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BooleanValue

object OverrideRaycast : Module("OverrideRaycast", Category.MISC, gameDetecting = false) {
    private val alwaysActive by BooleanValue("AlwaysActive", true)

    fun shouldOverride() = handleEvents() || alwaysActive
}
