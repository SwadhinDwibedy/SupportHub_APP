package com.example.supporthub.features.tickets

import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

internal fun buildCreateTicketData(
    ticketId: String,
    payload: TicketSubmissionPayload,
    timestamp: Any,
): HashMap<String, Any?> = hashMapOf(
    "ticketId" to ticketId,
    "employeeUid" to payload.employeeUid,
    "employeeName" to payload.employeeName,
    "employeeEmail" to payload.employeeEmail,
    "workspaceName" to payload.workspaceName,
    "subject" to payload.subject,
    "description" to payload.description,
    "category" to payload.category,
    "priority" to payload.priority,
    "status" to TICKET_STATUS_OPEN,
    "assignedAgentId" to null,
    "assignedAgentName" to null,
    "assignedAgentEmail" to null,
    "assignedAgentRole" to null,
    "assignedAgentDepartment" to null,
    "assignedTimestamp" to null,
    "assignedByAdmin" to null,
    "assignmentStatus" to null,
    "internalNotes" to null,
    "resolutionNotes" to null,
    "createdAt" to timestamp,
    "updatedAt" to timestamp,
    "resolvedAt" to null,
)

class TicketFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) {

    suspend fun createTicket(payload: TicketSubmissionPayload): Result<Unit> {
        return try {
            val document = firestore.collection(FirebaseCollections.TICKETS).document()
            val timestamp = FieldValue.serverTimestamp()
            val data = buildCreateTicketData(
                ticketId = document.id,
                payload = payload,
                timestamp = timestamp,
            )
            document.set(data).await()
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    fun observeEmployeeTickets(employeeUid: String): Flow<List<Ticket>> = callbackFlow {
        val registration = firestore
            .collection(FirebaseCollections.TICKETS)
            .whereEqualTo("employeeUid", employeeUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents
                    .orEmpty()
                    .mapNotNull(::toTicket)
                    .sortedByDescending { it.createdAt?.toString().orEmpty() }

                trySend(tickets)
            }

        awaitClose { registration.remove() }
    }

    fun observeAllTickets(): Flow<List<Ticket>> = callbackFlow {
        val registration = firestore
            .collection(FirebaseCollections.TICKETS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents
                    .orEmpty()
                    .mapNotNull(::toTicket)
                    .sortedByDescending { it.createdAt?.toString().orEmpty() }

                trySend(tickets)
            }

        awaitClose { registration.remove() }
    }

    suspend fun getTicketById(ticketId: String): Result<Ticket> {
        return try {
            val snapshot = firestore
                .collection(FirebaseCollections.TICKETS)
                .document(ticketId)
                .get()
                .await()

            val ticket = toTicket(snapshot)
                ?: return Result.failure(IllegalStateException("Ticket not found"))

            Result.success(ticket)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun updateTicketAssignment(
        ticketId: String,
        assignment: TicketAssignmentUpdate,
    ): Result<Unit> {
        return try {
            Log.d("ASSIGN", "Updating Firestore")
            Log.d("ASSIGN", "Document ID = $ticketId")
            firestore
                .collection(FirebaseCollections.TICKETS)
                .document(ticketId)
                .update(
                    mapOf(
                        "assignedAgentId" to assignment.assignedAgentId,
                        "assignedAgentName" to assignment.assignedAgentName,
                        "assignedAgentEmail" to assignment.assignedAgentEmail,
                        "assignedAgentRole" to assignment.assignedAgentRole,
                        "assignedAgentDepartment" to assignment.assignedAgentDepartment,
                        "assignedTimestamp" to assignment.assignedTimestamp,
                        "assignedByAdmin" to assignment.assignedByAdmin,
                        "assignmentStatus" to assignment.assignmentStatus,
                        "status" to assignment.ticketStatus,
                        "updatedAt" to assignment.updatedAt,
                    )
                )
                .await()
            Log.d("ASSIGN", "Firestore update completed")
            Result.success(Unit)
        } catch (error: Exception) {
            Log.e("ASSIGN", "Firestore Error", error)
            Result.failure(error)
        }
    }

    suspend fun updateTicketDetails(
        ticketId: String,
        updates: Map<String, Any?>,
    ): Result<Unit> {
        return try {
            firestore
                .collection(FirebaseCollections.TICKETS)
                .document(ticketId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun toTicket(document: DocumentSnapshot): Ticket? {
        if (!document.exists()) {
            return null
        }
        return Ticket(
            ticketId = document.getString("ticketId") ?: document.id,
            employeeUid = document.getString("employeeUid").orEmpty(),
            employeeName = document.getString("employeeName").orEmpty(),
            employeeEmail = document.getString("employeeEmail").orEmpty(),
            workspaceName = document.getString("workspaceName").orEmpty(),
            subject = document.getString("subject").orEmpty(),
            description = document.getString("description").orEmpty(),
            category = document.getString("category").orEmpty(),
            priority = document.getString("priority").orEmpty(),
            status = document.getString("status") ?: TICKET_STATUS_OPEN,
            assignedAgentId = document.getString("assignedAgentId"),
            assignedAgentName = document.getString("assignedAgentName"),
            assignedAgentEmail = document.getString("assignedAgentEmail"),
            assignedAgentRole = document.getString("assignedAgentRole"),
            assignedAgentDepartment = document.getString("assignedAgentDepartment"),
            assignedTimestamp = document.get("assignedTimestamp"),
            assignedByAdmin = document.getString("assignedByAdmin"),
            assignmentStatus = document.getString("assignmentStatus"),
            internalNotes = document.getString("internalNotes"),
            resolutionNotes = document.getString("resolutionNotes"),
            createdAt = document.get("createdAt"),
            updatedAt = document.get("updatedAt"),
            resolvedAt = document.get("resolvedAt"),
        )
    }
}

data class TicketAssignmentUpdate(
    val assignedAgentId: String?,
    val assignedAgentName: String?,
    val assignedAgentEmail: String?,
    val assignedAgentRole: String?,
    val assignedAgentDepartment: String?,
    val assignedTimestamp: Any?,
    val assignedByAdmin: String?,
    val assignmentStatus: String?,
    val ticketStatus: String,
    val updatedAt: Any?,
)
