package com.marlow.systems.OBSTool.controllers

import com.marlow.configs.Config
import com.marlow.systems.OBSTool.models.*
import com.marlow.systems.OBSTool.queries.*
import com.marlow.systems.OBSTool.security.sha512
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.use

class ObstToolController (private val ds: Config) {

    //============ CONTROLLERS FOR USER SIGN IN, SIGN UP, & SIGN OUT =====================================
    fun accountRegistration(
        usernameParam: String,
        firstNameParam: String,
        lastNameParam: String,
        emailParam: String,
        ipAddressParam: String,
        portParam: Int,
        passwordParam: String,
        roleTypeParam: String,
        activityParam: String,
        browserInfo: String,
    ): Int {
        val access = 0
        return ds.connect().use { connection ->
            connection.prepareStatement(INSERT_INFORMATION).use {
                it.setString(1, usernameParam)
                it.setString(2, firstNameParam)
                it.setString(3, lastNameParam)
                it.setString(4, emailParam)
                it.setString(5, ipAddressParam)
                it.setInt(6, portParam)
                it.setString(7, passwordParam.sha512())
                it.setString(8, roleTypeParam)
                it.setObject(9, OffsetDateTime.now())
                it.setString(10, activityParam)
                it.setString(11, browserInfo)
                it.setString(12, "new")
                it.setInt(13, access)
                val rs = it.executeQuery()
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    fun chekUserCred(emailParam: String, passwordParam: String): MutableList<UserLogin> {
        val userData = mutableListOf<UserLogin>()
        ds.connect().use { connection ->
            connection.prepareStatement(CHECK_USER_CREDENTIAL).also {
                it.setString(1, emailParam)
                it.setString(2, passwordParam.sha512())
            }.executeQuery().use { data ->
                while (data.next()){
                    val id = data.getInt("id")
                    val email = data.getString("email")
                    val roleType = data.getString("role_type")
                    val isValid = data.getBoolean("is_valid")
                    val approval = data.getInt("approval")
                    userData.add(UserLogin(id,email, roleType, isValid, approval))
                }
            }
            return userData
        }
    }

    fun logoutUserCred(idParam: Int): Int {
        return ds.connect().use { connection ->
            connection.prepareStatement(LOGOUT_USER_CRED).also {
                it.setInt(1, idParam)
            }.executeUpdate()
        }
    }

    fun updateUserInfo(
        idParam: Int,
        usernameParam: String,
        firstNameParam: String,
        lastNameParam: String,
        emailParam: String,
        passwordParam: String?
    ): Int {
        return ds.connect().use { con ->
            if (passwordParam.isNullOrBlank()){
                con.prepareStatement(UPDATE_USER_INFO_NO_PASS).also {
                    it.setInt(1, idParam)
                    it.setString(2, usernameParam)
                    it.setString(3, firstNameParam)
                    it.setString(4, lastNameParam)
                    it.setString(5, emailParam)
                }.executeUpdate()
            } else {
                con.prepareStatement(UPDATE_USER_INFO).also {
                    it.setInt(1, idParam)
                    it.setString(2, usernameParam)
                    it.setString(3, firstNameParam)
                    it.setString(4, lastNameParam)
                    it.setString(5, emailParam)
                    it.setString(6, passwordParam.sha512())
                }.executeUpdate()
            }
        }
    }

    fun getUserInfo(idParam: Int): MutableList<UserInfo> {
        val userData = mutableListOf<UserInfo>()
        ds.connect().use { connection ->
            connection.prepareStatement(GET_USER_INFO).also {
                it.setInt(1, idParam)
            }.executeQuery().use { data ->
                while (data.next()){
                    val username = data.getString("username")
                    val firstName = data.getString("first_name")
                    val lastName = data.getString("last_name")
                    val email = data.getString("email")
                    val isValid = data.getBoolean("is_valid")
                    val approval = data.getInt("approval_status")
                    userData.add(UserInfo(username, firstName, lastName, email, isValid, approval))
                }
            }
            return userData
        }
    }

    fun getAllUserInfo(): MutableList<AllUserInfo> {
        val userData = mutableListOf<AllUserInfo>()
        ds.connect().use { connection ->
            connection.prepareStatement(GET_ALL_USER_INFO).executeQuery().use { data ->
                while (data.next()){
                    val id = data.getInt("id")
                    val username = data.getString("username")
                    val email = data.getString("email")
                    val roleType = data.getString("role_type")
                    val moduleTitle = data.getString("module_title") ?: ""
                    val server = data.getString("server") ?: ""
                    val isAllowed = data.getBoolean("is_allowed")
                    val isActive = data.getBoolean("is_active")
                    userData.add(AllUserInfo(id,username, email, roleType, moduleTitle, server, isAllowed,isActive))
                }
            }
            return userData
        }
    }

    //============ CONTROLLERS FOR LOGS =====================================
    fun insertAPILogs(
        apiIdParam: Int,
        responseTimeParam: Double,
        uptimeParam: Double,
        errorRateParam: Double,
        throughputParam: Int,
        statusCodeParam: Int,
        successParam: Boolean,
        checkedAtParam: String
    ): Int {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_API_LOG).also {
                it.setInt(1, apiIdParam)
                it.setDouble(2, responseTimeParam)
                it.setDouble(3, uptimeParam)
                it.setDouble(4, errorRateParam)
                it.setInt(5, throughputParam)
                it.setInt(6, statusCodeParam)
                it.setBoolean(7, successParam)
                it.setString(8, checkedAtParam)
            }.executeUpdate()
        }
    }

    fun insertErrorLogs(
        moduleIdParam: Int,
        responseParam: JsonElement,
        responseTimeParam: Double,
        responseSizeParam: Double,
        statusParam: String,
        dateParam: String,
        urlParam: String
    ): Int {
        val jsonString = Json.encodeToString(JsonObject.serializer(), responseParam.jsonObject)

        return ds.connect().use { con ->
            con.prepareStatement(INSERT_ERROR_LOGS).also {
                val pgObject = org.postgresql.util.PGobject().apply {
                    type = "json"
                    value = jsonString
                }
                val instant = java.time.Instant.parse(dateParam)
                val timestamp = java.sql.Timestamp.from(instant)

                it.setInt(1, moduleIdParam)
                it.setObject(2, pgObject)
                it.setDouble(3, responseTimeParam)
                it.setDouble(4, responseSizeParam)
                it.setString(5, statusParam)
                it.setTimestamp(6, timestamp)
                it.setString(7, urlParam)
            }.executeUpdate()
        }
    }

    fun getErrorLogsList(): MutableList<ErrorLogs2> {
        val res = mutableListOf<ErrorLogs2>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ERROR_LOGS).use { query ->
                query.executeQuery().use { rs ->
                    while (rs.next()) {
                        val id = rs.getInt("id")
                        val server = rs.getString("server")
                        val moduleName = rs.getString("module_name")
                        val response = rs.getString("response")
                        val responseTime = rs.getDouble("response_time")
                        val responseSize = rs.getDouble("response_size")
                        val status = rs.getString("status")
                        val date = rs.getString("date")

                        res.add(
                            ErrorLogs2(
                                id = id,
                                server = server,
                                moduleName = moduleName,
                                response = response,
                                responseTime = responseTime,
                                responseSize = responseSize,
                                status = status,
                                date = date
                            )
                        )
                    }
                }
            }
        }
        return res
    }

    fun insertAudit(userIdParam: Int, browserInfo: String, activityParam: String) {
        ds.connect().use { con ->
            con.prepareStatement(INSERT_AUDIT_LOGS).also {
                it.setInt(1, userIdParam)
                it.setObject(2, OffsetDateTime.now())
                it.setString(3, browserInfo)
                it.setString(4, activityParam)
            }.executeUpdate()
        }
    }

    fun getAuditLogsById(idParam: Int): MutableList<HistoryLogs> {
        val userData = mutableListOf<HistoryLogs>()
        ds.connect().use { connection ->
            connection.prepareStatement(GET_ALL_AUDIT_LOGS).also {
                it.setInt(1, idParam)
            }.executeQuery().use { data ->
                while (data.next()){
                    val username = data.getString("username")
                    val email = data.getString("email")
                    val date = data.getString("audit_at")
                    val activity = data.getString("activity")
                    val browser = data.getString("browser")
                    userData.add(HistoryLogs(username, email, activity, date, browser))
                }
            }
            return userData
        }
    }

    fun parseBrowser(userAgent: String): String {
        val ua = userAgent.lowercase()
        return when {
            ua.contains("edg") -> "Edge"
            ua.contains("opr") || ua.contains("opera") -> "Opera"
            ua.contains("chrome") && !ua.contains("edg") -> "Chrome"
            ua.contains("firefox") -> "Firefox"
            ua.contains("safari") && !ua.contains("chrome") -> "Safari"
            ua.contains("brave") -> "Brave"
            ua.contains("msie") || ua.contains("trident") -> "Internet Explorer"
            else -> "Unknown"
        }
    }

    //============ CONTROLLERS FOR DASHBOARD =====================================
    fun getAverageResponseTime(): Int {
        return ds.connect().use { con ->
            con.prepareStatement(GET_AVERAGE_RTIME).use { query ->
                val res = query.executeQuery()
                res.next()
                res.getInt(1)
            }
        }
    }

    fun getModuleStatus(moduleIdParam: Int): ModuleStatus? {
        return ds.connect().use { con ->
            con.prepareStatement(GET_MODULE_STATUS).use { query ->
                query.setInt(1, moduleIdParam)
                query.executeQuery().use { rs ->
                    if (rs.next()) {
                        val totalRequests = rs.getInt("total_requests")
                        val avgResponseTime = rs.getDouble("avg_response_time")
                        val uptime = rs.getDouble("uptime")
                        val errorRate = rs.getDouble("error_rate")
                        val throughput = rs.getDouble("throughput")
                        val lastCheck = rs.getString("last_check")

                        ModuleStatus(
                            totalRequests,
                            avgResponseTime,
                            uptime,
                            errorRate,
                            throughput,
                            lastCheck
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun getAPIBodyById(idParam: Int): String? {
        ds.connect().use { con ->
            con.prepareStatement(GET_API_BODY_BY_ID).use { query ->
                query.setInt(1, idParam)
                val res = query.executeQuery()
                return if (res.next()) res.getString(1) else null
            }
        }
    }

    fun getAPIStatusCount(): MutableList<APIStatusCount> {
        val res = mutableListOf<APIStatusCount>()
        ds.connect().use { con ->
            con.prepareStatement(GET_API_STATUS_COUNT).use { query ->
                query.executeQuery().use { rs ->
                    while (rs.next()){
                        val running = rs.getInt("running_count")
                        val notRunning = rs.getInt("not_running_count")

                        res.add(APIStatusCount(running, notRunning))
                    }
                }
            }
        }
        return res
    }

    fun getDashboardMetrics(): MutableList<DashboardMetrics> {
        val res = mutableListOf<DashboardMetrics>()
        ds.connect().use { con ->
            con.prepareStatement(GET_DASHBOARD_METRICS).use { query ->
                query.executeQuery().use { rs ->
                    while (rs.next()){
                        val overallHealth = rs.getDouble("overall_health")
                        val activeServices = rs.getInt("active_services")
                        val totalRequest = rs.getInt("total_requests")
                        val responseTime = rs.getDouble("avg_response_time")

                        res.add(DashboardMetrics(overallHealth, activeServices, totalRequest, responseTime))
                    }
                }
            }
        }
        return res
    }

    fun getHostName(): MutableList<HostName> {
        val hostNames = mutableListOf<HostName>()
        ds.connect().use { con ->
            con.prepareStatement(GET_HOST_NAME).use { query ->
                query.executeQuery().use { rs ->
                    while (rs.next()){
                        hostNames.add(HostName(rs.getString("ip_address"), rs.getInt("port"), rs.getString("username")))
                    }
                }
            }
        }
        return hostNames
    }

    fun checkTotalReqByModuleId(moduleIdParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(GET_TOTAL_REQUEST).use { query ->
                query.setInt(1, moduleIdParam)
                val res = query.executeQuery()
                res.next()
                res.getInt(1)
            }
        }
    }

    fun getTotalRequest(): Int {
        return ds.connect().use { con ->
            con.prepareStatement(GET_ALL_TOTAL_REQUEST).use { query ->
                val res = query.executeQuery()
                res.next()
                res.getInt(1)
            }
        }
    }

    //============ CONTROLLERS FOR REQUEST =====================================
    fun getAllModulePending(): MutableList<ModulePending> {
        val res = mutableListOf<ModulePending>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_MODULES_PENDING).executeQuery().use { data ->
                    while (data.next()) {
                        val id = data.getInt("id")
                        val moduleTitle = data.getString("title")
                        val email = data.getString("email")
                        val roleType = data.getString("role_type")
                        val moduleType = data.getString("module_type")
                        val lastCheckTimestamp = data.getTimestamp("created_at")
                        val requestedAt = lastCheckTimestamp?.toLocalDateTime()
                            ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mma"))
                        res.add(
                            ModulePending(
                                id,
                                email,
                                roleType,
                                moduleType,
                                moduleTitle,
                                requestedAt
                            )
                        )
                    }
            }
        }
        return res
    }

    fun getAllModuleDeletePending(): MutableList<ModulePending> {
        val res = mutableListOf<ModulePending>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_MODULES_DELETE_REQUEST).executeQuery().use { data ->
                while (data.next()) {
                    val id = data.getInt("id")
                    val moduleTitle = data.getString("title")
                    val email = data.getString("email")
                    val roleType = data.getString("role_type")
                    val moduleType = data.getString("module_type")
                    val lastCheckTimestamp = data.getTimestamp("created_at")
                    val requestedAt = lastCheckTimestamp?.toLocalDateTime()
                        ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mma"))
                    res.add(
                        ModulePending(
                            id,
                            email,
                            roleType,
                            moduleType,
                            moduleTitle,
                            requestedAt
                        )
                    )
                }
            }
        }
        return res
    }

    fun updateModuleRequest(idParam: Int, acceptParam: String): Int {
        return ds.connect().use { con ->
            con.prepareStatement(UPDATE_MODULE_REQUEST).also {
                it.setInt(1, idParam)
                it.setString(2, acceptParam)
            }.executeUpdate()
        }
    }

    fun insertAuditForRequest(emailPara: String, browserInfo: String, activityParam: String) {
        ds.connect().use { con ->
            con.prepareStatement(INSERT_REQUESTS_LOGS).also {
                it.setString(1, emailPara)
                it.setObject(2, OffsetDateTime.now())
                it.setString(3, browserInfo)
                it.setString(4, activityParam)
            }.executeUpdate()
        }
    }

    fun deleteModuleData(idParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(DELETE_MODULE_DATA).use { query ->
                query.setInt(1, idParam)
                val rowsAffected = query.executeQuery()
                if (rowsAffected.next()) rowsAffected.getInt(1) else 0
            }
        }
    }

    fun getAllAccountPending(): MutableList<AccountPending> {
        val res = mutableListOf<AccountPending>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_ACCOUNT_PENDING).executeQuery().use { data ->
                while (data.next()) {
                    val id = data.getInt("id")
                    val username = data.getString("username")
                    val email = data.getString("email")
                    val status = data.getString("role_type")
                    val lastCheckTimestamp = data.getTimestamp("created_at")
                    val requestedAt = lastCheckTimestamp?.toLocalDateTime()
                        ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mma"))
                    res.add(
                        AccountPending(
                            id,
                            username,
                            email,
                            requestedAt,
                            status
                        )
                    )
                }
            }
        }
        return res
    }

    fun updateAccountPending(idParam: Int, statusParam: String): Int {
        return ds.connect().use { con ->
            con.prepareStatement(UPDATE_ACCOUNT_PENDING).also {
                it.setInt(1, idParam)
                it.setString(2, statusParam)
            }.executeUpdate()
        }
    }

    fun insertImportedWorkspace(
        moduleId: Int,
        ptype: String,
        methodParam: String,
        hostParam: String,
        portParam: Int,
        endpoint: String,
        parameters: String?,
        statusParam: String?,
        bodyParam: String
    ): Int {
        val jsonElement = Json.parseToJsonElement(bodyParam)
        val jsonString = Json.encodeToString(JsonObject.serializer(), jsonElement.jsonObject)
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_IMPORTED_WORKSPACE).also {
                it.setInt(1, moduleId)
                it.setString(2, ptype)
                it.setString(3, methodParam)
                it.setString(4, hostParam)
                it.setInt(5, portParam)
                it.setString(6, endpoint)
                it.setString(7, parameters)
                it.setString(8, statusParam)
                it.setObject(9, jsonString, java.sql.Types.OTHER)
            }.executeUpdate()
        }
    }


    //============ CONTROLLERS FOR MODULE =====================================
    fun addModuleTitle(moduleTitleParam: String, moduleTypeParam: String, emailParam: String): ModuleID? {
        var moduleId: ModuleID? = null
        ds.connect().use { con ->
            con.prepareStatement(INSERT_MODULE_TITLE).use { stmt ->
                stmt.setString(1, moduleTitleParam)
                stmt.setString(2, moduleTypeParam)
                stmt.setString(3, emailParam)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    moduleId = ModuleID(rs.getInt(1))
                }
            }
        }
        return moduleId
    }

    fun getAllModulesByType(moduleTypeParam: String, userIdParam: Int): MutableList<GetModule> {
        val res = mutableListOf<GetModule>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_MODULES).use { result ->
                result.setString(1, moduleTypeParam)
                result.setInt(2,userIdParam)
                result.executeQuery().use { data ->
                    while (data.next()) {
                        val id = data.getInt("id")
                        val moduleTitle = data.getString("title")
                        val description = data.getString("description")
                        val status = data.getString("status")
                        val lastCheckTimestamp = data.getTimestamp("last_check")
                        val lastCheck = lastCheckTimestamp?.toLocalDateTime()
                            ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mma"))
                        val acceptance = data.getString("acceptance")
                        val requestedBy = data.getString("requested_by")
                        res.add(
                            GetModule(
                                id,
                                moduleTitle,
                                description,
                                status,
                                lastCheck,
                                acceptance,
                                requestedBy
                            )
                        )
                    }
                }
            }
        }
        return res
    }

    fun getAllModulesTitle(): MutableList<GetModuleTitle> {
        val res = mutableListOf<GetModuleTitle>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_MODULES_TITLE).executeQuery().use { data ->
                while (data.next()) {
                    val moduleTitle = data.getString("title")
                    val moduleType = data.getString("module_type")
                    res.add(
                        GetModuleTitle(
                            moduleTitle,
                            moduleType
                        )
                    )
                }
            }
        }
        return res
    }

    fun updateModuleHealth(moduleId: Int) {
        ds.connect().use { con ->
            con.prepareStatement(UPDATE_MODULE_HEALTH).also {
                it.setInt(1, moduleId)
            }.execute()
        }
    }

    fun deleteModuleForRequest(idParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(DELETE_MODULE_REQUEST).also {
                it.setInt(1, idParam)
            }.executeUpdate()
        }
    }

    fun updateModuleTitle(idParam: Int, titleParam: String): Int {
        return ds.connect().use { con ->
            con.prepareStatement(UPDATE_MODULE_TITLE).also {
                it.setInt(1, idParam)
                it.setString(2, titleParam)
            }.executeUpdate()
        }
    }

    fun updateModuleDescription(idParam: Int, description: String?): Int {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_MODULE_DESCRIPTION).also {
                it.setInt(1, idParam)
                it.setString(2, description)
            }.executeUpdate()
        }
    }


    //============ CONTROLLERS FOR OTHERS =====================================
    fun insertUserPrivilege(
        userIdParam: Int,
        emailParam: String,
        moduleTitleParam: String,
        serverParam: String,
        isAllowedParam: Boolean,
        activityParam: String,
        accessParam: Int,
        browserInfo: String
    ): Int {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_USER_PRIVILEGE).also {
                it.setInt(1, userIdParam)
                it.setString(2, emailParam)
                it.setString(3, moduleTitleParam)
                it.setString(4, serverParam)
                it.setBoolean(5, isAllowedParam)
                it.setObject(6, OffsetDateTime.now())
                it.setString(7, activityParam)
                it.setInt(8, accessParam)
                it.setString(9, browserInfo)
                it.setString(10, "new")
            }.executeUpdate()
        }
    }

    fun getUserPrivilegeById(idParam: Int): MutableList<ViewUserPrivilege> {
        val res = mutableListOf<ViewUserPrivilege>()
        ds.connect().use { con ->
            con.prepareStatement(GET_USER_PRIVILEGE_BY_ID).also {
                it.setInt(1, idParam)
            }.executeQuery().use { data ->
                while (data.next()) {
                    val moduleTitle = data.getString("module_title")
                    val server = data.getString("server")
                    val isAllowed = data.getBoolean("is_allowed")
                    res.add(
                        ViewUserPrivilege(
                            moduleTitle,
                            server,
                            isAllowed
                        )
                    )
                }
            }
        }
        return res
    }

    fun getNotifInfo(idParam: Int): MutableList<Notification> {
        val res = mutableListOf<Notification>()
        ds.connect().use { con ->
            con.prepareStatement(GET_NOTIF_INFO).also {
                it.setInt(1, idParam)
            }.executeQuery().use { data ->
                while (data.next()) {
                    val id = data.getInt("id")
                    val userId = data.getInt("user_id")
                    val activity = data.getString("activity")
                    val date = data.getString("audit_at")
                    val status = data.getString("status")
                    val accessed = data.getInt("accessed")
                    res.add(
                        Notification(
                            id,
                            userId,
                            activity,
                            date,
                            status,
                            accessed
                        )
                    )
                }
            }
        }
        return res
    }

    fun updateNotifStatus(idParam: Int?, userIdParam: Int?, statusParam: String): Int {
        return ds.connect().use { con ->
            con.prepareStatement(UPDATE_NOTIF_STATUS).also {
                if (idParam == null) it.setNull(1, java.sql.Types.INTEGER) else it.setInt(1, idParam)
                if (userIdParam == null) it.setNull(2, java.sql.Types.INTEGER) else it.setInt(2, userIdParam)
                it.setString(3, statusParam)
            }.executeUpdate()
        }
    }

    fun insertModuleLogs(userIdParam: Int, moduleId: Int, statusParam: String) {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_MODULE_USAGE_LOGS).also {
                it.setInt(1, userIdParam)
                it.setInt(2, moduleId)
                it.setString(3, statusParam)
            }.executeUpdate()
        }
    }

    fun getAllModuleUsage(): MutableList<ModuleUsage> {
        val res = mutableListOf<ModuleUsage>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_MODULE_USAGE).executeQuery().use { data ->
                while (data.next()) {
                    val email = data.getString("email")
                    val roleType = data.getString("role_type")
                    val moduleTitle = data.getString("title")
                    val server = data.getString("module_type")
                    val status = data.getString("status")
                    val lastCheckTimestamp = data.getTimestamp("opened_at")
                    val requestedAt = lastCheckTimestamp?.toLocalDateTime()
                        ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mma"))
                    res.add(
                        ModuleUsage(
                            email,
                            roleType,
                            moduleTitle,
                            server,
                            status,
                            requestedAt
                        )
                    )
                }
            }
        }
        return res
    }

    //============ CONTROLLERS FOR API DETAILS =====================================
    fun getAllAPIData(moduleIdParam: Int, typethParam: String): MutableList<GetDataAPI> {
        val res = mutableListOf<GetDataAPI>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_API).use { query ->
                query.setInt(1, moduleIdParam)
                query.setString(2, typethParam)
                query.executeQuery().use { data ->
                    while (data.next()) {
                        val id: Int? = data.getInt("id")
                        val moduleId: Int? = data.getInt("module_id")
                        val method: String? = data.getString("method")
                        val host: String? = data.getString("host")
                        val port: Int? = data.getInt("port")
                        val endpoint: String? = data.getString("endpoint")
                        val parameters: String? = data.getString("parameters")
                        val status: String? = data.getString("status")
                        val responseTime: String? = data.getString("response_time")
                        val commentCount: Int? = data.getInt("comments")

                        res.add(GetDataAPI(id, moduleId,method, host, port, endpoint, parameters, status, responseTime, commentCount))
                    }
                }
            }
        }
        return res
    }

    fun getAllAPIDataByID(IdParam: Int): MutableList<GetDataAPI> {
        val res = mutableListOf<GetDataAPI>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_API_BY_ID).use { query ->
                query.setInt(1, IdParam)
                query.executeQuery().use { data ->
                    while (data.next()) {
                        val id: Int? = data.getInt("id")
                        val moduleId: Int? = data.getInt("module_id")
                        val method: String? = data.getString("method")
                        val host: String? = data.getString("host")
                        val port: Int? = data.getInt("port")
                        val endpoint: String? = data.getString("endpoint")
                        val parameters: String? = data.getString("parameters")
                        val status: String? = data.getString("status")
                        val responseTime: String? = data.getString("response_time")
                        val commentCount: Int? = data.getInt("comments")

                        res.add(GetDataAPI(id, moduleId,method, host, port, endpoint, parameters, status, responseTime, commentCount))
                    }
                }
            }
        }
        return res
    }

    fun searchAPIData(keyParam: String): MutableList<SearchDataAPI> {
        val res = mutableListOf<SearchDataAPI>()
        ds.connect().use { con ->
            con.prepareStatement(SEARCH_API_DATA).use { query ->
                query.setString(1, keyParam)
                query.executeQuery().use { data ->
                    while (data.next()) {
                        val id: Int? = data.getInt("id")
                        val moduleId: Int? = data.getInt("module_id")
                        val method: String? = data.getString("method")
                        val host: String? = data.getString("host")
                        val port: Int? = data.getInt("port")
                        val endpoint: String? = data.getString("endpoint")
                        val parameters: String? = data.getString("parameters")
                        val status: String? = data.getString("status")
                        val responseTime: String? = data.getString("response_time")
                        val commentCount: Int? = data.getInt("comments")
                        val moduleTitle: String? = data.getString("module_title")

                        res.add(SearchDataAPI(id, moduleId,method, host, port, endpoint, parameters, status, responseTime, commentCount, moduleTitle))
                    }
                }
            }
        }
        return res
    }

    fun addHashPin(pinCodeParam: String, moduleIdParam: Int): Int {
        val hashedPin = pinCodeParam.sha512()
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_HASH_PIN).also {
                it.setString(1, hashedPin)
                it.setInt(2, moduleIdParam)
            }.executeUpdate()
        }
    }

    fun checkIsExists(moduleIdParam: Int): MutableList<CheckPinExists> {
        val res = mutableListOf<CheckPinExists>()
        ds.connect().use { con ->
            con.prepareStatement(CHECK_PIN_EXISTS).use { query ->
                query.setInt(1, moduleIdParam)
                query.executeQuery().use { rs ->
                    while (rs.next()){
                        val exists = rs.getBoolean("is_exists")
                        res.add(CheckPinExists(exists))
                    }
                }
            }
        }
        return res
    }

    fun verifyPinCode(inputPinParam: String, moduleIdParam: Int): Boolean {
        val inputPin = inputPinParam.sha512()
        return ds.connect().use { con ->
            con.prepareStatement(CHECK_PIN_CODE).use { query ->
                query.setString(1, inputPin)
                query.setInt(2, moduleIdParam)
                query.executeQuery().use { rs ->
                    rs.next() && rs.getBoolean(1)
                }
            }
        }
    }

    fun setPinActive(pin: String, active: Boolean) {
        val hashedPin = pin.sha512()
        val sql = if (active) SET_PIN_ACTIVE else SET_PIN_OFF
        ds.connect().use { con ->
            con.prepareStatement(sql).use { query ->
                query.setString(1, hashedPin)
                query.executeUpdate()
            }
        }
    }

    fun insertOrUpdateAuth(inputAuthParam: String?): Int {
        val res = 0
        ds.connect().use { con ->
            con.prepareStatement(INSERT_OR_UPDATE_AUTH_TOKEN).also {
                it.setString(1, inputAuthParam)
            }.executeUpdate()
        }
        return res
    }

    fun insertAPIComment(apiIdParam: Int, moduleId: Int, userComment: String, fileImage: String?, commentedBy: String, activityParam: String, browserInfo: String, userIdParam: Int) {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_API_COMMENT).also {
                it.setInt(1, apiIdParam)
                it.setInt(2, moduleId)
                it.setString(3, userComment)
                it.setString(4, fileImage)
                it.setString(5, commentedBy)
                it.setObject(6, OffsetDateTime.now())
                it.setString(7, activityParam)
                it.setString(8, browserInfo)
                it.setInt(9, userIdParam)
            }.executeUpdate()
        }
    }

    fun getAllAPIComments(apiIdParam: Int, moduleIdParam: Int): MutableList<APIUserComments> {
        val res = mutableListOf<APIUserComments>()
        ds.connect().use { con ->
            con.prepareStatement(GET_API_COMMENTS).use { query ->
                query.setInt(1, apiIdParam)
                query.setInt(2, moduleIdParam)
                query.executeQuery().use { rs ->
                    while (rs.next()){
                        val username = rs.getString("username")
                        val comments = rs.getString("comments")
                        val img = rs.getString("uploaded_image")
                        val sent = rs.getTimestamp("sent_at")
                        val sentAt = sent?.toLocalDateTime()
                            ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mma"))
                        res.add(APIUserComments(username, comments, img, sentAt))
                    }
                }
            }
        }
        return res
    }

    fun getAllAPIDetailsInfo(): MutableList<APIDetailsInfo> {
        val res = mutableListOf<APIDetailsInfo>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_API_INFO).use { query -> query.executeQuery().use { rs ->
                    while (rs.next()){
                        val id = rs.getInt("id")
                        val method = rs.getString("method")
                        val title = rs.getString("title")
                        val type = rs.getString("type")
                        val endpoint = rs.getString("endpoint")
                        val parameter = rs.getString("parameter")
                        val keys = rs.getString("keys")
                        val description = rs.getString("descriptions")
                        val relatedAPI = rs.getString("related_apis")
                        val relatedModules = rs.getString("related_modules")

                        res.add(APIDetailsInfo(id, method, endpoint, parameter, title, type, keys, description, relatedAPI, relatedModules))
                    }
                }
            }
        }
        return res
    }

    fun updateAPIStatus(idParam: Int, statusParam: String): Int {
        ds.connect().use { con ->
            con.prepareStatement(UPDATE_API_STATUS).use { query ->
                query.setString(1, statusParam)
                query.setInt(2, idParam)
                return query.executeUpdate()
            }
        }
    }

    fun updateAPIBody(idParam: Int, bodyParam: String): Int {
        val jsonElement = Json.parseToJsonElement(bodyParam)
        val jsonString = Json.encodeToString(JsonObject.serializer(), jsonElement.jsonObject)

        ds.connect().use { con ->
            con.prepareStatement(UPDATE_API_BODY).use { query ->
                query.setInt(1, idParam)
                query.setObject(2, jsonString, java.sql.Types.OTHER)
                return query.executeUpdate()
            }
        }
    }

    fun updateAPIHost(idParam: Int, hostParam: String): Int {
        ds.connect().use { con ->
            con.prepareStatement(UPDATE_MODULE_API_HOST).use { query ->
                query.setInt(1, idParam)
                query.setString(2, hostParam)
                return query.executeUpdate()
            }
        }
    }

    fun updateLastCheck(moduleId: Int) {
        ds.connect().use { con ->
            con.prepareStatement(UPDATE_LAST_CHECK).also {
                it.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()))
                it.setInt(2, moduleId)
            }.executeUpdate()
        }
    }

    fun updateAPIData(
        id: Int,
        method: String,
        host: String,
        port: Int,
        endpoint: String,
        parameters: String?,
    ): Int {
        return ds.connect().use { con ->
            con.prepareStatement(UPDATE_API_DATA).also {
                it.setInt(1, id)
                it.setString(2, method)
                it.setString(3, host)
                it.setInt(4, port)
                it.setString(5, endpoint)
                it.setString(6, parameters)
            }.executeUpdate()
        }
    }

    fun deleteAPIData(idParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(DELETE_API_DATA).also {
                it.setInt(1, idParam)
            }.executeUpdate()
        }
    }

    fun deleteAPIDetail(idParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(DELETE_API_DETAIL).use { query ->
                query.setInt(1, idParam)
                val rowsAffected = query.executeQuery()
                if (rowsAffected.next())  rowsAffected.getInt(1) else 0
            }
        }
    }

    fun deleteAPIParams(idParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(DELETE_API_PARAMS).use { query ->
                query.setInt(1, idParam)
                val rowsAffected = query.executeQuery()
                if (rowsAffected.next())  rowsAffected.getInt(1) else 0
            }
        }
    }

    fun deleteAPIHeader(idParam: Int): Int {
        return ds.connect().use { con ->
            con.prepareStatement(DELETE_API_HEADERS).use { query ->
                query.setInt(1, idParam)
                val rowsAffected = query.executeQuery()
                if (rowsAffected.next())  rowsAffected.getInt(1) else 0
            }
        }
    }

    fun insertAPIDetails(
        apiIdParam: Int,
        moduleIdParam: Int,
        keyParam: String,
        valueParam: String,
        descriptionParam: String?,
        sqlFileParam: String?
    ): Int? {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_API_DETAILS).also {
                it.setInt(1, apiIdParam)
                it.setInt(2, moduleIdParam)
                it.setString(3, keyParam)
                it.setString(4, valueParam)
                it.setString(5, descriptionParam)
                it.setString(6, sqlFileParam)
            }.executeUpdate()
        }
    }

    fun insertAPIParams(
        apiIdParam: Int,
        moduleIdParam: Int,
        keyParam: String,
        valueParam: String,
        descriptionParam: String?
    ): Int? {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_API_PARAMS).also {
                it.setInt(1, apiIdParam)
                it.setInt(2, moduleIdParam)
                it.setString(3, keyParam)
                it.setString(4, valueParam)
                it.setString(5, descriptionParam)
            }.executeUpdate()
        }
    }

    fun insertAPIHeader(
        apiIdParam: Int,
        moduleIdParam: Int,
        keyParam: String
    ): Int? {
        return ds.connect().use { con ->
            con.prepareStatement(INSERT_API_HEADERS).also {
                it.setInt(1, apiIdParam)
                it.setInt(2, moduleIdParam)
                it.setString(3, keyParam)
            }.executeUpdate()
        }
    }

    fun getAllAPIDetails(apiIdParam: Int): MutableList<GetAPIDetails1> {
        val res = mutableListOf<GetAPIDetails1>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_API_DETAILS).use { query ->
                query.setInt(1, apiIdParam)
                query.executeQuery().use { data ->
                    while (data.next()) {
                        val id: Int = data.getInt("id")
                        val key: String = data.getString("key")
                        val value: String = data.getString("value")
                        val description: String? = data.getString("description")
                        val sqlFile: String? = data.getString("sql_file")

                        res.add(GetAPIDetails1(id,key, value, description, sqlFile))
                    }
                }
            }
        }
        return res
    }

    fun getAllAPIParams(apiIdParam: Int): MutableList<GetAPIDetails> {
        val res = mutableListOf<GetAPIDetails>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_API_PARAMS).use { query ->
                query.setInt(1, apiIdParam)
                query.executeQuery().use { data ->
                    while (data.next()) {
                        val id: Int = data.getInt("id")
                        val key: String = data.getString("key")
                        val value: String = data.getString("value")
                        val description: String? = data.getString("description")

                        res.add(GetAPIDetails(id,key, value, description))
                    }
                }
            }
        }
        return res
    }

    fun getAllAPIHeader(apiIdParam: Int): MutableList<GetAPITags> {
        val res = mutableListOf<GetAPITags>()
        ds.connect().use { con ->
            con.prepareStatement(GET_ALL_API_HEADERS).use { query ->
                query.setInt(1, apiIdParam)
                query.executeQuery().use { data ->
                    while (data.next()) {
                        val id: Int = data.getInt("id")
                        val key: String = data.getString("key")

                        res.add(GetAPITags(id,key))
                    }
                }
            }
        }
        return res
    }

    fun insertAPI(
        moduleIdParam: Int,
        typeParam: String,
        methodParam: String,
        hostParam: String,
        portParam: Int,
        endpointParam: String,
        parametersParam: String?,
        statusParam: String?,
    ): Int{
        return  ds.connect().use { con ->
            con.prepareStatement(INSERT_API_DATA).also {
                it.setInt(1, moduleIdParam)
                it.setString(2, typeParam)
                it.setString(3, methodParam)
                it.setString(4, hostParam)
                it.setInt(5, portParam)
                it.setString(6, endpointParam)
                it.setString(7, parametersParam)
                it.setString(8, statusParam)
                val rs = it.generatedKeys
                if (rs.next()) rs.getInt(1) else -1
            }.executeUpdate()
        }

    }
}