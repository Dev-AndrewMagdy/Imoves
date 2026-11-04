package com.triangles.imoves.features.movies.home.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.triangles.imoves.R
import com.triangles.imoves.ui.theme.ImovesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoviesSearchBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun typingNotifiesQueryChange() {
        val query = mutableStateOf("")

        composeRule.setContent {
            ImovesTheme {
                MoviesSearchBar(
                    query = query.value,
                    onQueryChange = { query.value = it },
                    onClearQuery = { query.value = "" },
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("dune")

        composeRule.runOnIdle { assertEquals("dune", query.value) }
    }

    @Test
    fun clickingClearNotifiesClearQuery() {
        val query = mutableStateOf("dune")

        composeRule.setContent {
            ImovesTheme {
                MoviesSearchBar(
                    query = query.value,
                    onQueryChange = { query.value = it },
                    onClearQuery = { query.value = "" },
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.clear_search))
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle { assertEquals("", query.value) }
    }

    @Test
    fun clearActionIsHiddenWhenQueryIsEmpty() {
        val query = mutableStateOf("")

        composeRule.setContent {
            ImovesTheme {
                MoviesSearchBar(
                    query = query.value,
                    onQueryChange = { query.value = it },
                    onClearQuery = { query.value = "" },
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.clear_search))
            .assertDoesNotExist()
    }
}
