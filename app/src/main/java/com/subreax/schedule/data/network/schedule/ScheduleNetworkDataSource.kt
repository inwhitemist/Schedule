package com.subreax.schedule.data.network.schedule

import com.subreax.schedule.data.network.model.NetworkSchedule
import com.subreax.schedule.utils.Resource
import java.util.Date

interface ScheduleNetworkDataSource {
    suspend fun getSchedule(id: String, from: Date): Resource<NetworkSchedule>
}
