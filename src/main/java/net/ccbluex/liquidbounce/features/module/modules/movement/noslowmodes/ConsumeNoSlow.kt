package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.ncp.UNCP2
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other.*

object ConsumeNoSlow : BaseNoSlow(
    arrayOf(
        Vanilla,
        SwitchItem(),
        OldIntave,
        UNCP,
        UNCP2,
        Place(),
        EmptyPlace(),
        Medusa,
        Drop(),
        Grim2365,
    )
)
