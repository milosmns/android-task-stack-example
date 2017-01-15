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
import me.angrybyte.activitytasks.utils.Utils
import me.angrybyte.activitytasks.utils.findInIntent
import me.angrybyte.activitytasks.utils.getId

/**
 * Superclass of all app's activities. Displays everything that's needed on the UI,
 * overriding activities only need to override stuff to provide detailed info about themselves.
 */
abstract class GenericTaskActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_TASKS = 1
    private val KEY_ORIGIN_NAME = "KEY_ORIGIN_NAME"
    private val KEY_ORIGIN_ID = "KEY_ORIGIN_ID"
    private val KEY_ORIGIN_TASK_ID = "KEY_ORIGIN_TASK_ID"

    /* Activity stuff */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

        @Suppress("DEPRECATION")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_TASKS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.GET_TASKS), PERMISSION_REQUEST_TASKS)
        } else {
            setupActivity()
        }
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
        // update details from intent and task info
        descriptionText.text = getString(
                R.string.description_template,
                getActivityTitle(), getId(), Utils.hexCaps(taskId), getOriginActivity(), getOriginActivityId(), getOriginTaskId()
        )
        stackInfo.text = getTaskStack()

        // assign click listeners for buttons so that they open the correct activities
        val classes = listOf(DefaultActivity::class.java, SingleTaskActivity::class.java, SingleTopActivity::class.java, SingleInstanceActivity::class.java)
        listOf(start_default, start_single_task, start_single_top, start_single_instance).forEachIndexed {
            i, iView ->
            iView.setOnClickListener {
                val intent = Intent(GenericTaskActivity@ this, classes[i])
                intent.putExtra(KEY_ORIGIN_NAME, GenericTaskActivity@ this.javaClass.simpleName!!)
                intent.putExtra(KEY_ORIGIN_ID, getId())
                intent.putExtra(KEY_ORIGIN_TASK_ID, Utils.hexCaps(taskId))
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
     * Tries to find the current task stack, if possible.
     */
    @Suppress("DEPRECATION")
    private fun getTaskStack(): String {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val taskList = manager.getRunningTasks(10) // this may not work on Lollipop...

        if (taskList.isEmpty()) {
            return getString(R.string.stack_unknown)
        }

        val builder = StringBuilder(taskList.size)
        taskList.forEach {
            // Task @%1$s \"%2$s\", Base: %3$s, Top: %4$s, Total/Running: %5$d/%6$d
            val taskId = Utils.hexCaps(it.id)
            val baseName = it.baseActivity.shortClassName
            val topName = it.topActivity.shortClassName
            builder.append("-")
            builder.append(getString(R.string.stack_item_template, taskId, baseName, topName, it.numActivities, it.numRunning))
            builder.append("\n\n")
        }
        return builder.toString()
    }

    /* Override these in sub-activities */

    /**
     * Gets the unique activity title from the implementation class.
     */
    protected abstract fun getActivityTitle(): String

}