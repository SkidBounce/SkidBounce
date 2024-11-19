package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.utils.ClassUtils.getAllClasses

object BowNoSlow : BaseNoSlow(
    BowNoSlow::class.java.`package`.getAllClasses<NoSlowMode>()
        .map(Class<NoSlowMode>::newInstance)
        .filter(NoSlowMode::allowBow)
        .toTypedArray()
)
