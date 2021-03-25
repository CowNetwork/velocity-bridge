package network.cow.velocity.bridge.distribution

import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class InMemoryPlayerDistributionService : PlayerDistributionService() {

    override fun getInitialServer(playerId: UUID): String? = null

}
