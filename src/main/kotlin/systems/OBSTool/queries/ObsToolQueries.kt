package com.marlow.systems.OBSTool.queries


//============ QUERY FOR USER SIGN IN, SIGN UP, & SIGN OUT =====================================
const val INSERT_INFORMATION    = "SELECT obs_tool_monitoring.insert_information(?,?,?,?,?,?,?,?,?,?,?,?,?)"
const val CHECK_USER_CREDENTIAL = "SELECT * FROM obs_tool_monitoring.check_user_credential(?, ?)"
const val LOGOUT_USER_CRED      = "CALL obs_tool_monitoring.logout_user_cred(?)"
const val UPDATE_USER_INFO      = "CALL obs_tool_monitoring.update_user_info(?,?,?,?,?,?)"
const val UPDATE_USER_INFO_NO_PASS      = "CALL obs_tool_monitoring.update_user_info_no_pass(?,?,?,?,?)"
const val GET_USER_INFO         = "SELECT * FROM obs_tool_monitoring.get_user_credential(?)"
const val GET_ALL_USER_INFO     = "SELECT * FROM obs_tool_monitoring.get_all_user_info()"

//============ QUERY FOR LOGS =====================================
const val INSERT_API_LOG = "CALL obs_tool_monitoring.upsert_api_log( ?::integer, ?::double precision, ?::double precision, ?::double precision, ?::integer, ?::integer, ?::boolean, ?::timestamptz )"
const val INSERT_ERROR_LOGS = "CALL obs_tool_monitoring.insert_error_logs(?,?::json,?,?,?,?,?)"
const val GET_ERROR_LOGS = "SELECT * FROM obs_tool_monitoring.get_error_logs();"
const val INSERT_AUDIT_LOGS     = "CALL obs_tool_monitoring.insert_audit_logs(?,?,?,?)"
const val GET_ALL_AUDIT_LOGS    = "SELECT * FROM obs_tool_monitoring.get_all_audit_logs(?)"

//============ QUERY FOR DASHBOARD =====================================
const val GET_AVERAGE_RTIME = "SELECT * FROM obs_tool_monitoring.get_avg_response_time()"
const val GET_MODULE_STATUS = "SELECT * FROM obs_tool_monitoring.get_module_status(?)"
const val GET_API_BODY_BY_ID = "SELECT * FROM obs_tool_monitoring.get_api_body(?)"
const val GET_API_STATUS_COUNT = "SELECT * FROM obs_tool_monitoring.get_api_status_count()"
const val GET_DASHBOARD_METRICS = "SELECT * FROM obs_tool_monitoring.get_dashboard_metrics()"
const val GET_HOST_NAME = "SELECT * FROM obs_tool_monitoring.get_host_names()"
const val GET_TOTAL_REQUEST = "SELECT * FROM obs_tool_monitoring.get_total_request(?)"
const val GET_ALL_TOTAL_REQUEST = "SELECT * FROM obs_tool_monitoring.get_all_total_request()"

//============ QUERY FOR REQUEST =====================================
const val GET_ALL_MODULES_PENDING = "SELECT * FROM obs_tool_monitoring.get_all_modules_pending()"
const val GET_ALL_MODULES_DELETE_REQUEST = "SELECT * FROM obs_tool_monitoring.get_all_modules_delete_pending()"
const val UPDATE_MODULE_REQUEST = "CALL obs_tool_monitoring.update_module_pending(?,?)"
const val INSERT_REQUESTS_LOGS = "CALL obs_tool_monitoring.insert_audit_logs_request(?,?,?,?)"
const val DELETE_MODULE_DATA = "SELECT obs_tool_monitoring.delete_module_data(?)"
const val GET_ALL_ACCOUNT_PENDING = "SELECT * FROM obs_tool_monitoring.get_all_account_pending()"
const val UPDATE_ACCOUNT_PENDING = "CALL obs_tool_monitoring.update_account_pending(?,?)"

const val INSERT_IMPORTED_WORKSPACE = "CALL obs_tool_monitoring.insert_imported_workspace(?,?,?,?,?,?,?,?,?::json);"

//============ QUERY FOR MODULE =====================================
const val INSERT_MODULE_TITLE = "SELECT obs_tool_monitoring.insert_module_fun(?,?,?)"
const val GET_ALL_MODULES = "SELECT * FROM obs_tool_monitoring.get_all_modules(?,?)"
const val GET_ALL_MODULES_TITLE = "SELECT * FROM obs_tool_monitoring.get_all_modules_title()"
const val UPDATE_MODULE_HEALTH = "SELECT obs_tool_monitoring.update_module_health(?);"
const val DELETE_MODULE_REQUEST = "CALL obs_tool_monitoring.delete_module_request(?)"
const val UPDATE_MODULE_TITLE = "CALL obs_tool_monitoring.update_module_title(?,?)"
const val INSERT_MODULE_DESCRIPTION = "CALL obs_tool_monitoring.insert_module_description(?,?)"

