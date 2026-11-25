package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiagnosisResult
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * [Principio D - DIP] Implementacion de detalle de [AnalysisHistoryRepository] respaldada por
 * Firestore, en `users/{uid}/history`: a diferencia de [InMemoryAnalysisHistoryRepository], el
 * historial sobrevive a cerrar la app y queda asociado a la cuenta real del usuario (no al
 * proceso), que es el otro punto donde "se conecta la base con la autenticacion": cada analisis
 * se guarda bajo el `uid` de [FirebaseAuth.currentUser], nunca en una coleccion global.
 */
class FirestoreAnalysisHistoryRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val externalScope: CoroutineScope
) : AnalysisHistoryRepository {

    private val _history = MutableStateFlow<List<DiagnosisResult>>(emptyList())
    override val history: StateFlow<List<DiagnosisResult>> = _history.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        firebaseAuth.addAuthStateListener { auth ->
            listenerRegistration?.remove()
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _history.value = emptyList()
                return@addAuthStateListener
            }
            listenerRegistration = historyCollection(uid)
                .orderBy("recordedAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        Log.e("FirestoreHistory", "No se pudo escuchar el historial", error)
                        return@addSnapshotListener
                    }
                    _history.value = snapshot.documents.mapNotNull { it.toDiagnosisResult() }
                }
        }
    }

    override fun record(result: DiagnosisResult) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val data = mapOf(
            "code" to result.code,
            "confidence" to result.confidence,
            "riskLevel" to result.riskLevel.name,
            "recordedAt" to FieldValue.serverTimestamp()
        )
        externalScope.launch {
            try {
                historyCollection(uid).add(data).await()
            } catch (e: Exception) {
                Log.e("FirestoreHistory", "No se pudo guardar el analisis", e)
            }
        }
    }

    override fun clear() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        externalScope.launch {
            try {
                val snapshot = historyCollection(uid).get().await()
                snapshot.documents.forEach { it.reference.delete().await() }
            } catch (e: Exception) {
                Log.e("FirestoreHistory", "No se pudo borrar el historial", e)
            }
        }
    }

    private fun historyCollection(uid: String) =
        firestore.collection("users").document(uid).collection("history")

    private fun com.google.firebase.firestore.DocumentSnapshot.toDiagnosisResult(): DiagnosisResult? {
        val code = getString("code") ?: return null
        val confidence = getDouble("confidence")?.toFloat() ?: return null
        val riskLevel = getString("riskLevel")?.let { name ->
            runCatching { RiskLevel.valueOf(name) }.getOrNull()
        } ?: return null
        return DiagnosisResult(code = code, confidence = confidence, riskLevel = riskLevel)
    }
}
