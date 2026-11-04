package com.triangles.imoves.base.state

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.triangles.domain.core.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun <State, Effect, Event> BaseScreen(
    viewModel: BaseViewModel<State, Effect, Event>,
    onEffect: (Effect) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    loadingContent: @Composable BoxScope.() -> Unit = { CircularProgressIndicator() },
    errorContent: @Composable BoxScope.(error: AppError, onRetry: () -> Unit) -> Unit =
        { error, retry -> DefaultErrorContent(error, retry) },
    overlayLoadingContent: @Composable BoxScope.() -> Unit = { OverlayLoading() },
    content: @Composable BoxScope.(state: State) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val overlayLoading by viewModel.overlayLoading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<UiOverlayEvent.Dialog?>(null) }

    ObserveEffect(flow = viewModel.effect, onEffect = onEffect)
    ObserveOverlayEvents(
        events = viewModel.overlayEvents,
        snackbarHostState = snackbarHostState,
        onDialog = { dialog = it },
    )

    dialog?.let { OverlayDialog(dialog = it, onDismiss = { dialog = null }) }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = contentWindowInsets,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        StateBox(
            state = screenState,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding),
            loadingContent = loadingContent,
            errorContent = errorContent,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content(uiState)

                AnimatedVisibility(
                    visible = overlayLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    overlayLoadingContent()
                }
            }
        }
    }
}

@Composable
fun <Effect> ObserveEffect(
    flow: Flow<Effect>,
    onEffect: (Effect) -> Unit,
) {
    val currentHandler by rememberUpdatedState(onEffect)
    LaunchedEffect(flow) {
        flow.collect { currentHandler(it) }
    }
}

@Composable
private fun ObserveOverlayEvents(
    events: Flow<UiOverlayEvent>,
    snackbarHostState: SnackbarHostState,
    onDialog: (UiOverlayEvent.Dialog?) -> Unit,
) {
    val context = LocalContext.current
    val currentOnDialog by rememberUpdatedState(onDialog)

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                // launch: showSnackbar suspends until dismissed and must not block later events
                is UiOverlayEvent.SnackBar -> launch {
                    snackbarHostState.showSnackbar(
                        message = event.message.asString(context),
                        actionLabel = event.actionLabel,
                        duration = event.duration,
                    )
                }

                is UiOverlayEvent.Toast ->
                    Toast.makeText(
                        context,
                        event.message.asString(context),
                        event.duration.toLength(),
                    ).show()

                is UiOverlayEvent.Dialog -> currentOnDialog(event)
                UiOverlayEvent.DismissDialog -> currentOnDialog(null)
            }
        }
    }
}

@Composable
private fun OverlayDialog(
    dialog: UiOverlayEvent.Dialog,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (dialog.cancelable) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dialog.cancelable,
            dismissOnClickOutside = dialog.cancelable,
        ),
        title = { Text(text = dialog.title.asString()) },
        text = { Text(text = dialog.message.asString()) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(dialog.positiveText))
            }
        },
        dismissButton = dialog.negativeText?.let { negative ->
            {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(negative))
                }
            }
        },
    )
}

/** Dimmed full-screen loader that also swallows touches so the content underneath is not clickable. */
@Composable
private fun OverlayLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

private fun ToastDuration.toLength(): Int =
    if (this == ToastDuration.Short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
