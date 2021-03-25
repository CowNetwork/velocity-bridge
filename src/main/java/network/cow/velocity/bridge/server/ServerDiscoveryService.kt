package network.cow.velocity.bridge.server

import com.velocitypowered.api.proxy.server.ServerInfo

/**
 * @author Benedikt Wüller
 */
abstract class ServerDiscoveryService {

    private val serverRegisterListeners = mutableListOf<(ServerInfo) -> Unit>()
    private val serverUnregisterListeners = mutableListOf<(ServerInfo) -> Unit>()

    fun addRegisterServerListener(listener: (ServerInfo) -> Unit) {
        this.serverRegisterListeners.add(listener)
    }

    fun addUnregisterServerListener(listener: (ServerInfo) -> Unit) {
        this.serverUnregisterListeners.add(listener)
    }

    protected fun onRegister(server: ServerInfo) {
        this.serverRegisterListeners.forEach { it(server) }
    }

    protected fun onUnregister(server: ServerInfo) {
        this.serverUnregisterListeners.forEach { it(server) }
    }

}
