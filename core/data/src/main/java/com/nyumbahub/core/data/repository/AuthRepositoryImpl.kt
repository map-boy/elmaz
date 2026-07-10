package com.nyumbahub.core.data.repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.domain.model.User
import com.nyumbahub.core.domain.model.UserRole
import com.nyumbahub.core.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private fun parseRole(raw: String?): UserRole =
    raw?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: UserRole.SEEKER

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    private val db = FirebaseFirestore.getInstance()

    override val currentUser: Flow<User?> = callbackFlow {
        val scope = this
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val fbUser = fa.currentUser
            if (fbUser == null) {
                trySend(null)
            } else {
                scope.launch {
                    val doc = runCatching { db.collection("users").document(fbUser.uid).get().await() }.getOrNull()
                    trySend(
                        User(
                            id = fbUser.uid,
                            email = fbUser.email ?: "",
                            displayName = doc?.getString("displayName") ?: (fbUser.displayName ?: ""),
                            phone = doc?.getString("phone") ?: "",
                            role = parseRole(doc?.getString("role")),
                            agency = doc?.getString("agency") ?: ""
                        )
                    )
                }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = runCatching {
        val u = auth.signInWithEmailAndPassword(email, password).await().user!!
        val doc = runCatching { db.collection("users").document(u.uid).get().await() }.getOrNull()
        User(
            id = u.uid,
            email = u.email ?: "",
            displayName = doc?.getString("displayName") ?: (u.displayName ?: ""),
            phone = doc?.getString("phone") ?: "",
            role = parseRole(doc?.getString("role")),
            agency = doc?.getString("agency") ?: ""
        )
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String, role: UserRole, agency: String, phone: String): Result<User> = runCatching {
        val u = auth.createUserWithEmailAndPassword(email, password).await().user!!
        u.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        db.collection("users").document(u.uid).set(
            mapOf(
                "uid"         to u.uid,
                "email"       to (u.email ?: ""),
                "displayName" to name,
                "role"        to role.name,
                "agency"      to agency,
                "phone"       to phone,
                "plan"        to "free",
                "createdAt"   to System.currentTimeMillis()
            )
        ).await()
        User(id = u.uid, email = u.email ?: "", displayName = name, phone = phone, role = role, agency = agency)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val u = auth.signInWithCredential(credential).await().user!!
        val docRef = db.collection("users").document(u.uid)
        val doc = docRef.get().await()
        if (!doc.exists()) {
            docRef.set(
                mapOf(
                    "uid"         to u.uid,
                    "email"       to (u.email ?: ""),
                    "displayName" to (u.displayName ?: ""),
                    "role"        to UserRole.SEEKER.name,
                    "agency"      to "",
                    "phone"       to "",
                    "plan"        to "free",
                    "createdAt"   to System.currentTimeMillis()
                )
            ).await()
            User(id = u.uid, email = u.email ?: "", displayName = u.displayName ?: "", role = UserRole.SEEKER)
        } else {
            User(
                id = u.uid,
                email = u.email ?: "",
                displayName = doc.getString("displayName") ?: (u.displayName ?: ""),
                phone = doc.getString("phone") ?: "",
                role = parseRole(doc.getString("role")),
                agency = doc.getString("agency") ?: ""
            )
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }
    override suspend fun sendOtp(phone: String): Result<Unit> = Result.failure(UnsupportedOperationException("Phone OTP not yet implemented"))
    override suspend fun verifyOtp(phone: String, code: String): Result<Unit> = Result.failure(UnsupportedOperationException("Phone OTP not yet implemented"))
    override suspend fun signOut() { auth.signOut() }
    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        auth.currentUser!!.delete().await()
    }
}


