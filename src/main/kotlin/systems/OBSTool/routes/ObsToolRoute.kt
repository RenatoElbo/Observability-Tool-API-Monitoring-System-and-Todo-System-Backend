package com.marlow.systems.OBSTool.routes

import com.marlow.configs.Config
import com.marlow.globals.GlobalMethods
import com.marlow.globals.GlobalModel.globalResponse
import com.marlow.globals.GlobalResponse
import com.marlow.systems.OBSTool.controllers.ObstToolController
import com.marlow.systems.OBSTool.models.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.PartData
import io.ktor.http.content.TextContent
import io.ktor.http.content.forEachPart
import io.ktor.http.isSuccess
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.put
import java.io.File


fun Route.obsToolRouting() {
    route("/server-monitoring") {
        val otm = ObstToolController(Config())
        val globalMethod = GlobalMethods()
        val errorCode = 500
        val statusOk = 200

        //============ ROUTES FOR USER SIGN IN, SIGN UP, & SIGN OUT =====================================
        post("/user-registration") {
            try {
                val userInfo = call.receive<RegistrationRequest>()
                val browserInfo = otm.parseBrowser(call.request.headers["User-Agent"] ?: "Unknown")
                val result = otm.accountRegistration(
                    userInfo.username,
                    userInfo.firstName,
                    userInfo.lastName,
                    userInfo.email,
                    userInfo.ipAddress,
                    userInfo.port,
                    userInfo.password,
                    userInfo.roleType,
                    userInfo.activity,
                    browserInfo
                )

                when (result) {
                    -1 -> call.respond(HttpStatusCode.Conflict, globalResponse(409, "User already exists"))
                    1 -> call.respond(HttpStatusCode.OK, globalResponse(statusOk, "User created successfully"))
                    else -> call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, "Unknown error"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message ?: "Server error"))
            }
        }

        get("/user-login/{email}/{pass}") {
            try {
                val emailParam = call.parameters["email"].toString()
                val passParam = call.parameters["pass"].toString()

                val user = otm.chekUserCred(emailParam, passParam)
                return@get call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/logout-user-cred/{id}"){
            try {
                call.parameters["id"]?.also { return@put call.respond(HttpStatusCode.OK, otm.logoutUserCred(it.toInt())) }
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/update-user-info/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val userData = call.receive<UpdateUserInfo>()

                val (username, firstName, lastName, email, password) = userData

                otm.updateUserInfo(
                    id,
                    username,
                    firstName,
                    lastName,
                    email,
                    password
                )

                return@put  call.respond(HttpStatusCode.OK, globalResponse(statusOk, "User Info has been successfully updated"))
            } catch (e: Exception) {
               return@put call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-user-info/{userId}") {
            try {
                call.parameters["userId"]?.also { return@get call.respond(HttpStatusCode.OK, otm.getUserInfo(it.toInt())) }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-user-info") {
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getAllUserInfo())
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }


        //============ ROUTES FOR LOGS =====================================
        post("/insert/apiLog/{api-id}") {
            try {
                val id = Integer.parseInt(call.parameters["api-id"])
                val apiLogData = call.receive<APILog>()

                otm.insertAPILogs(
                    apiIdParam = id,
                    responseTimeParam = apiLogData.responseTime,
                    uptimeParam = apiLogData.uptime,
                    errorRateParam = apiLogData.errorRate,
                    throughputParam = apiLogData.throughput,
                    statusCodeParam = apiLogData.statusCode,
                    successParam = apiLogData.success,
                    checkedAtParam = apiLogData.checkedAt
                )
                return@post call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API Logged Successfully."))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/insert-error-logs") {
            try {
                val errorLogsData = call.receive<ErrorLogs>()
                val (moduleId, response, responseTime, responseSize, status, date, url) = errorLogsData

                otm.insertErrorLogs(moduleId, response, responseTime, responseSize, status, date, url)
                return@post call.respond( HttpStatusCode.OK, globalResponse(statusOk, "Error log inserted successfully") )
            } catch (e: java.lang.Exception){
                return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    globalResponse(errorCode, e.message.toString())
                )
            }
        }

        get("/get-error-logs") {
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getErrorLogsList())
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        post("/insert-user-audit/{userId}/{activity}"){
            try {
                val userId = Integer.parseInt(call.parameters["userId"])
                val browserInfo = otm.parseBrowser(call.request.headers["User-Agent"] ?: "Unknown")
                val activity = call.parameters["activity"].toString()
                otm.insertAudit(userId, browserInfo, activity)
                return@post call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Audit Inserted!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-history-logs/{userId}") {
            try {
                call.parameters["userId"]?.also { return@get call.respond(HttpStatusCode.OK, otm.getAuditLogsById(it.toInt())) }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }


        //============ ROUTES FOR DASHBOARD =====================================
        get("/get-average-response-time"){
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getAverageResponseTime())
            } catch (e: Exception){
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/module-status/{module-id}") {
            try {
                val moduleId = call.parameters["module-id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid module ID")

                val status = otm.getModuleStatus(moduleId)

                if (status != null) {
                    return@get call.respond(HttpStatusCode.OK, status)
                } else {
                    return@get call.respond(HttpStatusCode.NotFound, globalResponse(404, "Not Found"))
                }
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.InternalServerError,
                    globalResponse(errorCode, e.message.toString())
                )
            }
        }

        get("/get-api-body/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Invalid ID"))
                    return@get
                }

                val body = otm.getAPIBodyById(id)
                if (body != null) {
                    return@get call.respondText(body, ContentType.Application.Json)
                } else {
                    return@get call.respond(HttpStatusCode.NotFound, globalResponse(404, "API not found"))
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-api-status-count") {
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getAPIStatusCount())
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-dashboard-metrics") {
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getDashboardMetrics())
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-host-names"){
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getHostName())
            } catch (e: Exception){
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-total-req/{moduleId}"){
            try {
                call.parameters["moduleId"]?.also {return@get call.respond(HttpStatusCode.OK, otm.checkTotalReqByModuleId(it.toInt()))}
            } catch (e: Exception){
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-total-req"){
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getTotalRequest())
            } catch (e: Exception){
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        //============ ROUTES FOR REQUESTS =====================================
        get("/get-all-modules-pending") {
            try {
                val modules = otm.getAllModulePending()
                if (modules.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, emptyList<ModulePending>())
                } else {
                    return@get call.respond(HttpStatusCode.OK, modules)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-modules-delete-pending") {
            try {
                val modules = otm.getAllModuleDeletePending()
                if (modules.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, emptyList<ModulePending>())
                } else {
                    return@get call.respond(HttpStatusCode.OK, modules)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/update-module-request/{id}/{num}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val accept = call.parameters["num"].toString()
                otm.updateModuleRequest(id, accept)
                return@put call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Update Successfully"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/insert-request-logs/{email}/{activity}"){
            try {
                val email = call.parameters["email"].toString()
                val browserInfo = otm.parseBrowser(call.request.headers["User-Agent"] ?: "Unknown")
                val activity = call.parameters["activity"].toString()
                otm.insertAuditForRequest(email, browserInfo, activity)
                return@post call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Audit Inserted!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        delete("/delete-module-data/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val result = otm.deleteModuleData(id)
                if (result == 0)
                    return@delete call.respond(HttpStatusCode.InternalServerError, globalResponse(400, "Failed to delete record"))

                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Module deleted successfully"))
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        get("/get-all-account-pending") {
            try {
                val accounts = otm.getAllAccountPending()
                if (accounts.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, emptyList<AccountPending>())
                } else {
                    return@get call.respond(HttpStatusCode.OK, accounts)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/update-account-pending/{id}/{status}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val accept = call.parameters["status"].toString()
                otm.updateAccountPending(id, accept)
                return@put call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Update Successfully"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/insert-imported-workspace") {
            try {
                val workspace = call.receive<ImportedWorkspace>()

                val result = otm.insertImportedWorkspace(
                    moduleId = workspace.moduleId,
                    ptype = workspace.pType,
                    methodParam = workspace.method,
                    hostParam = workspace.host,
                    portParam = workspace.port,
                    endpoint = workspace.endpoint,
                    parameters = workspace.parameters,
                    statusParam = workspace.status,
                    bodyParam = workspace.body ?: "{\"key\":\"value\"}"
                )

                if (result < 0) {
                    return@post call.respond(
                        HttpStatusCode.OK,
                        globalResponse(400, "Record insertion failed")
                    )
                }

                return@post call.respond(
                    HttpStatusCode.OK,
                    globalResponse(200, "Imported workspace has been successfully added")
                )

            } catch (e: Exception) {
                return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    globalResponse(500, e.message.toString())
                )
            }
        }


        //============ ROUTES FOR MODULE =====================================
        post("/add-module-title") {
            try {
                val moduleItem = call.receive<Module>()

                if (moduleItem.moduleTitle.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Missing module title"))
                }

                val moduleId = otm.addModuleTitle(moduleItem.moduleTitle, moduleItem.moduleType, moduleItem.email)
                if (moduleId == null) {
                    return@post call.respond(HttpStatusCode.InternalServerError, "Failed to insert module")
                }
                return@post call.respond(HttpStatusCode.OK, moduleId)
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        get("/get-all-modules/{module-type}/{id}") {
            try {
                val moduleType = call.parameters["module-type"].toString()
                val userId = Integer.parseInt(call.parameters["id"])
                val modules = otm.getAllModulesByType(moduleType, userId)
                if (modules.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, emptyList<GetModule>())
                } else {
                    return@get call.respond(HttpStatusCode.OK, modules)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-modules-title") {
            try {
                val modules = otm.getAllModulesTitle()
                if (modules.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, emptyList<GetModule>())
                } else {
                    return@get call.respond(HttpStatusCode.OK, modules)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/update-module-health/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                return@put call.respond(HttpStatusCode.OK, otm.updateModuleHealth(id))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/delete-module-request/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val result = otm.deleteModuleForRequest(id)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Module deleted successfully"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/update-module-title/{id}/{title}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val title = call.parameters["title"].toString()
                val result = otm.updateModuleTitle(id, title)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Module update successfully"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/update-module-description/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val module = call.receive<ModuleDescription>()
                otm.updateModuleDescription(id, module.description)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Module update successfully"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        //============ ROUTES FOR OTHERS =====================================
        post("/add-user-privilege") {
            try {
                val userData = call.receive<UserPrivilege>()
                val browserInfo = otm.parseBrowser(call.request.headers["User-Agent"] ?: "Unknown")

                if (userData.moduleTitle.isBlank() || userData.email.isBlank() || userData.server.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Missing Field, please complete all fields."))
                }

                otm.insertUserPrivilege(userData.userId, userData.email, userData.moduleTitle, userData.server, userData.isAllowed, userData.activity, userData.access, browserInfo)
                return@post call.respond(HttpStatusCode.OK, globalResponse(statusOk, "User is now have access to this Module: ${userData.moduleTitle}."))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        get("/get-user-privilege/{userId}") {
            try {
                val userId = Integer.parseInt(call.parameters["userId"])
                val modulesAllowed = otm.getUserPrivilegeById(userId)
                return@get call.respond(HttpStatusCode.OK, modulesAllowed)
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-notif-info/{userId}") {
            try {
                val userId = Integer.parseInt(call.parameters["userId"])
                return@get call.respond(HttpStatusCode.OK, otm.getNotifInfo(userId))
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/update-notif-status") {
            try {
                val notif = call.receive<UpdateNotif>()
                otm.updateNotifStatus(notif.id, notif.userId,notif.status)
                return@put call.respond(HttpStatusCode.OK, globalResponse(statusOk, "notif updated"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/insert-module-logs/{userId}/{moduleId}/{status}") {
            try {
                val userId = Integer.parseInt(call.parameters["userId"])
                val moduleId = Integer.parseInt(call.parameters["moduleId"])
                val status = call.parameters["status"].toString()
                otm.insertModuleLogs(userId, moduleId, status)
                return@put call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Module Logs updated"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        get("/get-module-usage-logs") {
            try {
                return@get call.respond(HttpStatusCode.OK, otm.getAllModuleUsage())
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        //============ ROUTES FOR API DETAILS =====================================
        post("/add-api") {
            try {
                val apiData = call.receive<DataAPI>()
                val (moduleId, type, method, host, port, endpoint, parameters, status) = apiData

                if (type.isBlank() || method.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        globalResponse(400, "Missing required fields")
                    )
                }

                val res = otm.insertAPI(
                    moduleIdParam = moduleId,
                    typeParam = type,
                    methodParam = method,
                    hostParam = host,
                    portParam = port,
                    endpointParam = endpoint,
                    parametersParam = parameters,
                    statusParam = status
                )

                if (res < 0) {
                    return@post call.respond(
                        HttpStatusCode.OK,
                        globalResponse(400, "Record insertion failed")
                    )
                }

                return@post call.respond(
                    HttpStatusCode.OK,
                    globalResponse(statusOk, "API has been successfully added")
                )
            } catch (e: Exception) {
                return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    globalResponse(errorCode, e.message.toString())
                )
            }
        }

        get("/get-all-apis/{module-id}/{type}") {
            try {
                val moduleId = Integer.parseInt(call.parameters["module-id"])
                val type = call.parameters["type"] ?: return@get call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Specify Type"))

                val apiData = otm.getAllAPIData(moduleId, type)
                if (apiData.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, globalResponse(statusOk, "No apiData found"))
                } else {
                    return@get call.respond(HttpStatusCode.OK, apiData)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-apis/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])

                val apiData = otm.getAllAPIDataByID(id)
                if (apiData.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, globalResponse(statusOk, "No apiData found"))
                } else {
                    return@get call.respond(HttpStatusCode.OK, apiData)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/search-all-apis/{key}") {
            try {
                val type = call.parameters["key"].toString()
                val apiData = otm.searchAPIData(type)
                if (apiData.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, globalResponse(statusOk, "No apiData found"))
                } else {
                    return@get call.respond(HttpStatusCode.OK, apiData)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-api-details/{api-id}") {
            try {
                val id = Integer.parseInt(call.parameters["api-id"])
                val apiData = otm.getAllAPIDetails(id)
                if (apiData.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, globalResponse(statusOk, "No apiData found"))
                } else {
                    return@get call.respond(HttpStatusCode.OK, apiData)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-api-sql-file-details/{fileName}") {
            try {
                val fileName = call.parameters["fileName"]
                if (fileName.isNullOrBlank()) {
                    return@get call.respond(HttpStatusCode.BadRequest, "File name is missing")
                }

                val filePath = "image_uploads/sql_file/$fileName"
                val file = File(filePath)

                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message ?: "Internal server error"))
            }
        }

        get("/get-all-api-params/{api-id}") {
            try {
                val id = Integer.parseInt(call.parameters["api-id"])
                val apiData = otm.getAllAPIParams(id)
                if (apiData.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, globalResponse(statusOk, "No apiData found"))
                } else {
                    return@get call.respond(HttpStatusCode.OK, apiData)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/get-all-api-headers/{api-id}") {
            try {
                val id = Integer.parseInt(call.parameters["api-id"])
                val apiData = otm.getAllAPIHeader(id)
                if (apiData.isEmpty()) {
                    return@get call.respond(HttpStatusCode.OK, globalResponse(statusOk, "No apiData found"))
                } else {
                    return@get call.respond(HttpStatusCode.OK, apiData)
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        post("/insert-api-details") {
            try {
                val multipart = call.receiveMultipart()
                var sqlFileName: String? = null
                var apiDetails: ApiDetails? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (part.originalFileName?.endsWith(".sql") == true) {
                                sqlFileName = globalMethod.saveSqlFile(part)
                            }
                        }
                        is PartData.FormItem -> {
                            if (part.name == "apiDetails") {
                                apiDetails = kotlinx.serialization.json.Json.decodeFromString(ApiDetails.serializer(), part.value)
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (apiDetails != null) {
                    otm.insertAPIDetails(
                        apiDetails!!.apiId,
                        apiDetails!!.moduleId,
                        apiDetails!!.key,
                        apiDetails!!.value,
                        apiDetails!!.description ?: "",
                        sqlFileName ?: ""
                    )

                    call.respond(
                        HttpStatusCode.OK,
                        globalResponse(statusOk, "API Details added Successfully")
                    )
                } else {
                    call.respond(HttpStatusCode.BadRequest, "API details not provided")
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
            }
        }

        post("/insert-api-params") {
            try {
                val apiDetails = call.receive<ApiDetails>()
                otm.insertAPIParams(apiDetails.apiId, apiDetails.moduleId, apiDetails.key, apiDetails.value, apiDetails.description)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API Params added Successfully"))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/insert-api-headers") {
            try {
                val apiDetails = call.receive<ApiTags>()
                otm.insertAPIHeader(apiDetails.apiId, apiDetails.moduleId, apiDetails.key)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API Header added Successfully"))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/hash-code") {
            try {
                val code = call.receive<PinCode>()
                if (code.pinCode.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Pin is Require!")
                }

                otm.addHashPin(code.pinCode, code.moduleId)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Pin hashed Successfully"))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/auth-token"){
            try {
                val auth = call.receive<AuthToken>()
                val token = auth.bearerToken
                if (token.isNullOrBlank()){
                    return@post call.respond(HttpStatusCode.BadRequest, "Authorization Token is Require!")
                }
                otm.insertOrUpdateAuth(token)
                return@post call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Token Saved."))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/auth-token/logout"){
            try {
                otm.insertOrUpdateAuth(null)
                return@post call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Token Logged Out."))
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        post("/insert-user-comment") {
            try {
                var apiId: Int? = null
                var moduleId: Int? = null
                var comments: String? = null
                var commentBy: String? = null
                var uploadedFileName: String? = null
                var imageError: String? = null
                var activity: String? = null
                var userId: Int? = null
                val browserInfo = otm.parseBrowser(call.request.headers["User-Agent"] ?: "Unknown")


                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> when (part.name) {
                            "apiId" -> apiId = part.value.toInt()
                            "moduleId" -> moduleId = part.value.toInt()
                            "comments" -> comments = part.value
                            "commentBy" -> commentBy = part.value
                            "activity" -> activity = part.value
                            "userId" -> userId = part.value.toInt()
                        }
                        is PartData.FileItem -> if (part.name == "image") {
                            try {
                                uploadedFileName = globalMethod.saveImage(part)
                            } catch (e: Exception) {
                                imageError = "Invalid image format."
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (imageError != null)
                    return@post call.respond(HttpStatusCode.BadRequest, GlobalResponse(400, false, imageError))

                if (apiId == null || moduleId == null || comments == null || commentBy == null)
                    return@post call.respond(HttpStatusCode.BadRequest, GlobalResponse(400, false, "Missing required fields."))

                otm.insertAPIComment(apiId!!, moduleId!!, comments!!, uploadedFileName, commentBy!!, activity!!, browserInfo, userId!!)
                call.respond(HttpStatusCode.OK, GlobalResponse(200, true, "Comment inserted successfully."))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, GlobalResponse(500, false, e.message ?: "Internal server error"))
            }
        }

        get("/get-api-comments/{id}/{moduleId}") {
            try {
                val apiId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val moduleId = call.parameters["moduleId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                val comments = otm.getAllAPIComments(apiId, moduleId)
                call.respond(comments)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(500, e.message.toString()))
            }
        }

        get("/get-api-all-details") {
            try {
                val comments = otm.getAllAPIDetailsInfo()
                call.respond(HttpStatusCode.OK, comments)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(500, e.message.toString()))
            }
        }

        get("/get-api-comment/{image-name}") {
            try {
                val imageName = call.parameters["image-name"].toString()
                val errorImageUrl = "image_uploads/general-unavailable-image.webp"

                val filePath = "image_uploads/$imageName"
                val file = File(filePath)

                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respondFile(File(errorImageUrl))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        get("/check-hash-code/{pin}/{moduleId}") {
            try {
                val pin = call.parameters["pin"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Specify PIN"))
                val moduleId = Integer.parseInt(call.parameters["moduleId"])

                val exists = otm.verifyPinCode(pin, moduleId)
                if (exists) {
                    otm.setPinActive(pin, true)
                    return@get call.respond(HttpStatusCode.OK, mapOf("valid" to true))
                } else {
                    return@get call.respond(HttpStatusCode.OK, mapOf("valid" to false))
                }
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/check-pin-exists/{moduleId}") {
            try {
                val moduleId = Integer.parseInt(call.parameters["moduleId"])

                val exists = otm.checkIsExists(moduleId)
                return@get call.respond(HttpStatusCode.OK, exists)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        get("/logout-pin/{pin}") {
            try {
                val pin = call.parameters["pin"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Specify PIN"))

                otm.setPinActive(pin, false)
                return@get call.respond(HttpStatusCode.OK, mapOf("logout" to true))
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        post("/execute-api-test/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val request = call.receive<TestApiRequest>()
                val client = HttpClient()

                val response: HttpResponse = client.request(request.url) {
                    method = HttpMethod.parse(request.method)
                    request.headers?.forEach { header ->
                        headers.append(header.key, header.value)
                    }
                    if (request.method.uppercase() != "GET" && request.body != null) {
                        setBody(TextContent(request.body.toString(), ContentType.Application.Json))
                    }
                }

                val responseText = response.bodyAsText()
                val responseJson: JsonElement = try {
                    Json.parseToJsonElement(responseText)
                } catch (e: Exception) {
                    Json.encodeToJsonElement(responseText)
                }

                val success = response.status.isSuccess()
                otm.updateLastCheck(id)

                call.respond(
                    HttpStatusCode.OK,
                    TestApiValidationResponse(
                        success = success,
                        status = response.status.value,
                        body = responseJson,
                        message = if (success) "API call successful"
                        else "API call failed with status ${response.status.value}",
                        time = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    TestApiValidationResponse(
                        success = false,
                        status = errorCode,
                        body = Json.encodeToJsonElement(""),
                        message = "Error: ${e.message}",
                        time = System.currentTimeMillis()
                    )
                )
            }
        }

        delete("/delete-api-data/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                otm.deleteAPIData(id)

                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API deleted successfully"))
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        delete("/delete-api-detail/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val result = otm.deleteAPIDetail(id)
                if (result == 0)
                    return@delete call.respond(HttpStatusCode.InternalServerError, globalResponse(400, "Failed to delete record"))

                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API detail deleted successfully"))
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        delete("/delete-api-params/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val result = otm.deleteAPIParams(id)
                if (result == 0)
                    return@delete call.respond(HttpStatusCode.InternalServerError, globalResponse(400, "Failed to delete record"))

                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API Params deleted successfully"))
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        delete("/delete-api-headers/{id}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val result = otm.deleteAPIHeader(id)
                if (result == 0)
                    return@delete call.respond(HttpStatusCode.InternalServerError, globalResponse(400, "Failed to delete record"))

                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API Header deleted successfully"))
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/update-api-data/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                val apiData = call.receive<DataAPIUpdate>()

                if (id == null) {
                    return@put call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Invalid or missing API id"))
                }

                val (method, host, port, endpoint, parameters) = apiData

                val res = otm.updateAPIData(
                    id = id,
                    method = method,
                    host = host,
                    port = port,
                    endpoint = endpoint,
                    parameters = parameters
                )

                if (res < 0) {
                    call.respond(HttpStatusCode.OK, globalResponse(400, "API update success"))
                } else {
                    call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API has been successfully updated"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, globalResponse(errorCode, e.message.toString()))
            }
        }

        put("/update-status/{id}/{status}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                val status = call.parameters["status"]

                if (id == null || status.isNullOrBlank()) {
                    return@put call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Invalid id or status"))
                }

                val updatedRows = otm.updateAPIStatus(id, status)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Status updated!"))
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/update-module-api-host/{id}/{host}") {
            try {
                val id = Integer.parseInt(call.parameters["id"])
                val host = call.parameters["host"].toString()

                otm.updateAPIHost(id, host)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "API Host Updated!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

        put("/update-api-body/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                val body = call.receive<Body>()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, globalResponse(400, "Invalid ID"))
                    return@put
                }

                val updatedRows = otm.updateAPIBody(id, body.body)
                call.respond(HttpStatusCode.OK, globalResponse(statusOk, "Body request updated!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

    }
}

