package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import network.cow.velocity.bridge.getCurrentDate
import java.time.ZonedDateTime
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class InMemoryPlayerSessionService : PlayerSessionService() {

    private val sessions = mutableMapOf<UUID, ZonedDateTime>()

    override fun startSession(player: Player): InitializePlayerSessionResponse {
        this.stopSession(player)
        this.sessions[player.uniqueId] = getCurrentDate()
        return InitializePlayerSessionResponse(SessionResponse.INITIALIZED)
    }

    override fun stopSession(uuid: UUID) {
        val createdAt = this.sessions.remove(uuid) ?: return
        this.onStopSession(uuid, StopSessionReason.DISCONNECTED, Session(createdAt))
    }

}
