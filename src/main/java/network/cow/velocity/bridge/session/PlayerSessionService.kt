package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.PlayerSettings
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
abstract class PlayerSessionService {

    private val stopSessionListeners = mutableListOf<(UUID, SessionStopCause, Session) -> Unit>()

    /**
     * Adds a listener to call, after a [Session] has been closed for any given [UUID].
     */
    fun addStopSessionListener(listener: (UUID, SessionStopCause, Session) -> Unit) {
        this.stopSessionListeners.add(listener)
    }

    /**
     * Called by the [PlayerSessionService], after a [Session] for the given [playerId] has been closed.
     */
    protected fun onStopSession(playerId: UUID, cause: SessionStopCause, session: Session) {
        this.stopSessionListeners.forEach { it(playerId, cause, session) }
    }

    /**
     * Creates a new session for the given [player] and returns the resulting [InitializeSessionResult] synchronously.
     */
    abstract fun startSession(player: Player): InitializeSessionResult

    /**
     * Updates the [locale] for the given [playerId].
     */
    abstract fun updateLocale(playerId: UUID, locale: String)

    /**
     * Updates the [chatMode] for the given [playerId].
     */
    abstract fun updateChatMode(playerId: UUID, chatMode: PlayerSettings.ChatMode)

    /**
     * Stops any active [Session] for the given [playerId] gracefully.
     */
    abstract fun stopSession(playerId: UUID)

    /**
     * Stops any active [Session] for the given [player] gracefully.
     */
    fun stopSession(player: Player) = stopSession(player.uniqueId)

}
