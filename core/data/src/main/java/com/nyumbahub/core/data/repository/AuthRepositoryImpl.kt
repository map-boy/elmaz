package com.nyumbahub.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.domain.model.User
import com.nyumbahub.core.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val db = FirebaseFirestore.getInstance()

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.let {
                User(id = it.uid, email = it.email ?: "", displayName = it.displayName ?: "")
            })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = runCatching {
        val u = auth.signInWithEmailAndPassword(email, password).await().user!!
        User(id = u.uid, email = u.email ?: "", displayName = u.displayName ?: "")
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User> = runCatching {
        val u = auth.createUserWithEmailAndPassword(email, password).await().user!!
        u.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        db.collection("users").document(u.uid).set(
            mapOf(
                "uid"         to u.uid,
                "email"       to (u.email ?: ""),
                "displayName" to name,
                "role"        to "user",
                "plan"        to "free",
                "createdAt"   to System.currentTimeMillis()
            )
        ).await()
        User(id = u.uid, email = u.email ?: "", displayName = name)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val u = auth.signInWithCredential(credential).await().user!!
        val doc = db.collection("users").document(u.uid).get().await()
        if (!doc.exists()) {
            db.collection("users").document(u.uid).set(
                mapOf(
                    "uid"         to u.uid,
                    "email"       to (u.email ?: ""),
                    "displayName" to (u.displayName ?: ""),
                    "role"        to "user",
                    "plan"        to "free",
                    "createdAt"   to System.currentTimeMillis()
                )
            ).await()
        }
        User(id = u.uid, email = u.email ?: "", displayName = u.displayName ?: "")
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun sendOtp(phone: String): Result<Unit> = runCatching { }

    override suspend fun verifyOtp(phone: String, code: String): Result<Unit> = runCatching { }

    override suspend fun signOut() { auth.signOut() }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        auth.currentUser!!.delete().await()
    }
}
