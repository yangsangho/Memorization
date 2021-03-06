package kr.yangbob.memorization

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kr.yangbob.memorization.data.SimpleDate
import java.util.*

const val ANIMATION_HALF_TIME: Long = 400
const val ANIMATION_FULL_TIME: Long = 800
const val CREATE_CAL_RECV_ID = 10
const val PUSH_ALARM_RECV_ID = 11
const val IN_APP_UPDATE_RECV_ID = 13

const val EXTRA_TO_RESULT_DATESTR = "dateStr"
const val EXTRA_TO_QST_ID = "qstID"
const val EXTRA_TO_TUTORIAL = "tutorial"
const val NOTIFICATION_CHANNEL_ID = "noti_channel_id"
const val NOTIFICATION_CHANNEL_NAME = "noti_channel_name"

const val SETTING_ENTIRE_ACTIVITY_SORT_ITEM = "entireSortItem"
const val SETTING_ENTIRE_ACTIVITY_SORT_ORDER = "entireSortOrder"
const val SETTING_RESULT_ACTIVITY_SORT_ITEM = "resultSortItem"
const val SETTING_RESULT_ACTIVITY_SORT_ORDER = "resultSortOrder"
const val SETTING_IS_FIRST_MAIN = "firstMain"
const val SETTING_IS_FIRST_ENTIRE = "firstEntire"
const val SETTING_IS_FIRST_TODAY = "firstToday"
const val SETTING_IS_FIRST_TEST = "firstTest"
const val SETTING_IS_FIRST_CREATE = "firstCreate"

//val todayDate = SimpleDate(Calendar.getInstance())
val todayDate = SimpleDate.newInstanceToday()
val tomorrowDate = todayDate.clone().apply {
    addDate(Calendar.DAY_OF_MONTH, 1)
}

val STAGE_LIST = Stage.values()

enum class Stage(val nextTest: Int) {
    NEW(1),
    BEGIN_ONE(1), BEGIN_TWO(1), BEGIN_THREE(3), AFTER_THREE(7),
    AFTER_WEEK(15), AFTER_HALF(30), AFTER_MONTH(30), REVIEW(45)
}

data class SortInfo(
    var sortedItemIdx: Int,
    var isAscending: Boolean
)

//// Receiver 관련
//fun cancelAlarm(context: Context) {
//    val pendingIntent = Intent(context, CreateCalendarReceiver::class.java).let {
//        PendingIntent.getBroadcast(context, CREATE_CAL_RECV_ID, it, PendingIntent.FLAG_NO_CREATE)
//    }
//    if (pendingIntent != null) {
//        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmMgr.cancel(pendingIntent)
//    }
//}

fun setTimer(context: Context) {
    val createCalIntent = Intent(context, CreateCalendarReceiver::class.java)
    val pushAlarmIntent = Intent(context, PushAlarmReceiver::class.java)
    val ccPendingIntent = PendingIntent.getBroadcast(
        context,
        CREATE_CAL_RECV_ID,
        createCalIntent,
        PendingIntent.FLAG_NO_CREATE
    )
    val paPendingIntent = PendingIntent.getBroadcast(
        context,
        PUSH_ALARM_RECV_ID,
        pushAlarmIntent,
        PendingIntent.FLAG_NO_CREATE
    )

    if(paPendingIntent == null){
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime,
            AlarmManager.INTERVAL_HALF_DAY,
            PendingIntent.getBroadcast(context, PUSH_ALARM_RECV_ID, pushAlarmIntent, 0)
        )
    }

    if (ccPendingIntent == null) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime,
            AlarmManager.INTERVAL_DAY,
            PendingIntent.getBroadcast(context, CREATE_CAL_RECV_ID, createCalIntent, 0)
        )
    }
}