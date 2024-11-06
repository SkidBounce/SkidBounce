/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.minecraft.entity.item.EntityTNTPrimed
import java.awt.Color

object TNTESP : Module("TNTESP", Category.RENDER, spacedName = "TNT ESP") {

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        mc.theWorld.loadedEntityList.filterIsInstance<EntityTNTPrimed>().forEach { drawEntityBox(it, Color.RED, false) }
    }
}
