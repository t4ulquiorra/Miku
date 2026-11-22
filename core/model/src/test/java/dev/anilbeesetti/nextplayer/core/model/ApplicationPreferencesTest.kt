package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationPreferencesTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `default mediaLayoutMode is CRATE when mediaViewMode is FOLDERS`() {
        val prefs = ApplicationPreferences(mediaViewMode = MediaViewMode.FOLDERS)
        assertEquals(MediaLayoutMode.CRATE, prefs.mediaLayoutMode)
    }

    @Test
    fun `default mediaLayoutMode is LIST when mediaViewMode is VIDEOS`() {
        val prefs = ApplicationPreferences(mediaViewMode = MediaViewMode.VIDEOS)
        assertEquals(MediaLayoutMode.LIST, prefs.mediaLayoutMode)
    }

    @Test
    fun `existing saved preferences with LIST decode correctly`() {
        val jsonStr = """{"mediaLayoutMode":"LIST"}"""
        val prefs = json.decodeFromString<ApplicationPreferences>(jsonStr)
        assertEquals(MediaLayoutMode.LIST, prefs.mediaLayoutMode)
    }

    @Test
    fun `existing saved preferences with GRID decode correctly`() {
        val jsonStr = """{"mediaLayoutMode":"GRID"}"""
        val prefs = json.decodeFromString<ApplicationPreferences>(jsonStr)
        assertEquals(MediaLayoutMode.GRID, prefs.mediaLayoutMode)
    }

    @Test
    fun `unknown or corrupted mediaLayoutMode falls back without crashing`() {
        val jsonStr = """{"mediaLayoutMode":"CORRUPTED_VALUE"}"""
        val prefs = json.decodeFromString<ApplicationPreferences>(jsonStr)
        assertEquals(MediaLayoutMode.CRATE, prefs.mediaLayoutMode)
    }

    @Test
    fun `missing mediaLayoutMode defaults to CRATE when view mode is FOLDERS`() {
        val jsonStr = """{"mediaViewMode":"FOLDERS"}"""
        val prefs = json.decodeFromString<ApplicationPreferences>(jsonStr)
        assertEquals(MediaLayoutMode.CRATE, prefs.mediaLayoutMode)
    }
}
