package com.example.firebase

import android.content.Context
import com.example.R
import com.example.firebase.models.CustomPhraseDto
import com.example.firebase.models.FavoriteDto
import com.example.firebase.models.UserProfileDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun currentUserId(): String? = auth.currentUser?.uid

    // --- User Profile Sync ---
    suspend fun syncUserProfile(
        totalMastered: Long,
        currentStreak: Long,
        preferredLanguage: String,
        selectedSlot: String
    ): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return try {
            val user = auth.currentUser
            val payload = mapOf(
                "userId" to uid,
                "displayName" to (user?.displayName ?: "Language Learner"),
                "email" to (user?.email ?: ""),
                "totalMastered" to totalMastered,
                "currentStreak" to currentStreak,
                "preferredLanguage" to preferredLanguage,
                "selectedSlot" to selectedSlot,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Custom Phrases Cloud Sync ---
    fun observeCustomPhrases(): Flow<List<CustomPhraseDto>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/customPhrases"
        emitAll(
            db.collection("users").document(uid).collection("customPhrases")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(CustomPhraseDto::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveCustomPhrase(phrase: CustomPhraseDto): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/customPhrases/${phrase.id}"
        return try {
            val payload = mapOf(
                "id" to phrase.id,
                "userId" to uid,
                "language" to phrase.language,
                "nativeText" to phrase.nativeText,
                "romanization" to phrase.romanization,
                "english" to phrase.english,
                "category" to phrase.category,
                "culturalNote" to phrase.culturalNote,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("customPhrases")
                .document(phrase.id)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomPhrase(phraseId: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/customPhrases/$phraseId"
        return try {
            db.collection("users").document(uid).collection("customPhrases")
                .document(phraseId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Bookmarked Favorites Cloud Sync ---
    fun observeFavorites(): Flow<List<FavoriteDto>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/favorites"
        emitAll(
            db.collection("users").document(uid).collection("favorites")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FavoriteDto::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveFavorite(phraseId: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/favorites/$phraseId"
        return try {
            val payload = mapOf(
                "phraseId" to phraseId,
                "userId" to uid,
                "savedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("favorites")
                .document(phraseId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(phraseId: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/favorites/$phraseId"
        return try {
            db.collection("users").document(uid).collection("favorites")
                .document(phraseId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
