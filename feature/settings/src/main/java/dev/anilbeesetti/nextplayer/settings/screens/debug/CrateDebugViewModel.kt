package dev.anilbeesetti.nextplayer.settings.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anilbeesetti.nextplayer.core.domain.GetRecentlyPlayedVideoUseCase
import dev.anilbeesetti.nextplayer.core.domain.GetSortedFoldersUseCase
import dev.anilbeesetti.nextplayer.core.domain.GetSortedVideosUseCase
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.model.Video
import dev.anilbeesetti.nextplayer.core.model.findClosestFolder
import dev.anilbeesetti.nextplayer.core.ui.components.CrateCardVideoPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class CrateDebugUiState(
    val folders: List<Folder> = emptyList(),
    val recentlyPlayedVideo: Video? = null,
    val lastPlayedFolderPath: String? = null,
    val previewCache: Map<String, List<CrateCardVideoPreview>> = emptyMap(),
    val settledFrontFolderPath: String? = null,
    val settledFrontVideos: List<Video> = emptyList(),
)

@KoinViewModel
class CrateDebugViewModel(
    private val getSortedFoldersUseCase: GetSortedFoldersUseCase,
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val getRecentlyPlayedVideoUseCase: GetRecentlyPlayedVideoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrateDebugUiState())
    val uiState: StateFlow<CrateDebugUiState> = _uiState.asStateFlow()

    private var frontVideosJob: Job? = null
    private var nearbyPreviewsJob: Job? = null

    init {
        viewModelScope.launch {
            getSortedFoldersUseCase().collect { folders ->
                _uiState.update { state ->
                    val lastPath = state.recentlyPlayedVideo?.let { v ->
                        folders.findClosestFolder(v.path)?.path
                    }
                    state.copy(
                        folders = folders,
                        lastPlayedFolderPath = lastPath,
                    )
                }
            }
        }
        viewModelScope.launch {
            getRecentlyPlayedVideoUseCase(folderPath = null).collect { video ->
                _uiState.update { state ->
                    val lastPath = video?.let { v ->
                        state.folders.findClosestFolder(v.path)?.path
                    }
                    state.copy(
                        recentlyPlayedVideo = video,
                        lastPlayedFolderPath = lastPath,
                    )
                }
            }
        }
    }

    /**
     * Loads the full video list for the settled front folder and up to 3 preview
     * videos for the front 3 folders (never called while dragging/flinging).
     */
    fun onSettledOnFolder(index: Int) {
        val folders = _uiState.value.folders
        val frontFolder = folders.getOrNull(index) ?: return

        if (_uiState.value.settledFrontFolderPath != frontFolder.path) {
            frontVideosJob?.cancel()
            frontVideosJob = viewModelScope.launch {
                getSortedVideosUseCase(frontFolder.path).collect { videos ->
                    val direct = videos.filter { it.parentPath == frontFolder.path }
                    val previews = direct.take(3).map { v ->
                        CrateCardVideoPreview(
                            title = v.displayName,
                            durationMillis = v.duration,
                            uriString = v.uriString,
                        )
                    }
                    _uiState.update { state ->
                        state.copy(
                            settledFrontFolderPath = frontFolder.path,
                            settledFrontVideos = direct,
                            previewCache = state.previewCache + (frontFolder.path to previews),
                        )
                    }
                }
            }
        }

        // Load up to 3 previews for the 2 cards immediately behind the front card
        nearbyPreviewsJob?.cancel()
        nearbyPreviewsJob = viewModelScope.launch {
            for (offset in 1..2) {
                val f = folders.getOrNull(index + offset) ?: continue
                if (_uiState.value.previewCache.containsKey(f.path)) continue
                val videos = getSortedVideosUseCase(f.path).first()
                val previews = videos
                    .filter { it.parentPath == f.path }
                    .take(3)
                    .map { v ->
                        CrateCardVideoPreview(
                            title = v.displayName,
                            durationMillis = v.duration,
                            uriString = v.uriString,
                        )
                    }
                _uiState.update { state ->
                    state.copy(previewCache = state.previewCache + (f.path to previews))
                }
            }
        }
    }
}
