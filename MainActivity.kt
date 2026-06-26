package com.ui.randomalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar
import kotlin.random.Random
import android.os.CountDownTimer
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Handler
import android.os.Looper
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent

class MainActivity : ComponentActivity()
{
    companion object
    {
        var startTimerFromNotification = false
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        startTimerFromNotification =
            intent.getBooleanExtra(
                "START_TIMER",
                false
            )

        setContent {
            SummonScreen()
        }
    }
}

@Composable
fun SummonScreen()
{

    var startTime by remember { mutableStateOf("13:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var result by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(1500L) } // 25分
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit)
    {
        if (
            MainActivity.startTimerFromNotification &&
            !timerRunning
        )
        {
            timerRunning = true

            object : CountDownTimer(1500_000, 1000)
            {
                override fun onTick(
                    millisUntilFinished: Long
                )
                {
                    timeLeft =
                        millisUntilFinished / 1000
                }

                override fun onFinish()
                {
                    result = "作業完了！"
                    timerRunning = false
                }
            }.start()

            MainActivity.startTimerFromNotification =
                false
        }
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {

        Text("ランダム召集アプリ")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("開始時刻") },
                    enabled = !timerRunning
        )


        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("終了時刻") },
            enabled = !timerRunning
            )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = !timerRunning,
            onClick =
                {
                    val (sh, sm) = parseTime(startTime)
                val (eh, em) = parseTime(endTime)

                val randomTime =
                    getRandomTimeInRange(
                        sh,
                        sm,
                        eh,
                        em
                    )

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = randomTime

                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    result =
                        "召集時刻: %02d:%02d".format(hour, minute)
                    timerRunning = true
//
//                    object : CountDownTimer(15_000, 1000) {
//
//                        override fun onTick(millisUntilFinished: Long) {
//                            timeLeft = millisUntilFinished / 1000
//                        }
//
//                        override fun onFinish() {
//                            result = "作業完了！"
//                            timerRunning = false
//                        }
//
//                    }.start()
                    scheduleAlarm(
                        context,
                        randomTime
                    )
            }
        )
        {
            Text("作業時間")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(result)
        if (timerRunning)
        {
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60

            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
fun parseTime(text: String): Pair<Int, Int>
{
    val parts = text.split(":")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    return hour to minute
}


fun getRandomTimeInRange(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int
): Long
{
    val start = Calendar.getInstance().apply{
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, startMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val end = Calendar.getInstance().apply{
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, endMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val startMillis = start.timeInMillis
    val endMillis = end.timeInMillis

    return startMillis +
            Random.nextLong(endMillis - startMillis)

}
@SuppressLint("MissingPermission")
fun showNotification(context: Context)
{
    val channelId = "summon_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
        val channel = NotificationChannel(
            channelId,
            "召集通知",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }

    val notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("召集命令")
            .setContentText("作業を開始してください")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

    NotificationManagerCompat.from(context)
        .notify(1, notification)
}
fun scheduleAlarm(
    context: Context,
    triggerTime: Long
)
{
    val intent =
        Intent(
            context,
            AlarmReceiver::class.java
        )

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        triggerTime,
        pendingIntent
    )
}