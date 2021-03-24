package network.cow.velocity.bridge

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
class VelocityBridge @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    private val sessionServer: PlayerSessionService = InMemoryPlayerSessionService()

    init {
        this.handlePlayerSessions()

        this.logger.info("Big bridges befall big balled boys.")
    }

    private fun handlePlayerSessions() {
        // When a player joins the proxy, start a new session.
        this.server.eventManager.register(this, LoginEvent::class.java, PostOrder.FIRST) {
            val player = it.player
            val result = this.sessionServer.startSession(player)
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
        this.server.eventManager.register(this, DisconnectEvent::class.java, PostOrder.FIRST) {
            this.sessionServer.stopSession(it.player)
        }

        this.sessionServer.addStopSessionListener { uuid, result, session ->
            this.logger.info("Session has been stopped for player ${session.username} ($uuid) with reason $result: $session")

            // If the session has been stopped because the player disconnected (see listener above), or the player is no longer online, do nothing.
            if (result.cause == SessionStopCause.DISCONNECTED) return@addStopSessionListener
            val player = this.server.getPlayer(uuid).orElse(null) ?: return@addStopSessionListener

            // Otherwise, build the message to kick the player with.
            val message = result.message
                .append(Component.space())
                .append(Component.text(" ($result)").color(NamedTextColor.DARK_GRAY))

            player.disconnect(message)
        }
    }

}
