package me.angrybyte.activitytasks.activities

import me.angrybyte.activitytasks.R

class SingleInstanceActivity : GenericTaskActivity() {

    override fun getActivityTitle() = getString(R.string.act_single_instance)!!

}
