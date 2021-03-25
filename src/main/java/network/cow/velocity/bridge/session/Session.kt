package network.cow.velocity.bridge.session

import net.kyori.adventure.text.Component
import network.cow.velocity.bridge.getCurrentDate
import java.time.ZonedDateTime

/**
 * @author Benedikt Wüller
 */

data class Session(val username: String, val startedAt: ZonedDateTime = getCurrentDate(), var stoppedAt: ZonedDateTime = getCurrentDate())

data class InitializePlayerSessionResponse(val response: SessionResponse = SessionResponse.UNKNOWN, val message: Component = Component.empty())

enum class SessionResponse {
    INITIALIZED,
    REJECTED,
    UNKNOWN
}

data class StopSessionResult(val cause: SessionStopCause, val message: Component = Component.empty())

enum class SessionStopCause {
    DISCONNECTED,
    KICKED,
    BANNED,
    OTHER
}
