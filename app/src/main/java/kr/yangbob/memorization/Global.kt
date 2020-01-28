package kr.yangbob.memorization

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kr.yangbob.memorization.alarm.CreateCalendarReceiver
import java.util.*

const val MILLIS_A_DAY = 24 * 60 * 60 * 1000

enum class Stage(val nextTest: Int) {
    INIT(1),
    BEGIN_ONE(1), BEGIN_TWO(1), BEGIN_THREE(3), AFTER_THREE(7),
    AFTER_WEEK(15), AFTER_HALF(30), AFTER_MONTH(30)
}

// Receiver 관련
const val TEST_CHK_RECV_ID = 10
fun cancelAlarm(context: Context){
    val pendingIntent = Intent(context, CreateCalendarReceiver::class.java).let {
        PendingIntent.getBroadcast(context, TEST_CHK_RECV_ID, it, PendingIntent.FLAG_NO_CREATE)
    }
    if(pendingIntent != null){
        Log.i("yangtest", "PendingIntent is Not Null -> Cancel Alarm!")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmMgr.cancel(pendingIntent)
    } else {
        Log.i("yangtest", "PendingIntent is Null -> Skip Cancel Alarm!")
    }
}
fun setTestChkAlarm(context: Context){
    val testChkIntent = Intent(context, CreateCalendarReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, TEST_CHK_RECV_ID, testChkIntent, PendingIntent.FLAG_NO_CREATE)
    if( pendingIntent == null ) {
        Log.i("yangtest", "PendingIntent is Null -> Set Alarm!")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime,
            AlarmManager.INTERVAL_DAY,
            PendingIntent.getBroadcast(context, TEST_CHK_RECV_ID, testChkIntent, 0)
        )
    } else {
        Log.i("yangtest", "PendingIntent is Not Null -> Skip Set Alarm!")
    }
}