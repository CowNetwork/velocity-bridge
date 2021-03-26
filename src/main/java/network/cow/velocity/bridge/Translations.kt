package network.cow.velocity.bridge

/**
 * @author Benedikt Wüller
 */

object Translations {
    const val COMMON_ID = "proxy.common.id"
    const val COMMON_REASON = "proxy.common.reason"
    const val COMMON_ERROR = "proxy.common.error"

    const val ERROR_INITIAL_SERVER_NOT_FOUND = "proxy.errors.initial_server_not_found"
    const val ERROR_SERVER_NOT_FOUND = "proxy.errors.server_not_found"
    const val ERROR_SERVER_CONNECTION_FAILED = "proxy.errors.server_connection_failed"

    const val SESSION_STOPPED_KICKED = "proxy.session.stop_causes.kicked"
    const val SESSION_STOPPED_BANNED = "proxy.session.stop_causes.banned"
    const val SESSION_STOPPED_MAINTENANCE = "proxy.session.stop_causes.maintenance"
    const val SESSION_STOPPED_ERROR = "proxy.session.stop_causes.error"
    const val SESSION_STOPPED_UNKNOWN = "proxy.session.stop_causes.unknown"

    const val SESSION_BANNED_DURATION_PERMANENT = "proxy.session.banned.duration_permanent"
    const val SESSION_BANNED_DURATION_UNTIL = "proxy.session.banned.duration_until"
}
