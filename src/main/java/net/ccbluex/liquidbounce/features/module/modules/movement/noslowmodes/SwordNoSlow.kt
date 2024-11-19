package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.utils.ClassUtils.getAllClasses

object SwordNoSlow : BaseNoSlow(
    SwordNoSlow::class.java.`package`.getAllClasses<NoSlowMode>()
        .map(Class<NoSlowMode>::newInstance)
        .filter(NoSlowMode::allowSword)
        .toTypedArray()
)
