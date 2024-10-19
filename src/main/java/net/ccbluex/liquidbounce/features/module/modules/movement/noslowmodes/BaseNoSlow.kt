package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.utils.ClassUtils.getRawValues
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue

open class BaseNoSlow(unsortedModes: Array<NoSlowMode>) {
    private val modes: List<NoSlowMode> = unsortedModes.sortedBy { it.modeName }

    private val modeValue = ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla")
    val mode get() = modes.find { it.modeName == modeValue.get() }!!
    val onlyMove by BooleanValue("OnlyMove", false) { mode.allowNoMove }
    val forwardMultiplier by FloatValue("ForwardMultiplier", 1f, 0.2f..1f)
    val strafeMultiplier by FloatValue("StrafeMultiplier", 1f, 0.2f..1f)

    @Suppress("LeakingThis")
    val values = getRawValues(this, BaseNoSlow::class.java).toMutableList().also {
        for (mode in modes) {
            it.addAll(1, mode.values.onEach { value ->
                value.isSupported += { mode == this.mode }
                value.name = "${mode.modeName}-${value.name}"
            })
        }
    }.distinctBy { it.name }
}
