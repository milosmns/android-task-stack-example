package me.angrybyte.activitytasks.utils

import android.app.Activity
import java.util.*

/**
 * A few simple utilities.
 */
object Utils {

    /**
     * Returns the all-caps variant of the hex string generated from the given Int.
     */
    fun hexCaps(int: Int) = Integer.toHexString(int).toUpperCase(Locale.getDefault())

}

/**
 * Tries to find a specific String value in the current Intent using the given [needle] key.
 * If not found (or Intent is `null`), this function returns the [alternative] String provided.
 */
fun Activity.findInIntent(needle: String, alternative: String) = intent?.getStringExtra(needle) ?: alternative

/**
 * Returns the String representation of the activity's ID (generated using [hashCode]).
 */
fun Any.getId() = Utils.hexCaps(hashCode())