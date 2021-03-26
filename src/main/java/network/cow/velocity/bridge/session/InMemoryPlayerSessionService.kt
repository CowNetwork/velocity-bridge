package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.PlayerSettings
import dev.benedikt.localize.getLocale
import network.cow.velocity.bridge.getCurrentDate
import java.time.Duration
import java.time.temporal.TemporalUnit
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class InMemoryPlayerSessionService : PlayerSessionService() {

    private val sessions = mutableMapOf<UUID, Session>()

    override fun startSession(player: Player): InitializeSessionResult {
        this.stopSession(player)
//        val session = Session(UUID.randomUUID(), player.username, player.getLocale(), player.playerSettings.chatMode)
//        this.sessions[player.uniqueId] = session
        return SessionRejected(SessionStopPlayerBanned(UUID.randomUUID(), UUID.randomUUID(), "COCK_TOO_LARGE", getCurrentDate(), Duration.ofDays(7)))
    }

    override fun updateLocale(playerId: UUID, locale: String) {
        this.sessions[playerId]?.locale = locale
    }

    override fun updateChatMode(playerId: UUID, chatMode: PlayerSettings.ChatMode) {
        this.sessions[playerId]?.chatMode = chatMode
    }

    override fun stopSession(playerId: UUID) {
        val session = this.sessions.remove(playerId) ?: return
        session.stoppedAt = getCurrentDate()
        this.onStopSession(playerId, SessionStopPlayerDisconnected(session.id), session)
    }

}
