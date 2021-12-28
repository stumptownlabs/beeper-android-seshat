package com.beeper.android_seshat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.beeper.android_seshat.database.Database
import com.beeper.android_seshat.event.Event
import com.beeper.android_seshat.event.EventType
import com.beeper.android_seshat.profile.Profile
import com.beeper.android_seshat.search.SearchConfig
import com.beeper.android_seshat.search.SearchResult
import com.beeper.android_seshat.ui.theme.AppTheme
import com.beeper.android_seshat.ui.theme.MessagesTab
import com.beeper.android_seshat.ui.theme.SearchTab
import com.beeper.android_seshat.ui.theme.TabItems
import com.beeper.android_seshat.util.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class SampleSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbCreationResult = Database.get(filesDir.path)

        if (dbCreationResult is Error) {
            setContent {
                AppTheme {
                    Text("App error: DB couldn't be loaded.")
                }
            }
            return
        }
        val db = (dbCreationResult as Success).value

        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val selectedTab = remember { mutableStateOf<TabItems>(MessagesTab) }
                    when (selectedTab.value) {
                        MessagesTab -> {
                            val messages = remember { mutableStateOf(listOf<String>()) }
                            AppTheme {
                                MessagesViewContent(messages,
                                    { message ->

                                        messages.value = messages.value.toMutableList().apply {
                                            add(message)
                                        }.toList()
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            val body = JSONObject()
                                            body.put("body", message)
                                            val eventId = JSONObject()
                                            eventId.put("event_id", message)

                                            db.addEvent(
                                                Event(
                                                    EventType.Message,
                                                    message,
                                                    "m.text",
                                                    message,
                                                    "@example2:localhost",
                                                    151636_2244024,
                                                    "!test_room:localhost",
                                                ),
                                                Profile(
                                                    "Matrix user",
                                                    "https://avatar.com/avatar"
                                                )
                                            )
                                            //A commit to the database takes so time
                                            when(db.commit()){
                                                is com.beeper.android_seshat.util.Error -> {
                                                    Log.d("SampleSearchActivity",
                                                        "Commit error.")
                                                }
                                                is Success -> {
                                                    //After a successful commit, it takes some time
                                                    //for the data to be available on search
                                                    Log.d("SampleSearchActivity",
                                                        "Commit succeeded.")

                                                }
                                            }

                                        }

                                    },
                                    {
                                        selectedTab.value = SearchTab
                                    }
                                )
                            }
                        }
                        SearchTab -> {
                            val searchResultState =
                                remember { mutableStateOf(listOf<SearchResult>()) }

                            SearchViewContent(
                                searchResultState,
                                onSearchClicked = { searchTerm ->
                                    lifecycleScope.launchWhenStarted {
                                        withContext(Dispatchers.IO) {
                                            when (val searchResult =
                                                db.search(searchTerm, SearchConfig())) {
                                                is Error -> {
                                                    withContext(Dispatchers.Main) {
                                                        searchResultState.value = listOf()
                                                    }
                                                }
                                                is Success -> {
                                                    withContext(Dispatchers.Main) {
                                                        searchResultState.value =
                                                            searchResult.value.results
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onNavigateToMessagesClicked = {
                                    selectedTab.value = MessagesTab
                                }
                            )

                        }
                    }

                }
            }
        }
    }
}

