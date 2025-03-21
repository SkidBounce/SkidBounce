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
import net.ccbluex.liquidbounce.features.module.modules.targets.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.targets.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object Tracers : Module("Tracers", Category.RENDER) {

    private val colorMode by ListValue("Color", arrayOf("Custom", "DistanceColor", "Rainbow"), "Custom")
    private val colorRed by IntValue(
        "R",
        0,
        0..255
    ) { colorMode == "Custom" }
    private val colorGreen by IntValue(
        "G",
        160,
        0..255
    ) { colorMode == "Custom" }
    private val colorBlue by IntValue(
        "B",
        255,
        0..255
    ) { colorMode == "Custom" }

    private val thickness by FloatValue("Thickness", 2F, 1F..5F)

    private val bot by BooleanValue("Bots", true)
    private val teams by BooleanValue("Teams", false)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val thePlayer = mc.thePlayer ?: return

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(thickness)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        glBegin(GL_LINES)

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !bot && isBot(entity)) continue
            if (entity != thePlayer && isSelected(entity, false)) {
                val dist = (thePlayer.getDistanceToEntity(entity) * 2).toInt().coerceAtMost(255)

                val colorMode = colorMode.lowercase()
                val color = when {
                    entity is EntityPlayer && entity.isClientFriend -> Color(0, 0, 255, 150)
                    teams && Teams.state && Teams.isInYourTeam(entity) -> Color(0, 162, 232)
                    colorMode == "custom" -> Color(colorRed, colorGreen, colorBlue, 150)
                    colorMode == "distancecolor" -> Color(255 - dist, dist, 0, 150)
                    colorMode == "rainbow" -> ColorUtils.rainbow()
                    else -> Color(255, 255, 255, 150)
                }

                drawTraces(entity, color)
            }
        }

        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawTraces(entity: Entity, color: Color) {
        val thePlayer = mc.thePlayer ?: return

        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosZ)

        val yaw =
            thePlayer.prevRotationYaw + (thePlayer.rotationYaw - thePlayer.prevRotationYaw) * mc.timer.renderPartialTicks
        val pitch =
            thePlayer.prevRotationPitch + (thePlayer.rotationPitch - thePlayer.prevRotationPitch) * mc.timer.renderPartialTicks

        val eyeVector = Vec3(0.0, 0.0, 1.0).rotatePitch(-pitch.toRadians()).rotateYaw(-yaw.toRadians())

        glColor(color)

        glVertex3d(eyeVector.xCoord, thePlayer.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord)
        glVertex3d(x, y, z)
        glVertex3d(x, y, z)
        glVertex3d(x, y + entity.height, z)
    }
}
