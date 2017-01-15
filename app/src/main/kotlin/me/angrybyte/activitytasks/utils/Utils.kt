package me.angrybyte.activitytasks.utils

import android.app.Activity
import android.os.Build
import android.text.Html
import android.widget.TextView
import java.util.*

/**
 * A few simple utilities.
 */
object Utils {

    /**
     * Returns the all-caps variant of the hex string generated from the given Int.
     */
    fun toHex(int: Int) = Integer.toHexString(int).toUpperCase(Locale.getDefault())

    /**
     * Tries to parse the HEX String into an Int.
     */
    @Throws(NumberFormatException::class)
    fun parseHex(number: String) = Integer.parseInt(number.replace("0x", "").replace("#", ""), 16)

}

/**
 * Tries to find a specific String value in the current Intent using the given [needle] key.
 * If not found (or Intent is `null`), this function returns the [alternative] String provided.
 */
fun Activity.findInIntent(needle: String, alternative: String) = intent?.getStringExtra(needle) ?: alternative

/**
 * Marks all of the given text using the given [colors] by converting the whole text to HTML.
 */
fun TextView.markText(strings: Array<String>, colors: IntArray) {
    if (strings.size != colors.size) {
        throw RuntimeException("String and Color array must match in size")
    }

    // strings may be duplicate, keep track of that to avoid replacing HTML
    val replaced = HashSet<String>()
    var workingText = text.toString()
    for (i in strings.indices) {
        if (replaced.contains(strings[i])) {
            continue
        }
        val replacement = "<font color=\"#${Utils.toHex(colors[i])}\"><b>${strings[i]}</b></font>"
        workingText = workingText.replace(strings[i], replacement)
        replaced.add(strings[i])
    }

    setHtml(workingText.replace("\n", "<br>"))
}

/**
 * Sets the HTML text on this TextView, using the appropriate API level.
 */
fun TextView.setHtml(html: String) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION") // this was better btw
        text = Html.fromHtml(html)
    }
}

/**
 * Returns the String (hex) representation of the activity's ID (generated using [Any.hashCode]).
 */
fun Any.getId() = Utils.toHex(hashCode())