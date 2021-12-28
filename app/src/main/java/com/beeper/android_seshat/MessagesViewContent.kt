package com.beeper.android_seshat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beeper.android_seshat.ui.theme.AppTheme


@Composable
fun MessagesViewContent(messages : State<List<String>>,
                        onSendMessageClicked: (String) -> Unit,
                        onNavigateToSearchClicked: () -> Unit,) {

    val textState = remember { mutableStateOf(TextFieldValue()) }

    val paddingModifier = Modifier.padding(vertical = 16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                onNavigateToSearchClicked()
            }, modifier = paddingModifier
        ) {
            Text("Navigate to search ->")
        }
        Text(text = "Messages", modifier = paddingModifier)
        TextField(
            value = textState.value,
            onValueChange = { textState.value = it }
        )
        Button(
            onClick = {
                val message = textState.value.text
                if(message.isNotEmpty()) {
                    onSendMessageClicked(message)
                }
            }, modifier = paddingModifier
        ) {
            Text("Send message")
        }
        Text(text = "Sent messages:", modifier = paddingModifier)
        LazyColumn{
            messages.value.onEach {
                item {
                    Text(text = it, modifier = paddingModifier)
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun MessagesViewPreview() {
    val messages = remember { mutableStateOf(mutableListOf("test message",
        "test message 2", "test message 3")) }

    AppTheme {
        MessagesViewContent(messages,{},{})
    }
}
