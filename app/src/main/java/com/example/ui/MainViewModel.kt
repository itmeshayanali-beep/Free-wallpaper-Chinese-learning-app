package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PhraseEntity
import com.example.data.WallpaperPreferences
import com.example.engine.LanguageWallpaperService
import com.example.engine.WallpaperSlot
import com.example.firebase.FirebaseRepository
import com.example.firebase.GoogleAuthManager
import com.example.firebase.models.CustomPhraseDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val prefs = WallpaperPreferences.getInstance(application)
    private val firebaseRepo = FirebaseRepository(application)
    private val auth = FirebaseAuth.getInstance()

    val allPhrases: StateFlow<List<PhraseEntity>> = db.phraseDao().getAllPhrases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePhrases: StateFlow<List<PhraseEntity>> = db.phraseDao().getFavoritePhrases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentPhrase = MutableStateFlow<PhraseEntity?>(null)
    val currentPhrase: StateFlow<PhraseEntity?> = _currentPhrase.asStateFlow()

    private val _currentSlot = MutableStateFlow(WallpaperSlot.fromId(prefs.getSlotType()))
    val currentSlot: StateFlow<WallpaperSlot> = _currentSlot.asStateFlow()

    private val _languageFilter = MutableStateFlow(prefs.getLanguageFilter())
    val languageFilter: StateFlow<String> = _languageFilter.asStateFlow()

    private val _levelFilter = MutableStateFlow(prefs.getLevelFilter())
    val levelFilter: StateFlow<String> = _levelFilter.asStateFlow()

    private val _showSafeZones = MutableStateFlow(false)
    val showSafeZones: StateFlow<Boolean> = _showSafeZones.asStateFlow()

    private val _showSimulatedLauncher = MutableStateFlow(true)
    val showSimulatedLauncher: StateFlow<Boolean> = _showSimulatedLauncher.asStateFlow()

    private val _fontScale = MutableStateFlow(prefs.getFontScale())
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _showRomanization = MutableStateFlow(prefs.getShowRomanization())
    val showRomanization: StateFlow<Boolean> = _showRomanization.asStateFlow()

    private val _showEnglish = MutableStateFlow(prefs.getShowEnglish())
    val showEnglish: StateFlow<Boolean> = _showEnglish.asStateFlow()

    private val _showCategoryTag = MutableStateFlow(prefs.getShowCategoryTag())
    val showCategoryTag: StateFlow<Boolean> = _showCategoryTag.asStateFlow()

    private val _customBgPath = MutableStateFlow(prefs.getCustomBackgroundImagePath())
    val customBgPath: StateFlow<String?> = _customBgPath.asStateFlow()

    private val _backgroundDim = MutableStateFlow(prefs.getBackgroundDim())
    val backgroundDim: StateFlow<Float> = _backgroundDim.asStateFlow()

    // Firebase Auth & Cloud Sync State
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow<String>("Offline Storage")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    // Practice Quiz State
    private val _quizQuestion = MutableStateFlow<PhraseEntity?>(null)
    val quizQuestion: StateFlow<PhraseEntity?> = _quizQuestion.asStateFlow()

    private val _quizOptions = MutableStateFlow<List<String>>(emptyList())
    val quizOptions: StateFlow<List<String>> = _quizOptions.asStateFlow()

    private val _selectedQuizAnswer = MutableStateFlow<String?>(null)
    val selectedQuizAnswer: StateFlow<String?> = _selectedQuizAnswer.asStateFlow()

    private val _isQuizAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val isQuizAnswerCorrect: StateFlow<Boolean?> = _isQuizAnswerCorrect.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizStreak = MutableStateFlow(0)
    val quizStreak: StateFlow<Int> = _quizStreak.asStateFlow()

    // TTS Engine
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            _cloudSyncStatus.value = "Synced with Google Cloud"
            startCloudSync(user.uid)
        } else {
            _cloudSyncStatus.value = "Local Storage (Sign in to backup)"
        }
    }

    init {
        initTts()
        loadInitialPhrase()
        auth.addAuthStateListener(authListener)
    }

    private fun initTts() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
    }

    private fun loadInitialPhrase() {
        viewModelScope.launch(Dispatchers.IO) {
            val activeId = prefs.getActivePhraseId()
            var phrase = db.phraseDao().getPhraseById(activeId)
            if (phrase == null) {
                phrase = db.phraseDao().getRandomPhraseWithFilters(prefs.getLanguageFilter(), prefs.getLevelFilter())
                    ?: db.phraseDao().getRandomPhrase(prefs.getLanguageFilter())
            }
            _currentPhrase.value = phrase
            initNextQuizQuestion()
        }
    }

    private fun startCloudSync(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Sync user profile stats
                firebaseRepo.syncUserProfile(
                    totalMastered = _quizScore.value.toLong(),
                    currentStreak = _quizStreak.value.toLong(),
                    preferredLanguage = _languageFilter.value,
                    selectedSlot = _currentSlot.value.id
                )

                // 2. Observe and merge cloud custom phrases into local Room DB
                firebaseRepo.observeCustomPhrases().collectLatest { cloudPhrases ->
                    cloudPhrases.forEach { cloud ->
                        val local = db.phraseDao().getPhraseById(cloud.id)
                        if (local == null) {
                            db.phraseDao().insertPhrase(
                                PhraseEntity(
                                    id = cloud.id,
                                    language = cloud.language,
                                    nativeText = cloud.nativeText,
                                    romanization = cloud.romanization,
                                    english = cloud.english,
                                    category = cloud.category,
                                    level = "Custom",
                                    culturalNote = cloud.culturalNote
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _cloudSyncStatus.value = "Sync pause: ${e.localizedMessage ?: "Network issue"}"
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onComplete: (Boolean, String?) -> Unit) {
        val credentialManager = CredentialManager.create(activity)
        GoogleAuthManager.signInWithGoogle(
            activity = activity,
            credentialManager = credentialManager,
            onAuthSuccess = {
                onComplete(true, null)
            },
            onAuthError = { error ->
                onComplete(false, error)
            },
            scope = viewModelScope
        )
    }

    fun signOut(activity: Activity) {
        val credentialManager = CredentialManager.create(activity)
        GoogleAuthManager.signOut(
            credentialManager = credentialManager,
            onSignOutComplete = {
                _currentUser.value = null
            },
            scope = viewModelScope
        )
    }

    fun selectSlot(slot: WallpaperSlot) {
        prefs.setSlotType(slot.id)
        _currentSlot.value = slot
        if (auth.currentUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                firebaseRepo.syncUserProfile(
                    totalMastered = _quizScore.value.toLong(),
                    currentStreak = _quizStreak.value.toLong(),
                    preferredLanguage = _languageFilter.value,
                    selectedSlot = slot.id
                )
            }
        }
    }

    fun setCustomGalleryBackground(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "custom_wallpaper_bg.jpg")
                inputStream?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val path = targetFile.absolutePath
                prefs.setCustomBackgroundImagePath(path)
                prefs.setSlotType(WallpaperSlot.CUSTOM_GALLERY.id)
                _customBgPath.value = path
                _currentSlot.value = WallpaperSlot.CUSTOM_GALLERY
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateBackgroundDim(dim: Float) {
        prefs.setBackgroundDim(dim)
        _backgroundDim.value = dim
    }

    fun selectLanguageFilter(lang: String) {
        prefs.setLanguageFilter(lang)
        _languageFilter.value = lang
        nextHourPhrase()
    }

    fun selectLevelFilter(level: String) {
        prefs.setLevelFilter(level)
        _levelFilter.value = level
        nextHourPhrase()
    }

    fun toggleSafeZones() {
        _showSafeZones.value = !_showSafeZones.value
    }

    fun toggleSimulatedLauncher() {
        _showSimulatedLauncher.value = !_showSimulatedLauncher.value
    }

    fun updateFontScale(scale: Float) {
        prefs.setFontScale(scale)
        _fontScale.value = scale
    }

    fun toggleRomanization() {
        val newVal = !_showRomanization.value
        prefs.setShowRomanization(newVal)
        _showRomanization.value = newVal
    }

    fun toggleEnglish() {
        val newVal = !_showEnglish.value
        prefs.setShowEnglish(newVal)
        _showEnglish.value = newVal
    }

    fun toggleCategoryTag() {
        val newVal = !_showCategoryTag.value
        prefs.setShowCategoryTag(newVal)
        _showCategoryTag.value = newVal
    }

    fun nextHourPhrase() {
        viewModelScope.launch(Dispatchers.IO) {
            val phrase = db.phraseDao().getRandomPhraseWithFilters(_languageFilter.value, _levelFilter.value)
                ?: db.phraseDao().getRandomPhrase(_languageFilter.value)
            if (phrase != null) {
                prefs.setActivePhraseId(phrase.id)
                db.phraseDao().markPhraseShown(phrase.id, System.currentTimeMillis())
                _currentPhrase.value = phrase
            }
        }
    }

    fun selectPhrase(phrase: PhraseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setActivePhraseId(phrase.id)
            db.phraseDao().markPhraseShown(phrase.id, System.currentTimeMillis())
            _currentPhrase.value = phrase
        }
    }

    fun toggleFavorite(phrase: PhraseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val newFav = !phrase.isFavorite
            db.phraseDao().toggleFavorite(phrase.id, newFav)
            if (_currentPhrase.value?.id == phrase.id) {
                _currentPhrase.value = _currentPhrase.value?.copy(isFavorite = newFav)
            }
            // Sync to Firestore if user is authenticated
            if (auth.currentUser != null) {
                if (newFav) {
                    firebaseRepo.saveFavorite(phrase.id)
                } else {
                    firebaseRepo.removeFavorite(phrase.id)
                }
            }
        }
    }

    fun speakPhrase(phrase: PhraseEntity) {
        if (!isTtsReady || tts == null) return
        val locale = if (phrase.language == "ja") Locale.JAPANESE else Locale.CHINESE
        tts?.language = locale
        tts?.speak(phrase.nativeText, TextToSpeech.QUEUE_FLUSH, null, "phrase_tts_${phrase.id}")
    }

    fun addCustomPhrase(
        nativeText: String,
        romanization: String,
        english: String,
        language: String,
        category: String,
        level: String = "JLPT N3",
        culturalNote: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "${language}-${System.currentTimeMillis()}"
            val newPhrase = PhraseEntity(
                id = id,
                language = language,
                nativeText = nativeText.trim(),
                romanization = romanization.trim(),
                english = english.trim(),
                category = category.trim().ifEmpty { "Personal Note" },
                level = level.trim().ifEmpty { if (language == "zh") "HSK 1" else "JLPT N3" },
                culturalNote = culturalNote.trim()
            )
            db.phraseDao().insertPhrase(newPhrase)
            selectPhrase(newPhrase)

            // Cloud sync to Firestore if signed in
            if (auth.currentUser != null) {
                firebaseRepo.saveCustomPhrase(
                    CustomPhraseDto(
                        id = id,
                        userId = auth.currentUser!!.uid,
                        language = language,
                        nativeText = newPhrase.nativeText,
                        romanization = newPhrase.romanization,
                        english = newPhrase.english,
                        category = newPhrase.category,
                        culturalNote = newPhrase.culturalNote
                    )
                )
            }
        }
    }

    // --- Practice Quiz Methods ---
    fun initNextQuizQuestion(levelFilter: String = "all") {
        viewModelScope.launch(Dispatchers.IO) {
            val rawPhrases = allPhrases.value.ifEmpty {
                db.phraseDao().getAllPhrases()
                    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            }
            val filtered = if (levelFilter == "all") rawPhrases else rawPhrases.filter { it.level.equals(levelFilter, ignoreCase = true) }
            val pool = if (filtered.size >= 4) filtered else rawPhrases
            if (pool.size < 4) return@launch

            val target = pool.random()
            val wrongOptions = pool.filter { it.id != target.id }.shuffled().take(3).map { it.english }
            val options = (wrongOptions + target.english).shuffled()

            _quizQuestion.value = target
            _quizOptions.value = options
            _selectedQuizAnswer.value = null
            _isQuizAnswerCorrect.value = null
        }
    }

    fun submitQuizAnswer(answer: String) {
        val target = _quizQuestion.value ?: return
        _selectedQuizAnswer.value = answer
        val correct = answer == target.english
        _isQuizAnswerCorrect.value = correct
        if (correct) {
            _quizScore.value += 1
            _quizStreak.value += 1
        } else {
            _quizStreak.value = 0
        }

        // Sync streak to Firestore
        if (auth.currentUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                firebaseRepo.syncUserProfile(
                    totalMastered = _quizScore.value.toLong(),
                    currentStreak = _quizStreak.value.toLong(),
                    preferredLanguage = _languageFilter.value,
                    selectedSlot = _currentSlot.value.id
                )
            }
        }
    }

    fun createSetWallpaperIntent(): Intent {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            intent.action = android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            intent.putExtra(
                android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(getApplication(), LanguageWallpaperService::class.java)
            )
        } else {
            intent.action = android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        tts?.stop()
        tts?.shutdown()
    }
}
