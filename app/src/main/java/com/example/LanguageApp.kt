package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.PhraseSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LanguageApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            // Seed / update all rich phrases across HSK 1, HSK 2, JLPT N3, JLPT N2, JLPT N1
            db.phraseDao().insertPhrases(PhraseSeedData.initialPhrases)
        }
    }
}
