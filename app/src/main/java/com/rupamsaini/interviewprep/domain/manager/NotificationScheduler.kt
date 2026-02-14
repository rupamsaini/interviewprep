package com.rupamsaini.interviewprep.domain.manager

interface NotificationScheduler {
    fun scheduleDailyNotification(hour: Int, minute: Int)
    fun cancelDailyNotification()
}
