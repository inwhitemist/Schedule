package com.subreax.schedule.data.repository.schedule_id.tsu

import com.subreax.schedule.data.model.ScheduleId
import com.subreax.schedule.data.model.ScheduleType
import com.subreax.schedule.data.network.RetrofitService
import com.subreax.schedule.data.network.model.RetrofitDictionaryItem
import com.subreax.schedule.data.repository.schedule_id.ScheduleIdRepository
import com.subreax.schedule.utils.Resource
import com.subreax.schedule.utils.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import javax.inject.Inject

class TsuScheduleIdRepository @Inject constructor(
    private val service: RetrofitService
) : ScheduleIdRepository {
    override suspend fun getScheduleId(id: String): Resource<ScheduleId> {
        return withContext(Dispatchers.IO) {
            val datesRes = handleExceptions {
                service.getDates(id)
            }
            if (datesRes is Resource.Failure) {
                return@withContext Resource.Failure(datesRes.message)
            }

            val dates = datesRes.requireValue()
            if (dates.error == null) {
                return@withContext Resource.Success(
                    ScheduleId(
                        value = id,
                        type = parseScheduleType(dates.scheduleType)
                    )
                )
            }

            // if dates.error != null, schedule id still could exist
            isScheduleIdExist(id).ifSuccess { exists ->
                if (exists) {
                    Resource.Success(asUnknownScheduleId(id))
                } else {
                    Resource.Failure(UiText.hardcoded("id расписания '$id' не существует"))
                }
            }
        }
    }

    override suspend fun getScheduleIds(startsWith: String): Resource<List<ScheduleId>> {
        return withContext(Dispatchers.IO) {
            handleExceptions {
                service.getDictionaries(startsWith)
                    .map(::asUnknownScheduleId)
            }
        }
    }

    override suspend fun isScheduleIdExist(id: String): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            getScheduleIds(id).ifSuccess { ids ->
                val firstId = ids.firstOrNull()?.value
                Resource.Success(id == firstId)
            }
        }
    }

    private fun parseScheduleType(raw: String): ScheduleType {
        return when (raw) {
            "GROUP_P" -> ScheduleType.Student
            "PREP" -> ScheduleType.Teacher
            "AUD" -> ScheduleType.Room
            else -> ScheduleType.Unknown
        }
    }

    private inline fun <T> handleExceptions(block: () -> T): Resource<T> {
        return try {
            Resource.Success(block())
        } catch (ex: IOException) {
            Resource.Failure(UiText.hardcoded("Сетевая ошибка"))
        } catch (ex: Exception) {
            Resource.Failure(UiText.hardcoded(ex.message ?: "Unknown error"))
        }
    }

    private fun asUnknownScheduleId(id: String): ScheduleId {
        return ScheduleId(id, ScheduleType.Unknown)
    }

    private fun asUnknownScheduleId(item: RetrofitDictionaryItem): ScheduleId {
        return ScheduleId(item.value, ScheduleType.Unknown)
    }
}