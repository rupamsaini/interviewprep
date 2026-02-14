package com.rupamsaini.interviewprep.di

import com.rupamsaini.interviewprep.data.manager.WorkManagerNotificationScheduler
import com.rupamsaini.interviewprep.domain.manager.NotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {

    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(
        impl: WorkManagerNotificationScheduler
    ): NotificationScheduler
}
