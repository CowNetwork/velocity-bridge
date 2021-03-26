package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import network.cow.velocity.bridge.getCurrentDate
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class InMemoryPlayerSessionService : PlayerSessionService() {

    private val sessions = mutableMapOf<UUID, Session>()

    override fun startSession(player: Player): InitializeSessionResult {
        this.stopSession(player)
        this.sessions[player.uniqueId] = Session(player.username)
        return SessionInitialized()
    }

    override fun stopSession(playerId: UUID) {
        val session = this.sessions.remove(playerId) ?: return
        session.stoppedAt = getCurrentDate()
        this.onStopSession(playerId, SessionStopPlayerDisconnected(), session)
    }

}
