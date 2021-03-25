package network.cow.velocity.bridge.distribution

import java.util.UUID

/**
 * @author Benedikt Wüller
 */
abstract class PlayerDistributionService {

    private val playerMoveListeners = mutableListOf<(UUID, String) -> Unit>()

    fun addPlayerMoveListener(listener: (UUID, String) -> Unit) {
        this.playerMoveListeners.add(listener)
    }

    protected fun onPlayerMove(playerId: UUID, server: String) {
        this.playerMoveListeners.forEach { it(playerId, server) }
    }

    abstract fun getInitialServer(playerId: UUID): String?

}
