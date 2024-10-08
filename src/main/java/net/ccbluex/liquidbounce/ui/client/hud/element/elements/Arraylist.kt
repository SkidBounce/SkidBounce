/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.GameDetector
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.*
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.*
import net.ccbluex.liquidbounce.ui.font.*
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.shader.shaders.*
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(
    x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP)
) : Element(x, y, scale, side) {

    private val textColorMode by ListValue("Text-Color", arrayOf("Custom", "Random", "Rainbow", "Gradient"), "Rainbow")
    private val textRed by IntValue("Text-R", 0, 0..255) { textColorMode == "Custom" }
    private val textGreen by IntValue("Text-G", 111, 0..255) { textColorMode == "Custom" }
    private val textBlue by IntValue("Text-B", 255, 0..255) { textColorMode == "Custom" }

    // TODO: Make Color picker to fix this mess :/
    private val gradientTextRed1 by FloatValue("Text-Gradient-R1", 255f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen1 by FloatValue("Text-Gradient-G1", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue1 by FloatValue("Text-Gradient-B1", 0f, 0f..255f) { textColorMode == "Gradient" }

    private val gradientTextRed2 by FloatValue("Text-Gradient-R2", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen2 by FloatValue("Text-Gradient-G2", 255f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue2 by FloatValue("Text-Gradient-B2", 0f, 0f..255f) { textColorMode == "Gradient" }

    private val gradientTextRed3 by FloatValue("Text-Gradient-R3", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen3 by FloatValue("Text-Gradient-G3", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue3 by FloatValue("Text-Gradient-B3", 255f, 0f..255f) { textColorMode == "Gradient" }

    private val gradientTextRed4 by FloatValue("Text-Gradient-R4", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen4 by FloatValue("Text-Gradient-G4", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue4 by FloatValue("Text-Gradient-B4", 0f, 0f..255f) { textColorMode == "Gradient" }

    private val rectMode by ListValue("Rect", arrayOf("None", "Left", "Right"), "None")
    private val roundedRectRadius by FloatValue("RoundedRect-Radius", 0F, 0F..2F)
    private val rectColorMode by ListValue("Rect-Color", arrayOf("Custom", "Random", "Rainbow", "Gradient"), "Rainbow") { rectMode != "None" }
    private val isCustomRectSupported = { rectMode != "None" && rectColorMode == "Custom" }
    private val rectRed by IntValue("Rect-R", 255, 0..255, isSupported = isCustomRectSupported)
    private val rectGreen by IntValue("Rect-G", 255, 0..255, isSupported = isCustomRectSupported)
    private val rectBlue by IntValue("Rect-B", 255, 0..255, isSupported = isCustomRectSupported)
    private val rectAlpha by IntValue("Rect-Alpha", 255, 0..255, isSupported = isCustomRectSupported)

    // TODO: Make Color picker to fix this mess :/
    private val gradientRectRed1 by FloatValue("Rect-Gradient-R1", 255f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen1 by FloatValue("Rect-Gradient-G1", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue1 by FloatValue("Rect-Gradient-B1", 0f, 0f..255f) { rectColorMode == "Gradient" }

    private val gradientRectRed2 by FloatValue("Rect-Gradient-R2", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen2 by FloatValue("Rect-Gradient-G2", 255f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue2 by FloatValue("Rect-Gradient-B2", 0f, 0f..255f) { rectColorMode == "Gradient" }

    private val gradientRectRed3 by FloatValue("Rect-Gradient-R3", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen3 by FloatValue("Rect-Gradient-G3", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue3 by FloatValue("Rect-Gradient-B3", 255f, 0f..255f) { rectColorMode == "Gradient" }

    private val gradientRectRed4 by FloatValue("Rect-Gradient-R4", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen4 by FloatValue("Rect-Gradient-G4", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue4 by FloatValue("Rect-Gradient-B4", 0f, 0f..255f) { rectColorMode == "Gradient" }

    private val roundedBackgroundRadius by FloatValue("RoundedBackGround-Radius", 0F, 0F..5F)
    private val backgroundMode by ListValue("Background-Color", arrayOf("Custom", "Random", "Rainbow", "Gradient"), "Custom")
    private val backgroundRed by IntValue("Background-R", 0, 0..255) { backgroundMode == "Custom" }
    private val backgroundGreen by IntValue("Background-G", 0, 0..255) { backgroundMode == "Custom" }
    private val backgroundBlue by IntValue("Background-B", 0, 0..255) { backgroundMode == "Custom" }
    private val backgroundAlpha by IntValue("Background-Alpha", 127, 0..255) { backgroundMode == "Custom" }

    // TODO: Make Color picker to fix this mess :/
    private val gradientBackgroundRed1 by FloatValue("Background-Gradient-R1", 255f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen1 by FloatValue("Background-Gradient-G1", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue1 by FloatValue("Background-Gradient-B1", 0f, 0f..255f) { backgroundMode == "Gradient" }

    private val gradientBackgroundRed2 by FloatValue("Background-Gradient-R2", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen2 by FloatValue("Background-Gradient-G2", 255f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue2 by FloatValue("Background-Gradient-B2", 0f, 0f..255f) { backgroundMode == "Gradient" }

    private val gradientBackgroundRed3 by FloatValue("Background-Gradient-R3", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen3 by FloatValue("Background-Gradient-G3", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue3 by FloatValue("Background-Gradient-B3", 255f, 0f..255f) { backgroundMode == "Gradient" }

    private val gradientBackgroundRed4 by FloatValue("Background-Gradient-R4", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen4 by FloatValue("Background-Gradient-G4", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue4 by FloatValue("Background-Gradient-B4", 0f, 0f..255f) { backgroundMode == "Gradient" }

    private fun isColorModeUsed(value: String) = textColorMode == value || rectMode == value || backgroundMode == value

    private val saturation by FloatValue("Random-Saturation", 0.9f, 0f..1f) { isColorModeUsed("Random") }
    private val brightness by FloatValue("Random-Brightness", 1f, 0f..1f) { isColorModeUsed("Random") }
    private val rainbowX by FloatValue("Rainbow-X", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val rainbowY by FloatValue("Rainbow-Y", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val gradientX by FloatValue("Gradient-X", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }
    private val gradientY by FloatValue("Gradient-Y", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }

    private val tags by BooleanValue("Tags", true)
    private val tagsStyle by object : ListValue("TagsStyle", arrayOf("[]", "()", "<>", "-", "|", "Space"), "Space") {
        override fun isSupported() = tags

        // onUpdate - updates tag onInit and onChanged
        override fun onUpdate(value: String) = updateTagDetails()
    }
    private val tagsCase by ListValue("TagsCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal") { tags }
    private val tagsArrayColor by object : BooleanValue("TagsArrayColor", false) {
        override fun isSupported() = tags
        override fun onUpdate(value: Boolean) = updateTagDetails()
    }

    private val font by FontValue("Font", Fonts.minecraftFont)
    private val textShadow by BooleanValue("ShadowText", true)
    private val moduleCase by ListValue("ModuleCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal")
    private val space by FloatValue("Space", 0F, 0F..5F)
    private val textHeight by FloatValue("TextHeight", 11F, 1F..20F)
    private val textY by FloatValue("TextY", 1.5F, 0F..20F)

    private val animation by ListValue("Animation", arrayOf("Slide", "Smooth"), "Smooth") { tags }
    private val animationSpeed by FloatValue("AnimationSpeed", 0.2F, 0.01F..1F) { animation == "Smooth" }

    companion object {
        val spacedModules by BooleanValue("SpacedModules", false)
        val inactiveStyle by ListValue("InactiveModulesStyle", arrayOf("Normal", "Color", "Hide"), "Color")
        { GameDetector.state }
    }

    private var x2 = 0
    private var y2 = 0F

    private lateinit var tagPrefix: String

    private lateinit var tagSuffix: String

    private var modules = emptyList<Module>()

    private val inactiveColor = Color(255, 255, 255, 100).rgb

    init {
        updateTagDetails()
    }

    fun updateTagDetails() {
        val pair: Pair<String, String> = when (tagsStyle) {
            "[]", "()", "<>" -> tagsStyle[0].toString() to tagsStyle[1].toString()
            "-", "|" -> tagsStyle[0] + " " to ""
            else -> "" to ""
        }

        tagPrefix = (if (tagsArrayColor) " " else " §7") + pair.first
        tagSuffix = pair.second
    }

    private fun getDisplayString(module: Module): String {
        val moduleName = when (moduleCase) {
            "Uppercase" -> module.getName().uppercase()
            "Lowercase" -> module.getName().lowercase()
            else -> module.getName()
        }

        var tag = module.tag ?: ""

        tag = when (tagsCase) {
            "Uppercase" -> tag.uppercase()
            "Lowercase" -> tag.lowercase()
            else -> tag
        }

        val moduleTag = if (tags && !module.tag.isNullOrEmpty()) tagPrefix + tag + tagSuffix else ""

        return moduleName + moduleTag
    }

    override fun drawElement(): Border? {

        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = deltaTime

        for (module in moduleManager.modules) {
            val shouldShow = (module.inArray && module.state && (inactiveStyle != "Hide" || module.isActive))

            if (!shouldShow && module.slide <= 0f)
                continue

            val displayString = getDisplayString(module)

            val width = font.getStringWidth(displayString)

            when (animation) {
                "Slide" -> {
                    // If modules become inactive because they only work when in game, animate them as if they got disabled
                    module.slideStep += if (shouldShow) delta / 4F else -delta / 4F
                    if (shouldShow) {
                        if (module.slide < width) {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                        }
                    } else {
                        module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                    }

                    module.slide = module.slide.coerceIn(0F, width.toFloat())
                    module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
                }

                "Smooth" -> {
                    val target = if (shouldShow) width.toDouble() else -width / 5.0
                    module.slide =
                        AnimationUtil.base(module.slide.toDouble(), target, animationSpeed.toDouble()).toFloat()
                }
            }
        }

        // Draw arraylist
        val textCustomColor = Color(textRed, textGreen, textBlue, 1).rgb
        val rectCustomColor = Color(rectRed, rectGreen, rectBlue, rectAlpha).rgb
        val backgroundCustomColor = Color(backgroundRed, backgroundGreen, backgroundBlue, backgroundAlpha).rgb
        val textSpacer = textHeight + space

        val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
        val rainbowX = if (rainbowX == 0f) 0f else 1f / rainbowX
        val rainbowY = if (rainbowY == 0f) 0f else 1f / rainbowY

        val gradientOffset = System.currentTimeMillis() % 10000 / 10000F
        val gradientX = if (gradientX == 0f) 0f else 1f / gradientX
        val gradientY = if (gradientY == 0f) 0f else 1f / gradientY

        modules.forEachIndexed { index, module ->
            var yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                    if (side.vertical == Vertical.DOWN) index + 1 else index
            if (animation == "Smooth") {
                module.yAnim = AnimationUtil.base(module.yAnim.toDouble(), yPos.toDouble(), 0.2).toFloat()
                yPos = module.yAnim
            }

            val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

            val markAsInactive = inactiveStyle == "Color" && !module.isActive

            val displayString = getDisplayString(module)

            when (side.horizontal) {
                Horizontal.RIGHT, Horizontal.MIDDLE -> {
                    val xPos = -module.slide - 2

                    GradientShader.apply {
                        color1 = floatArrayOf(
                            gradientBackgroundRed1 / 255.0f,
                            gradientBackgroundGreen1 / 255.0f,
                            gradientBackgroundBlue1 / 255.0f,
                            1.0f
                        )
                        color2 = floatArrayOf(
                            gradientBackgroundRed2 / 255.0f,
                            gradientBackgroundGreen2 / 255.0f,
                            gradientBackgroundBlue2 / 255.0f,
                            1.0f
                        )
                        color3 = floatArrayOf(
                            gradientBackgroundRed3 / 255.0f,
                            gradientBackgroundGreen3 / 255.0f,
                            gradientBackgroundBlue3 / 255.0f,
                            1.0f
                        )
                        color4 = floatArrayOf(
                            gradientBackgroundRed4 / 255.0f,
                            gradientBackgroundGreen4 / 255.0f,
                            gradientBackgroundBlue4 / 255.0f,
                            1.0f
                        )
                        begin(
                            !markAsInactive && backgroundMode == "Gradient",
                            gradientX,
                            gradientY,
                            gradientOffset
                        ).use {
                            RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                                drawRoundedRect(
                                    xPos - if (rectMode == "Right") 5 else 2,
                                    yPos,
                                    if (rectMode == "Right") -3F else 0F,
                                    yPos + textHeight,
                                    when (backgroundMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        else -> backgroundCustomColor
                                    },
                                    roundedBackgroundRadius
                                )
                            }
                        }
                    }

                    GradientFontShader.apply {
                        color1 = floatArrayOf(
                            gradientTextRed1 / 255.0f,
                            gradientTextGreen1 / 255.0f,
                            gradientTextBlue1 / 255.0f,
                            1.0f
                        )
                        color2 = floatArrayOf(
                            gradientTextRed2 / 255.0f,
                            gradientTextGreen2 / 255.0f,
                            gradientTextBlue2 / 255.0f,
                            1.0f
                        )
                        color3 = floatArrayOf(
                            gradientTextRed3 / 255.0f,
                            gradientTextGreen3 / 255.0f,
                            gradientTextBlue3 / 255.0f,
                            1.0f
                        )
                        color4 = floatArrayOf(
                            gradientTextRed4 / 255.0f,
                            gradientTextGreen4 / 255.0f,
                            gradientTextBlue4 / 255.0f,
                            1.0f
                        )

                        begin(
                            !markAsInactive && textColorMode == "Gradient",
                            gradientX,
                            gradientY,
                            gradientOffset
                        ).use {
                            RainbowFontShader.begin(
                                !markAsInactive && textColorMode == "Rainbow",
                                rainbowX,
                                rainbowY,
                                rainbowOffset
                            ).use {
                                font.drawString(
                                    displayString, xPos - if (rectMode == "Right") 3 else 0, yPos + textY,
                                    if (markAsInactive) inactiveColor
                                    else when (textColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        else -> textCustomColor
                                    },
                                    textShadow
                                )
                            }
                        }
                    }

                    GradientShader.apply {
                        color1 = floatArrayOf(
                            gradientRectRed1 / 255.0f,
                            gradientRectGreen1 / 255.0f,
                            gradientRectBlue1 / 255.0f,
                            1.0f
                        )
                        color2 = floatArrayOf(
                            gradientRectRed2 / 255.0f,
                            gradientRectGreen2 / 255.0f,
                            gradientRectBlue2 / 255.0f,
                            1.0f
                        )
                        color3 = floatArrayOf(
                            gradientRectRed3 / 255.0f,
                            gradientRectGreen3 / 255.0f,
                            gradientRectBlue3 / 255.0f,
                            1.0f
                        )
                        color4 = floatArrayOf(
                            gradientRectRed4 / 255.0f,
                            gradientRectGreen4 / 255.0f,
                            gradientRectBlue4 / 255.0f,
                            1.0f
                        )

                        begin(
                            !markAsInactive && rectColorMode == "Gradient",
                            gradientX,
                            gradientY,
                            gradientOffset
                        ).use {
                            if (rectMode != "None") {
                                RainbowShader.begin(
                                    !markAsInactive && rectColorMode == "Rainbow",
                                    rainbowX,
                                    rainbowY,
                                    rainbowOffset
                                ).use {
                                    val rectColor =
                                        if (markAsInactive) inactiveColor
                                        else when (rectColorMode) {
                                            "Gradient" -> 0
                                            "Rainbow" -> 0
                                            "Random" -> moduleColor
                                            else -> rectCustomColor
                                        }

                                    when (rectMode) {
                                        "Left" -> drawRoundedRect(
                                            xPos - 5,
                                            yPos + 0.8F,
                                            xPos - 2,
                                            yPos + textHeight,
                                            rectColor,
                                            roundedRectRadius
                                        )

                                        "Right" -> drawRoundedRect(
                                            -3F,
                                            yPos,
                                            0F,
                                            yPos + textHeight,
                                            rectColor,
                                            roundedRectRadius
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Horizontal.LEFT -> {
                    val width = font.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectMode == "Left") 5 else 2

                    GradientShader.apply {
                        color1 = floatArrayOf(
                            gradientBackgroundRed1 / 255.0f,
                            gradientBackgroundGreen1 / 255.0f,
                            gradientBackgroundBlue1 / 255.0f,
                            1.0f
                        )
                        color2 = floatArrayOf(
                            gradientBackgroundRed2 / 255.0f,
                            gradientBackgroundGreen2 / 255.0f,
                            gradientBackgroundBlue2 / 255.0f,
                            1.0f
                        )
                        color3 = floatArrayOf(
                            gradientBackgroundRed3 / 255.0f,
                            gradientBackgroundGreen3 / 255.0f,
                            gradientBackgroundBlue3 / 255.0f,
                            1.0f
                        )
                        color4 = floatArrayOf(
                            gradientBackgroundRed4 / 255.0f,
                            gradientBackgroundGreen4 / 255.0f,
                            gradientBackgroundBlue4 / 255.0f,
                            1.0f
                        )

                        begin(
                            !markAsInactive && backgroundMode == "Gradient",
                            gradientX,
                            gradientY,
                            gradientOffset
                        ).use {
                            RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                                drawRoundedRect(
                                    0F, yPos, xPos + width + if (rectMode == "Right") 5 else 2, yPos + textHeight,
                                    when (backgroundMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        else -> backgroundCustomColor
                                    },
                                    roundedBackgroundRadius
                                )
                            }
                        }
                    }

                    GradientFontShader.apply {
                        color1 = floatArrayOf(
                            gradientTextRed1 / 255.0f,
                            gradientTextGreen1 / 255.0f,
                            gradientTextBlue1 / 255.0f,
                            1.0f
                        )
                        color2 = floatArrayOf(
                            gradientTextRed2 / 255.0f,
                            gradientTextGreen2 / 255.0f,
                            gradientTextBlue2 / 255.0f,
                            1.0f
                        )
                        color3 = floatArrayOf(
                            gradientTextRed3 / 255.0f,
                            gradientTextGreen3 / 255.0f,
                            gradientTextBlue3 / 255.0f,
                            1.0f
                        )
                        color4 = floatArrayOf(
                            gradientTextRed4 / 255.0f,
                            gradientTextGreen4 / 255.0f,
                            gradientTextBlue4 / 255.0f,
                            1.0f
                        )

                        begin(
                            !markAsInactive && textColorMode == "Gradient",
                            gradientX,
                            gradientY,
                            gradientOffset
                        ).use {
                            RainbowFontShader.begin(
                                !markAsInactive && textColorMode == "Rainbow",
                                rainbowX,
                                rainbowY,
                                rainbowOffset
                            ).use {
                                font.drawString(
                                    displayString, xPos, yPos + textY,
                                    if (markAsInactive) inactiveColor
                                    else when (textColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        else -> textCustomColor
                                    },
                                    textShadow
                                )
                            }
                        }
                    }

                    GradientShader.apply {
                        color1 = floatArrayOf(
                            gradientRectRed1 / 255.0f,
                            gradientRectGreen1 / 255.0f,
                            gradientRectBlue1 / 255.0f,
                            1.0f
                        )
                        color2 = floatArrayOf(
                            gradientRectRed2 / 255.0f,
                            gradientRectGreen2 / 255.0f,
                            gradientRectBlue2 / 255.0f,
                            1.0f
                        )
                        color3 = floatArrayOf(
                            gradientRectRed3 / 255.0f,
                            gradientRectGreen3 / 255.0f,
                            gradientRectBlue3 / 255.0f,
                            1.0f
                        )
                        color4 = floatArrayOf(
                            gradientRectRed4 / 255.0f,
                            gradientRectGreen4 / 255.0f,
                            gradientRectBlue4 / 255.0f,
                            1.0f
                        )

                        begin(
                            !markAsInactive && rectColorMode == "Gradient",
                            gradientX,
                            gradientY,
                            gradientOffset
                        ).use {
                            RainbowShader.begin(
                                !markAsInactive && rectColorMode == "Rainbow",
                                rainbowX,
                                rainbowY,
                                rainbowOffset
                            ).use {
                                if (rectMode != "None") {
                                    val rectColor =
                                        if (markAsInactive) inactiveColor
                                        else when (rectColorMode) {
                                            "Gradient" -> 0
                                            "Rainbow" -> 0
                                            "Random" -> moduleColor
                                            else -> rectCustomColor
                                        }

                                    when (rectMode) {
                                        "Left" -> drawRoundedRect(
                                            0F,
                                            yPos - 1,
                                            3F,
                                            yPos + textHeight,
                                            rectColor,
                                            roundedRectRadius
                                        )

                                        "Right" -> drawRoundedRect(
                                            xPos + width + 2,
                                            yPos,
                                            xPos + width + 2 + 3,
                                            yPos + textHeight,
                                            rectColor,
                                            roundedRectRadius
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }

                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }

            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        glColor4f(1f, 1f, 1f, 1f)
        return null
    }

    override fun updateElement() {
        modules = moduleManager.modules
            .filter { it.inArray && it.slide > 0 }
            .sortedBy { -font.getStringWidth(getDisplayString(it)) }
    }
}
