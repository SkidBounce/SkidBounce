package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.utils.ClassUtils.getAllClasses

object DrinkNoSlow : BaseNoSlow(
    DrinkNoSlow::class.java.`package`.getAllClasses<NoSlowMode>()
        .map(Class<NoSlowMode>::newInstance)
        .filter(NoSlowMode::allowDrink)
        .toTypedArray()
)
