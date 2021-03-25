package network.cow.velocity.bridge.server

import com.velocitypowered.api.proxy.server.ServerInfo

/**
 * @author Benedikt Wüller
 */
class DummyServerDiscoveryService : ServerDiscoveryService() {

    override fun getServers(): Set<ServerInfo> {
        return setOf()
    }

}
