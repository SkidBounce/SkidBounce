/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.ui.font

import org.lwjgl.opengl.GL11.glDeleteLists

data class CachedFont(val displayList: Int, var lastUsage: Long, var deleted: Boolean = false) {
    protected fun finalize() {
        if (!deleted) {
            glDeleteLists(displayList, 1)
        }
    }
}
