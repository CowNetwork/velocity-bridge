package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import network.cow.velocity.bridge.getCurrentDate
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class InMemoryPlayerSessionService : PlayerSessionService() {

    private val sessions = mutableMapOf<UUID, Session>()

    override fun startSession(player: Player): InitializePlayerSessionResponse {
        this.stopSession(player)
        this.sessions[player.uniqueId] = Session(player.username)
        return InitializePlayerSessionResponse(SessionResponse.INITIALIZED)
    }

    override fun stopSession(uuid: UUID) {
        val session = this.sessions.remove(uuid) ?: return
        session.stoppedAt = getCurrentDate()
        this.onStopSession(uuid, StopSessionResult(SessionStopCause.DISCONNECTED), session)
    }

}
