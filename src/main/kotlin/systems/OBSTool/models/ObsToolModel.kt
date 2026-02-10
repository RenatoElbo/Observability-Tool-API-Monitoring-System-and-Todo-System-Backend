package com.marlow.systems.OBSTool.models

import jdk.jfr.Description
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import javax.security.auth.Destroyable

@Serializable
data class PinCode(
    val pinCode: String,
    val moduleId: Int
)

@Serializable
data class AuthToken(
    val bearerToken: String?
)

@Serializable
data class Module(
    val id: Int? = null,
    val moduleTitle: String,
    val moduleType: String,
    val email: String
)

@Serializable
data class ModuleUsage(
    val email: String,
    val roleType: String,
    val moduleTitle: String,
    val server: String,
    val status: String,
    val openedAt: String?
)

@Serializable
data class GetModule(
    val id: Int? = null,
    val moduleTitle: String? = null,
    val description: String? = null,
    val status: String? = null,
    val lastCheck: String? = null,
    val acceptance: String? = null,
    val requestedBy: String
)

@Serializable
data class ModulePending(
    val id: Int,
    val email: String,
    val roleType: String,
    val moduleType: String,
    val moduleTitle: String,
    val requestedAt: String?
)

@Serializable
data class AccountPending(
    val id: Int,
    val username: String,
    val email: String,
    val requestedAt: String?,
    val status: String
)

@Serializable
data class GetModuleTitle(
    val moduleTitle: String? = null,
    val moduleType: String? = null,
)

@Serializable
data class CheckPinExists(
    val isExists: Boolean
)

@Serializable
data class UserPrivilege(
    val userId: Int,
    val email: String,
    val moduleTitle: String,
    val server: String,
    val isAllowed: Boolean,
    val activity: String,
    val access: Int
)

@Serializable
data class DataAPI(
    val moduleId: Int,
    val type: String,
    val method: String,
    val host: String,
    val port: Int,
    val endpoint: String,
    val parameters: String? = null,
    val status: String? = null,
)

@Serializable
data class ApiDetails(
    val apiId: Int,
    val moduleId: Int,
    val key: String,
    val value: String,
    val description: String? = null,
    val sqlFile: String?= null
)

@Serializable
data class ApiTags(
    val apiId: Int,
    val moduleId: Int,
    val key: String
)

@Serializable
data class GetAPIDetails(
    val id: Int,
    val key: String,
    val value: String,
    val description: String?,
)

@Serializable
data class GetAPIDetails1(
    val id: Int,
    val key: String,
    val value: String,
    val description: String?,
    val sqlFile: String?
)

@Serializable
data class GetAPITags(
    val id: Int,
    val key: String
)

@Serializable
data class ModuleID(
    val moduleId: Int
)

@Serializable
data class ImportedWorkspace(
    val moduleId: Int,
    val pType: String,
    val method: String,
    val host: String,
    val port: Int,
    val endpoint: String,
    val parameters: String?,
    val status: String?,
    val body: String?
)

@Serializable
data class DataAPIUpdate(
    val method: String,
    val host: String,
    val port: Int,
    val endpoint: String,
    val parameters: String? = null
)

@Serializable
data class HostName(
    val hostName: String?,
    val port: Int?,
    val username: String
)

@Serializable
data class APIDetailsInfo(
    val id: Int,
    val method: String,
    val endpoint: String,
    val parameters: String?,
    val moduleTitle: String,
    val type: String,
    val detail: String?,
    val description: String?,
    val relatedAPI: String?,
    val relatedModule: String?
)

@Serializable
data class GetDataAPI(
    val id: Int?,
    val moduleId: Int?,
    val method: String?,
    val host: String?,
    val port: Int?,
    val endpoint: String?,
    val parameters: String?,
    val status: String?,
    val responseTime: String?,
    val comments: Int? = null
)

@Serializable
data class SearchDataAPI(
    val id: Int?,
    val moduleId: Int?,
    val method: String?,
    val host: String?,
    val port: Int?,
    val endpoint: String?,
    val parameters: String?,
    val status: String?,
    val responseTime: String?,
    val comments: Int? = null,
    val moduleTitle: String?
)

@Serializable
data class TestApiRequest(
    val url: String,
    val method: String,
    val headers: List<RequestHeader>? = null,
    val body: JsonElement? = null,
)

@Serializable
data class APILog(
    val responseTime: Double,
    val uptime: Double,
    val errorRate: Double,
    val throughput: Int,
    val statusCode: Int,
    val success: Boolean,
    val checkedAt: String
)

@Serializable
data class RequestHeader(
    val key: String,
    val value: String
)

@Serializable
data class UserComments(
    val apiId: Int,
    val moduleId: Int,
    val comments: String,
    val image: String? = null,
    val commentBy: String
)

@Serializable
data class APIUserComments (
    val username: String,
    val comments: String,
    val image: String?,
    val sentAt: String?
)

@Serializable
data class TestApiValidationResponse(
    val success: Boolean,
    val status: Int,
    val body: JsonElement,
    val message: String,
    val time: Long
)

@Serializable
data class GenericResponse<out S>(
    val text: String = "",
    val code: Int = 0,
    val data: S
): Destroyable

@Serializable
data class ModuleStatus(
    val totalRequest: Int? = null,
    val responseTime: Double? = null,
    val uptime: Double? = null,
    val errorRate: Double? = null,
    val throughput: Double? = null,
    val lastCheck: String? = null
)

@Serializable
data class Body(
    val body: String
)

@Serializable
data class ErrorLogs(
    val moduleId: Int,
    val response: JsonElement,
    val responseTime: Double,
    val responseSize: Double,
    val status: String,
    val date: String,
    val url: String
)

@Serializable
data class ErrorLogs2(
    val id: Int,
    val server: String?,
    val moduleName: String?,
    val response: String?,
    val responseTime: Double,
    val responseSize: Double,
    val status: String?,
    val date: String?
)

@Serializable
data class APIStatusCount(
    val running: Int,
    val notRunning: Int
)

@Serializable
data class DashboardMetrics(
    val overallHealth: Double,
    val activeServices: Int,
    val totalRequest: Int,
    val responseTime: Double,
)

//--------Register---------
@Serializable
data class RegistrationRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val ipAddress: String,
    val port: Int,
    val password: String,
    val roleType: String,
    val activity: String
)

@Serializable
data class UserLogin(
    val id: Int?,
    val email: String?,
    val roleType: String?,
    val isValid: Boolean?,
    val approval: Int?
)

@Serializable
data class UserInfo (
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val isValid: Boolean,
    val approval: Int
)

@Serializable
data class AllUserInfo (
    val id: Int,
    val username: String,
    val email: String,
    val roleType: String?,
    val moduleTitle: String?,
    val server: String?,
    val isAllowed: Boolean,
    val isActive: Boolean
)

@Serializable
data class ModuleDescription(
    val description: String?
)

@Serializable
data class UpdateUserInfo(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String?
)

@Serializable
data class HistoryLogs(
    val username: String,
    val email: String,
    val activity: String,
    val date: String,
    val browser: String?
)

@Serializable
data class ViewUserPrivilege (
    val moduleTitle: String,
    val server: String,
    val isAllowed: Boolean
)

@Serializable
data class Notification(
    val id: Int,
    val userId: Int,
    val activity: String,
    val date: String,
    val status: String,
    val accessed: Int?
)

@Serializable
data class UpdateNotif(
    val id: Int?,
    val userId: Int?,
    val status: String
)