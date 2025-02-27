package com.subreax.schedule.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subreax.schedule.data.model.ScheduleBookmark
import com.subreax.schedule.data.model.ScheduleId
import com.subreax.schedule.data.model.ScheduleType
import com.subreax.schedule.data.repository.bookmark.BookmarkRepository
import com.subreax.schedule.data.repository.schedule.ScheduleRepository
import com.subreax.schedule.ui.GetScheduleUseCase
import com.subreax.schedule.ui.UiLoadingState
import com.subreax.schedule.ui.UiSubjectDetails
import com.subreax.schedule.utils.Resource
import com.subreax.schedule.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    private val scheduleRepository: ScheduleRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    private val getScheduleUseCase = GetScheduleUseCase(scheduleRepository, bookmarkRepository, appContext, viewModelScope)
    val renameSubjectUseCase = RenameSubjectUseCase(scheduleRepository)

    val bookmarks = bookmarkRepository.bookmarks

    private val _selectedBookmark = MutableStateFlow(
        ScheduleBookmark(
            ScheduleId("", ScheduleType.Student),
            ScheduleBookmark.NO_NAME
        )
    )
    val selectedBookmark = _selectedBookmark.asStateFlow()

    val schedule = getScheduleUseCase.schedule
    val loadingState = getScheduleUseCase.loadingState

    private val _pickedSubject = MutableStateFlow<UiSubjectDetails?>(null)
    val pickedSubject = _pickedSubject.asStateFlow()

    val errors = Channel<UiText>()

    init {
        viewModelScope.launch {
            val owners = bookmarks.first { it.isNotEmpty() }
            if (owners.isNotEmpty()) {
                getSchedule(owners.first().scheduleId.value)
            } else {
                errors.send(UiText.hardcoded("Вы не сохранили ни одного расписания"))
            }
        }

        viewModelScope.launch {
            loadingState
                .filter { it is UiLoadingState.Error }
                .collect {
                    errors.send((it as UiLoadingState.Error).message)
                }
        }
    }

    fun getSchedule(ownerNetworkId: String) {
        viewModelScope.launch {
            _selectedBookmark.value = bookmarkRepository.getBookmark(ownerNetworkId).requireValue()
        }

        viewModelScope.launch {
            getScheduleUseCase.init(ownerNetworkId)
        }
    }

    fun openSubjectDetails(subjectId: Long) {
        viewModelScope.launch {
            when (val res = getScheduleUseCase.getSubjectDetails(subjectId)) {
                is Resource.Success -> _pickedSubject.value = res.value
                is Resource.Failure -> {
                    hideSubjectDetails()
                    errors.send(res.message)
                }
            }
        }
    }

    fun hideSubjectDetails() {
        _pickedSubject.value = null
    }

    fun startRenaming(subjectId: Long) {
        viewModelScope.launch {
            val subject = scheduleRepository.getSubjectById(subjectId).requireValue()
            renameSubjectUseCase.startRenaming(subject.name, subject.nameAlias)
        }
    }

    fun finishRenaming() {
        viewModelScope.launch {
            renameSubjectUseCase.finishRenaming()
            getScheduleUseCase.refresh().join()
            _pickedSubject.value?.let {
                openSubjectDetails(it.subjectId)
            }
        }
    }

    fun cancelRenaming() {
        renameSubjectUseCase.cancelRenaming()
    }
}
