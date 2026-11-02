package com.triangles.imoves.base.state

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triangles.domain.core.error.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Effect, Event>(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────
    protected val _uiState = MutableStateFlow(initialState(savedStateHandle))
    open val uiState: StateFlow<State> = _uiState.asStateFlow()

    protected val currentState: State get() = _uiState.value

    protected abstract fun initialState(savedStateHandle: SavedStateHandle): State
    protected abstract fun reduceEvent(event: Event)

    fun onEvent(event: Event) = reduceEvent(event)

    protected fun updateState(reducer: State.() -> State) = _uiState.update(reducer)

    // ── Full-screen state (Content = idle) ────────────────────────────────
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Content)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    protected fun setLoading() { _screenState.value = ScreenState.Loading }
    protected fun setIdle() { _screenState.value = ScreenState.Content }
    protected fun setError(error: AppError) { _screenState.value = ScreenState.Error(error) }

    // ── Overlay loading ───────────────────────────────────────────────────
    private val _overlayLoading = MutableStateFlow(false)
    val overlayLoading: StateFlow<Boolean> = _overlayLoading.asStateFlow()

    protected fun showOverlayLoading() { _overlayLoading.value = true }
    protected fun hideOverlayLoading() { _overlayLoading.value = false }

    // ── One-shot events (buffered, so none are lost while the UI is stopped) ──
    private val _overlayEvents = Channel<UiOverlayEvent>(Channel.BUFFERED)
    val overlayEvents: Flow<UiOverlayEvent> = _overlayEvents.receiveAsFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effects.receiveAsFlow()

    protected fun sendEffect(effect: Effect) { _effects.trySend(effect) }

    protected fun snackBarEffect(
        message: UiText,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        _overlayEvents.trySend(UiOverlayEvent.SnackBar(message, actionLabel, duration))
    }

    protected fun toastEffect(
        message: UiText,
        duration: ToastDuration = ToastDuration.Short,
    ) {
        _overlayEvents.trySend(UiOverlayEvent.Toast(message, duration))
    }

    protected fun dialogEffect(
        title: UiText,
        message: UiText,
        positiveText: Int = android.R.string.ok,
        negativeText: Int? = android.R.string.cancel,
        cancelable: Boolean = true,
    ) {
        _overlayEvents.trySend(
            UiOverlayEvent.Dialog(title, message, positiveText, negativeText, cancelable)
        )
    }

    protected fun dismissDialog() {
        _overlayEvents.trySend(UiOverlayEvent.DismissDialog)
    }

    // ── Running use cases ─────────────────────────────────────────────────

    /**
     * Runs [call] and applies the standard UI handling:
     * - [fullScreen]: loading screen, then content on success or an error screen on failure.
     * - otherwise a failure is shown as a snackbar.
     * - [overlay]: blocking overlay loader for the duration of the call.
     *
     * [onError] runs in addition to that default handling, for extra side effects.
     */
    protected fun <T> execute(
        fullScreen: Boolean = false,
        overlay: Boolean = false,
        onError: (AppError) -> Unit = {},
        onSuccess: suspend (T) -> Unit = {},
        call: suspend () -> Result<T>,
    ): Job = viewModelScope.launch {
        if (fullScreen) setLoading()
        if (overlay) showOverlayLoading()
        try {
            call().fold(
                onSuccess = { data ->
                    if (fullScreen) setIdle()
                    onSuccess(data)
                },
                onFailure = { throwable ->
                    if (throwable is CancellationException) throw throwable
                    handleFailure(throwable.asAppError(), fullScreen, onError)
                },
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            handleFailure(exception.asAppError(), fullScreen, onError)
        } finally {
            if (overlay) hideOverlayLoading()
        }
    }

    private fun handleFailure(
        error: AppError,
        fullScreen: Boolean,
        onError: (AppError) -> Unit,
    ) {
        if (fullScreen) setError(error) else snackBarEffect(error.toUiText())
        onError(error)
    }
}
