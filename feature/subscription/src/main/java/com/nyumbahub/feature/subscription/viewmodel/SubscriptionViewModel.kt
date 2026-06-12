package com.nyumbahub.feature.subscription.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyumbahub.core.domain.model.SubscriptionPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor() : ViewModel() {

    private val _currentPlan = MutableStateFlow(SubscriptionPlan.FREE)
    val currentPlan: StateFlow<SubscriptionPlan> = _currentPlan

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    val plans = SubscriptionPlan.values().toList()

    fun selectPlan(plan: SubscriptionPlan) {
        if (plan == SubscriptionPlan.FREE) {
            _currentPlan.value = plan
            return
        }
        viewModelScope.launch {
            _purchaseState.value = PurchaseState.Processing
            // Save plan to Firestore
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("subscriptions").document(uid)
                    .set(mapOf("plan" to plan.name.lowercase(), "updatedAt" to System.currentTimeMillis()))
            }
            // On success:
            _currentPlan.value = plan
            _purchaseState.value = PurchaseState.Success(plan)
        }
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    data class Success(val plan: SubscriptionPlan) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

