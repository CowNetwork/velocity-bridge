package network.cow.velocity.bridge.session

import net.kyori.adventure.text.Component
import network.cow.velocity.bridge.getCurrentDate
import java.time.ZonedDateTime

/**
 * @author Benedikt Wüller
 */

data class Session(var startedAt: ZonedDateTime = getCurrentDate(), var stoppedAt: ZonedDateTime = getCurrentDate())

data class InitializePlayerSessionResponse(val response: SessionResponse = SessionResponse.UNKNOWN, val denyMessage: Component = Component.empty())

enum class SessionResponse {
    INITIALIZED,
    REJECTED,
    UNKNOWN
}

enum class StopSessionReason(val translationKey: String) {
    DISCONNECTED("proxy.session.stop.disconnected"),
    UNKNOWN("proxy.session.stop.unknown")
}