//============ QUERY FOR API DETAILS =====================================
const val INSERT_API_DATA = "CALL obs_tool_monitoring.insert_api_data(?,?,?,?,?,?,?,?)"
const val INSERT_API_DETAILS = "CALL obs_tool_monitoring.save_api_details(?,?,?,?,?,?)"
const val INSERT_API_PARAMS = "CALL obs_tool_monitoring.save_api_params(?,?,?,?,?)"
const val INSERT_API_HEADERS = "CALL obs_tool_monitoring.save_api_headers(?,?,?)"
const val GET_ALL_API_DETAILS = "SELECT * FROM obs_tool_monitoring.get_api_details(?)"
const val DELETE_API_DETAIL = "SELECT obs_tool_monitoring.delete_api_details(?)"
const val GET_ALL_API_PARAMS = "SELECT * FROM obs_tool_monitoring.get_api_params(?)"
const val DELETE_API_PARAMS = "SELECT obs_tool_monitoring.delete_api_params(?)"
const val GET_ALL_API_HEADERS = "SELECT * FROM obs_tool_monitoring.get_api_headers(?)"
const val DELETE_API_HEADERS = "SELECT obs_tool_monitoring.delete_api_headers(?)"
const val GET_ALL_API = "SELECT * FROM obs_tool_monitoring.get_all_api(?,?)"
const val GET_ALL_API_BY_ID = "SELECT * FROM obs_tool_monitoring.get_all_api_by_id(?)"
const val SEARCH_API_DATA = "SELECT * FROM obs_tool_monitoring.search_api_tags(?)"
const val UPDATE_API_STATUS = "CALL obs_tool_monitoring.update_api_status(?,?)"
const val DELETE_API_DATA = "CALL obs_tool_monitoring.delete_api_data(?)"
const val UPDATE_API_DATA = "CALL obs_tool_monitoring.update_api_data(?,?,?,?,?,?)"
const val UPDATE_API_BODY = "CALL obs_tool_monitoring.update_api_body(?,?::json)"
const val UPDATE_MODULE_API_HOST = "CALL obs_tool_monitoring.update_api_host(?,?)"
const val UPDATE_LAST_CHECK = "UPDATE obs_tool_monitoring.modules SET last_check = ? WHERE id = ?"

const val INSERT_HASH_PIN = "CALL obs_tool_monitoring.insert_hashed_pin(?,?)"
const val CHECK_PIN_CODE = "SELECT obs_tool_monitoring.check_pincode(?,?)"
const val CHECK_PIN_EXISTS = "SELECT * FROM obs_tool_monitoring.get_pincode_exists(?)"
const val SET_PIN_ACTIVE = "UPDATE obs_tool_monitoring.pincode SET is_active = true WHERE code = ?"
const val SET_PIN_OFF = "UPDATE obs_tool_monitoring.pincode SET is_active = false WHERE code = ?"

const val INSERT_OR_UPDATE_AUTH_TOKEN = "CALL obs_tool_monitoring.insert_auth_token(?)"
const val INSERT_API_COMMENT = "CALL obs_tool_monitoring.insert_api_comment(?,?,?,?,?,?,?,?,?)"
const val GET_API_COMMENTS = "SELECT * FROM obs_tool_monitoring.get_api_comments(?, ?)"

const val GET_ALL_API_INFO = "SELECT * FROM obs_tool_monitoring.get_all_api_details()"

//============ QUERY FOR OTHERS =====================================
const val INSERT_USER_PRIVILEGE = "CALL obs_tool_monitoring.insert_user_privilege(?,?,?,?,?,?,?,?,?,?)"
const val GET_USER_PRIVILEGE_BY_ID = "SELECT * FROM obs_tool_monitoring.get_user_privilege(?)"
const val GET_NOTIF_INFO        = "SELECT * FROM obs_tool_monitoring.get_notification_logs(?)"
const val UPDATE_NOTIF_STATUS   = "CALL obs_tool_monitoring.update_notif_status(?,?,?)"

const val INSERT_MODULE_USAGE_LOGS = "CALL obs_tool_monitoring.insert_module_usage_logs(?,?,?)"
const val GET_ALL_MODULE_USAGE = "SELECT * FROM obs_tool_monitoring.get_all_module_active_usage()"
