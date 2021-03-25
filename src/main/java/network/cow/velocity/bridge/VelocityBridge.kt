package network.cow.velocity.bridge

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.cow.velocity.bridge.distribution.InMemoryPlayerDistributionService
import network.cow.velocity.bridge.distribution.PlayerDistributionService
import network.cow.velocity.bridge.server.DummyServerDiscoveryService
import network.cow.velocity.bridge.server.ServerDiscoveryService
import network.cow.velocity.bridge.session.InMemoryPlayerSessionService
import network.cow.velocity.bridge.session.PlayerSessionService
import network.cow.velocity.bridge.session.SessionResponse
import network.cow.velocity.bridge.session.SessionStopCause
import org.slf4j.Logger

/**
 * @author Benedikt Wüller
 */
@Plugin(
    id = "velocity-bridge", url = "https://github.com/CowNetwork/velocity-bridge",
    name = "Velocity Bridge", description = "Bridges the proxy and our infrastructure.",
    authors = ["Benedikt Wüller"]
)
class VelocityBridge @Inject constructor(private val proxy: ProxyServer, private val logger: Logger) {

    private val discoveryService: ServerDiscoveryService = DummyServerDiscoveryService()
    private val sessionService: PlayerSessionService = InMemoryPlayerSessionService()
    private val playerService: PlayerDistributionService = InMemoryPlayerDistributionService()

    init {
        this.handleServerDiscovery()
        this.handlePlayerSessions()
        this.handlePlayerDistribution()

        // TODO: translations and handle locale change

        this.logger.info("Big bridges befall big balled boys.")
    }

    private fun handleServerDiscovery() {
        this.discoveryService.addRegisterServerListener(this.proxy::registerServer)
        this.discoveryService.addUnregisterServerListener(this.proxy::unregisterServer)
    }

    private fun handlePlayerSessions() {
        // When a player joins the proxy, start a new session.
        this.proxy.eventManager.register(this, LoginEvent::class.java, PostOrder.FIRST) {
            val player = it.player
            val result = this.sessionService.startSession(player)
            if (result.response == SessionResponse.INITIALIZED) {
                this.logger.info("Session has been initialized for player ${player.username} (${player.uniqueId}).")
                return@register
            }

            // If the session could not be initialized, kick the player with the corresponding message.
            val message = result.message
                .append(Component.space())
                .append(Component.text("(${result.response})").color(NamedTextColor.DARK_GRAY))

            player.disconnect(message)

            this.logger.info("Session couldn't be initialized for player ${player.username} (${player.uniqueId}). Response: ${result.response}.")
        }

        // When a player disconnected, stop the current session.
        this.proxy.eventManager.register(this, DisconnectEvent::class.java, PostOrder.FIRST) {
            this.sessionService.stopSession(it.player)
        }

        this.sessionService.addStopSessionListener { uuid, result, session ->
            this.logger.info("Session has been stopped for player ${session.username} ($uuid) with reason $result: $session")

            // If the session has been stopped because the player disconnected (see listener above), or the player is no longer online, do nothing.
            if (result.cause == SessionStopCause.DISCONNECTED) return@addStopSessionListener
            val player = this.proxy.getPlayer(uuid).orElse(null) ?: return@addStopSessionListener

            // Otherwise, build the message to kick the player with.
            val message = result.message
                .append(Component.space())
                .append(Component.text(" ($result)").color(NamedTextColor.DARK_GRAY))

            player.disconnect(message)
        }
    }

    private fun handlePlayerDistribution() {
        this.proxy.eventManager.register(this, PlayerChooseInitialServerEvent::class.java, PostOrder.FIRST) {
            // Request the initial server for this player.
            val server = this.playerService.getInitialServer(it.player.uniqueId)?.let(this.proxy::getServer)

            // If the initial server does not exist, kick the player.
            if (server == null || server.isEmpty) {
                val message = Component.text(Translations.ERROR_INITIAL_SERVER_NOT_FOUND).color(NamedTextColor.RED) // TODO: translate
                it.player.disconnect(message)
                return@register
            }

            it.setInitialServer(server.get())
        }

        // Move the player to the requested server.
        this.playerService.addPlayerMoveListener { uuid, serverName ->
            val player = this.proxy.getPlayer(uuid).orElse(null) ?: return@addPlayerMoveListener
            val server = this.proxy.getServer(serverName)

            // If the server does not exist, send a message to the affected player.
            if (server.isEmpty) {
                val message = Component.text(Translations.ERROR_SERVER_NOT_FOUND).color(NamedTextColor.RED) // TODO: translate
                player.sendMessage(message)
                return@addPlayerMoveListener
            }

            // Try to connect to the server.
            player.createConnectionRequest(server.get()).connect().thenAccept {
                if (it.isSuccessful) return@thenAccept

                // If the server rejected the connection, send a message to the affected player.
                val message = Component.text(Translations.ERROR_SERVER_CONNECTION_FAILED).color(NamedTextColor.RED) // TODO: translate
                    .append(Component.space())
                    .append(Component.text("(${it.status})").color(NamedTextColor.DARK_GRAY))

                player.sendMessage(message)
            }
        }
    }

}
