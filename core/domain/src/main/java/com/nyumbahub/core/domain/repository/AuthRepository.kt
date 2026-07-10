package com.nyumbahub.core.domain.repository
import com.nyumbahub.core.domain.model.User
import com.nyumbahub.core.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, name: String, role: UserRole = UserRole.SEEKER, agency: String = "", phone: String = ""): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, code: String): Result<Unit>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}
