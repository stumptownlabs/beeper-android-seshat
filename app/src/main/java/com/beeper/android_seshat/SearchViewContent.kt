package com.beeper.android_seshat


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beeper.android_seshat.search.SearchResult
import com.beeper.android_seshat.ui.theme.AppTheme

@Composable
fun SearchViewContent(searchResult: State<List<SearchResult>>,
                      onSearchClicked: (String) -> Unit,
                      onNavigateToMessagesClicked: () -> Unit,

                      ) {
    val searchTermState = remember { mutableStateOf(TextFieldValue()) }
    val paddingModifier = Modifier.padding(vertical = 16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                onNavigateToMessagesClicked()
            }, modifier = paddingModifier
        ) {
            Text("Navigate to messages ->")
        }
        Text(text = "Search", modifier = paddingModifier)
        TextField(
            label = {
                        Text("Search term")
                    },
            value = searchTermState.value,
            onValueChange = { searchTermState.value = it }
        )
        Button(
            onClick = {
                onSearchClicked(searchTermState.value.text)
            }, modifier = paddingModifier
        ) {
            Text("Search")
        }
        Text(text = "Search results:", modifier = paddingModifier)
        LazyColumn{
            if(searchResult.value.isEmpty()){
                item{
                    Text("No results found...",
                        color = Color.Blue,
                        modifier = Modifier.padding(32.dp))
                }
            }

            searchResult.value.onEach {
                item{
                    Row{
                        Text("Score:${it.score}, content:${it.eventSource}",
                            color = Color.Blue,
                            modifier = Modifier.padding(32.dp))
                    }
                }
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun SearchViewPreview() {
    val searchResult = remember { mutableStateOf(listOf<SearchResult>()) }
    AppTheme {
        SearchViewContent(
            searchResult,{}, {}
        )
    }
}

