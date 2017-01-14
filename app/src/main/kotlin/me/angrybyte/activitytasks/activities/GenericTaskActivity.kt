package me.angrybyte.activitytasks.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_layout.*
import me.angrybyte.activitytasks.R

abstract class GenericTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

        descriptionText.text = getToolbarTitle()
    }

    abstract fun getToolbarTitle(): String

}