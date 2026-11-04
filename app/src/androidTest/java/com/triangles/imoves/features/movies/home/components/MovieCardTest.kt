package com.triangles.imoves.features.movies.home.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.triangles.domain.features.moves.model.Movie
import com.triangles.imoves.R
import com.triangles.imoves.ui.theme.ImovesTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun displaysTitleAndReleaseYear() {
        val movie = movie()

        composeRule.setContent {
            ImovesTheme {
                MovieCard(movie = movie, onClick = {})
            }
        }

        composeRule.onNodeWithText(movie.title).assertIsDisplayed()
        composeRule.onNodeWithText(RELEASE_YEAR).assertIsDisplayed()
    }

    @Test
    fun displaysPosterContentDescription() {
        val movie = movie()

        composeRule.setContent {
            ImovesTheme {
                MovieCard(movie = movie, onClick = {})
            }
        }

        composeRule.onNodeWithContentDescription(
            context.getString(R.string.movie_poster, movie.title)
        ).assertIsDisplayed()
    }

    @Test
    fun clickingTheCardInvokesTheCallback() {
        var clicked = false
        val movie = movie()

        composeRule.setContent {
            ImovesTheme {
                MovieCard(movie = movie, onClick = { clicked = true })
            }
        }

        composeRule.onNode(hasClickAction()).performClick()

        assertTrue(clicked)
    }

    private fun movie() = Movie(
        id = 1,
        title = "Dune: Part Two",
        releaseDate = "2024-02-27",
        posterImage = "",
    )

    private companion object {
        const val RELEASE_YEAR = "2024"
    }
}
