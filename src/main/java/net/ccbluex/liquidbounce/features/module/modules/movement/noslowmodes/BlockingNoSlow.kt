package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.aac.AAC
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.aac.AAC2
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.NCP
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.NewNCP
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP2
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.watchdog.WatchDog

object BlockingNoSlow : BaseNoSlow(
    arrayOf(
        Vanilla,
        SwitchItem(),
        OldIntave,
        NCP,
        NewNCP,
        UNCP,
        UNCP2,
        AAC,
        AAC2,
        Place(),
        EmptyPlace(),
        WatchDog,
        Medusa,
        Drop(),
        Grim2365,
        InvalidC08,
    )
)
