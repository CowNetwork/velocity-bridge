package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
abstract class PlayerSessionService {

    private val stopSessionListeners = mutableListOf<(UUID, StopSessionResult, Session) -> Unit>()

    /**
     * Adds a listener to call, after a [Session] has been closed for any given [UUID].
     */
    fun addStopSessionListener(listener: (UUID, StopSessionResult, Session) -> Unit) {
        this.stopSessionListeners.add(listener)
    }

    /**
     * Called by the [PlayerSessionService], after a [Session] for the given [playerId] has been closed.
     */
    protected fun onStopSession(playerId: UUID, sessionResult: StopSessionResult, session: Session) {
        this.stopSessionListeners.forEach { it(playerId, sessionResult, session) }
    }

    /**
     * Creates a new session for the given [player] and returns the resulting [InitializePlayerSessionResponse] synchronously.
     * [InitializePlayerSessionResponse.response] will equal [SessionResponse.INITIALIZED] if the [Session] has been created.
     */
    abstract fun startSession(player: Player): InitializePlayerSessionResponse

    /**
     * Stops any active [Session] for the given [playerId] gracefully.
     */
    abstract fun stopSession(playerId: UUID)

    /**
     * Stops any active [Session] for the given [player] gracefully.
     */
    fun stopSession(player: Player) = stopSession(player.uniqueId)

}
