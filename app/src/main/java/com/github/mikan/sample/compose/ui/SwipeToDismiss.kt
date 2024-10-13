package com.github.mikan.sample.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.github.mikan.sample.compose.ui.theme.ComposeSampleTheme
import java.io.Serializable

data class MessageUiState(
    val id: MessageId,
    val title: String,
)

@JvmInline
value class MessageId(val value: String) : Serializable

sealed interface MessageScreenUiEvent

sealed interface MessagesSectionUiEvent : MessageScreenUiEvent {
    data class MarkAsRead(val id: MessageId) : MessagesSectionUiEvent
    data class Delete(val id: MessageId) : MessagesSectionUiEvent
}

@Composable
fun MessageScreen() {
    val messages = remember {
        mutableStateListOf(
            MessageUiState(MessageId("1"), "Hello, World!"),
            MessageUiState(MessageId("2"), "Hello, Compose!"),
            MessageUiState(MessageId("3"), "Hello, Android!"),
        )
    }

    val onUiEvent: (MessageScreenUiEvent) -> Unit = { uiEvent ->
        when (uiEvent) {
            is MessagesSectionUiEvent.MarkAsRead -> {
            }

            is MessagesSectionUiEvent.Delete -> {
                messages
                    .find { it.id == uiEvent.id }
                    ?.let {
                        messages.remove(it)
                    }
            }
        }
    }

    MessagesScreen(
        messages = messages,
        onUiEvent = onUiEvent,
    )
}

@Composable
fun MessagesScreen(
    messages: List<MessageUiState>,
    onUiEvent: (MessageScreenUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        MessagesSection(
            header = { Text("Unread") },
            messages = messages,
            onUiEvent = onUiEvent,
        )
    }
}

fun LazyListScope.MessagesSection(
    header: @Composable () -> Unit,
    messages: List<MessageUiState>,
    onUiEvent: (MessagesSectionUiEvent) -> Unit,
) {
    item {
        header()
    }
    items(messages, { it.id }) { message ->
        SwipeToDismissMessage(
            onMarkAsRead = { onUiEvent(MessagesSectionUiEvent.MarkAsRead(message.id)) },
            onDelete = { onUiEvent(MessagesSectionUiEvent.Delete(message.id)) },
        ) {
            Message(message)
        }
    }
}

@Composable
fun SwipeToDismissMessage(
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    message: @Composable () -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState()

    val icon: ImageVector
    val alignment: Alignment
    val color: Color

    when (swipeState.dismissDirection) {
        SwipeToDismissBoxValue.Settled -> {
            icon = Icons.Default.Edit
            alignment = Alignment.CenterStart
            color = Color.Blue
        }

        SwipeToDismissBoxValue.StartToEnd -> {
            icon = Icons.Default.Edit
            alignment = Alignment.CenterStart
            color = Color.Blue
        }

        SwipeToDismissBoxValue.EndToStart -> {
            icon = Icons.Default.Delete
            alignment = Alignment.CenterEnd
            color = Color.Red
        }
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = swipeState,
        backgroundContent = {
            Box(
                contentAlignment = alignment,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color),
            ) {
                Icon(icon, contentDescription = null)
            }
        }
    ) {
        message()
    }

    LaunchedEffect(swipeState) {
        when (swipeState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onMarkAsRead()
                swipeState.reset() // FIX: resetが効かない
            }

            SwipeToDismissBoxValue.EndToStart -> {
                onDelete()
            }

            SwipeToDismissBoxValue.Settled -> {}
        }
    }
}

@Composable
fun Message(
    uiState: MessageUiState,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier,
        shape = RectangleShape
    ) {
        ListItem(
            headlineContent = {
                Text(uiState.title)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagePreview() {
    ComposeSampleTheme {
        MessageScreen()
    }
}