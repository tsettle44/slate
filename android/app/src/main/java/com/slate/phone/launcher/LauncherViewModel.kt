package com.slate.phone.launcher

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slate.phone.admin.DeviceOwnerManager
import com.slate.phone.apps.AppResolver
import com.slate.phone.launcher.ui.currentZonedDateTime
import com.slate.phone.policy.PolicyEnforcer
import com.slate.phone.policy.PolicyRepository
import com.slate.phone.policy.SlateApp
import com.slate.phone.policy.SlatePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LauncherEvent {
    data class LaunchApp(val intent: Intent) : LauncherEvent
    data class ShowBlocked(val packageName: String) : LauncherEvent
    data class NavigateToDelay(val app: SlateApp, val delaySeconds: Int) : LauncherEvent
}

@HiltViewModel
class LauncherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val policyRepository: PolicyRepository,
    private val policyEnforcer: PolicyEnforcer,
    private val deviceOwnerManager: DeviceOwnerManager,
    private val appResolver: AppResolver,
) : ViewModel() {
    private val clock = MutableStateFlow(currentZonedDateTime())
    private val policy = MutableStateFlow(SlatePolicy())
    private val isDeviceOwner = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LauncherEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<LauncherEvent> = _events.asSharedFlow()

    private var clockJob: Job? = null

    init {
        viewModelScope.launch {
            policyRepository.policyFlow.collect { loaded ->
                policy.value = loaded
                refreshApps(loaded)
            }
        }

        viewModelScope.launch {
            combine(policy, clock, isDeviceOwner) { currentPolicy, time, owner ->
                LauncherUiState(
                    policy = currentPolicy,
                    tier1Apps = appResolver.tier1Apps(currentPolicy),
                    allApps = appResolver.resolveAllowlistedApps(currentPolicy),
                    isDeviceOwner = owner,
                    currentTime = time,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        startClock()
    }

    fun onLauncherResumed() {
        isDeviceOwner.value = deviceOwnerManager.isDeviceOwner()
        viewModelScope.launch {
            if (deviceOwnerManager.isDeviceOwner()) {
                deviceOwnerManager.initializeDeviceOwner()
                policyEnforcer.applyCurrentPolicy()
            }
        }
    }

    fun onAppSelected(app: SlateApp) {
        viewModelScope.launch {
            val currentPolicy = policyRepository.getPolicy()
            if (!policyRepository.isAllowed(app.packageName, currentPolicy)) {
                _events.emit(LauncherEvent.ShowBlocked(app.packageName))
                return@launch
            }

            val delaySeconds = policyRepository.getDelaySeconds(app, currentPolicy)
            if (delaySeconds > 0) {
                _events.emit(LauncherEvent.NavigateToDelay(app, delaySeconds))
            } else {
                _events.emit(LauncherEvent.LaunchApp(app.launchIntent))
            }
        }
    }

    fun onDelayComplete(app: SlateApp) {
        viewModelScope.launch {
            _events.emit(LauncherEvent.LaunchApp(app.launchIntent))
        }
    }

    fun showBlockedFor(packageName: String) {
        viewModelScope.launch {
            _events.emit(LauncherEvent.ShowBlocked(packageName))
        }
    }

    private fun refreshApps(currentPolicy: SlatePolicy) {
        _uiState.update {
            it.copy(
                tier1Apps = appResolver.tier1Apps(currentPolicy),
                allApps = appResolver.resolveAllowlistedApps(currentPolicy),
            )
        }
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (true) {
                clock.value = currentZonedDateTime()
                delay(1_000)
            }
        }
    }
}
