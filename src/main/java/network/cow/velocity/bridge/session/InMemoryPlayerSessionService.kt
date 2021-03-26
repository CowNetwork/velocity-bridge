package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.PlayerSettings
import dev.benedikt.localize.getLocale
import network.cow.velocity.bridge.getCurrentDate
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class InMemoryPlayerSessionService : PlayerSessionService() {

    private val sessions = mutableMapOf<UUID, Session>()

    override fun startSession(player: Player): InitializeSessionResult {
        this.stopSession(player)
        this.sessions[player.uniqueId] = Session(player.username, player.getLocale(), player.playerSettings.chatMode)
        return SessionInitialized()
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
        this.onStopSession(playerId, SessionStopPlayerDisconnected(), session)
    }

}
