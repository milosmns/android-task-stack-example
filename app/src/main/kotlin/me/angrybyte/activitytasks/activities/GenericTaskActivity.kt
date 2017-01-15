package me.angrybyte.activitytasks.activities

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_layout.*
import me.angrybyte.activitytasks.R
import me.angrybyte.activitytasks.utils.*

/**
 * Superclass of all app's activities. Displays everything that's needed on the UI,
 * overriding activities only need to override stuff to provide detailed info about themselves.
 */
abstract class GenericTaskActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_TASKS = 1
    private val KEY_ORIGIN_NAME = "KEY_ORIGIN_NAME"
    private val KEY_ORIGIN_ID = "KEY_ORIGIN_ID"
    private val KEY_ORIGIN_TASK_ID = "KEY_ORIGIN_TASK_ID"

    private var reInit: Boolean = false

    /* Activity stuff */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

        @Suppress("DEPRECATION") // not true, this is a valid KitKat permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_TASKS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.GET_TASKS), PERMISSION_REQUEST_TASKS)
        } else {
            setupActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        if (reInit) setupActivity()
    }

    override fun onStop() {
        super.onStop()
        reInit = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_TASKS -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                } else {
                    setupActivity()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        intent?.let {
            outState.putString(KEY_ORIGIN_NAME, intent.getStringExtra(KEY_ORIGIN_NAME))
            outState.putString(KEY_ORIGIN_ID, intent.getStringExtra(KEY_ORIGIN_ID))
            outState.putString(KEY_ORIGIN_TASK_ID, intent.getStringExtra(KEY_ORIGIN_TASK_ID))
        }
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setupActivity()
    }

    /**
     * Initializes all views.
     */
    private fun setupActivity() {
        // update stack details from the current intent and task info
        stackInfo.setHtml(getTaskStackHtml())

        // update current activity info
        val thisActTitle = getActivityTitle() // template already has 'Activity'
        val thisActHash = "#${getId()}"
        val thisTaskHash = "#${Utils.toHex(taskId)}"
        val originActName = getOriginActivity().replace("Activity", "")
        val originActHash = "#${getOriginActivityId()}"
        val originTaskHash = "#${getOriginTaskId()}"
        descriptionText.text = getString(R.string.description_template,
                thisActTitle, thisActHash, thisTaskHash, originActName, originActHash, originTaskHash
        )

        // calculate colors and replace plain IDs with colored IDs
        val launcherHash = "#${getString(R.string.activity_name_launcher)[0]}"
        val originActColor = if (launcherHash == originActHash) 0xCCCCCC else Utils.parseHex(originActHash)
        val originTaskColor = if (launcherHash == originTaskHash) 0xCCCCCC else Utils.parseHex(originTaskHash)
        val replacements = arrayOf(thisActHash, thisTaskHash, originActHash, originTaskHash)
        val colors = intArrayOf(hashCode(), taskId, originActColor, originTaskColor)
        descriptionText.markText(replacements, colors)

        // assign click listeners for buttons so that they open the correct activities
        val classes = listOf(DefaultActivity::class.java, SingleTaskActivity::class.java, SingleTopActivity::class.java, SingleInstanceActivity::class.java)
        listOf(start_default, start_single_task, start_single_top, start_single_instance).forEachIndexed {
            i, iView ->
            iView.setOnClickListener {
                val intent = Intent(GenericTaskActivity@ this, classes[i])
                intent.putExtra(KEY_ORIGIN_NAME, GenericTaskActivity@ this.javaClass.simpleName!!)
                intent.putExtra(KEY_ORIGIN_ID, getId())
                intent.putExtra(KEY_ORIGIN_TASK_ID, Utils.toHex(taskId))
                startActivity(intent)
            }
        }
    }

    /* Important operations */

    /**
     * Searches through the current Intent to find the origin Activity name, if supplied.
     */
    private fun getOriginActivity() = findInIntent(KEY_ORIGIN_NAME, getString(R.string.activity_name_launcher))

    /**
     * Searches through the current Intent to find the origin Activity ID, if supplied.
     */
    private fun getOriginActivityId() = findInIntent(KEY_ORIGIN_ID, getString(R.string.activity_name_launcher)[0].toString())

    /**
     * Searches through the current Intent to find the origin Task ID, if supplied.
     */
    private fun getOriginTaskId() = findInIntent(KEY_ORIGIN_TASK_ID, getString(R.string.activity_name_launcher)[0].toString())

    /**
     * Tries to find the current task stack, if possible. Outputs plain HTML text.
     */
    private fun getTaskStackHtml(): String {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION") // valid call, even with Lollipop limitations
        val taskList = manager.getRunningTasks(10) // this may not work on Lollipop...

        if (taskList.isEmpty()) {
            return getString(R.string.stack_unknown)
        }

        val builder = StringBuilder(taskList.size)
        for ((i, task) in taskList.withIndex()) {
            val colorHash = Utils.toHex(task.id)
            val taskId = "<font color=\"#$colorHash\"><b>#$colorHash</b></font>"
            val baseName = task.baseActivity.shortClassName
            val topName = task.topActivity.shortClassName
            val taskDescription = getString(R.string.stack_item_template, taskId, baseName, topName, task.numActivities, task.numRunning)
            builder.append("-")
            builder.append(taskDescription.replace("\n", "<br>"))
            // add new lines only on middle tasks
            if (i < taskList.size - 1) {
                builder.append("<br><br>")
            }
        }
        return builder.toString()
    }

    /* Override these in sub-activities */

    /**
     * Gets the unique activity title from the implementation class.
     */
    protected abstract fun getActivityTitle(): String

}