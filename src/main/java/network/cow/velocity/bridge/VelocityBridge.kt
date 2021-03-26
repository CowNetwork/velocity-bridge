package network.cow.velocity.bridge

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.PlayerSettingsChangedEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import dev.benedikt.localize.LocalizeService
import dev.benedikt.localize.getLocale
import dev.benedikt.localize.json.JsonHttpLocaleProvider
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.cow.velocity.bridge.distribution.InMemoryPlayerDistributionService
import network.cow.velocity.bridge.distribution.PlayerDistributionService
import network.cow.velocity.bridge.server.DummyServerDiscoveryService
import network.cow.velocity.bridge.server.ServerDiscoveryService
import network.cow.velocity.bridge.session.InMemoryPlayerSessionService
import network.cow.velocity.bridge.session.PlayerSessionService
import network.cow.velocity.bridge.session.SessionInitialized
import network.cow.velocity.bridge.session.SessionRejected
import network.cow.velocity.bridge.session.SessionStopPlayerDisconnected
import org.slf4j.Logger
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import dev.benedikt.localize.translateSync
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import kotlin.system.measureTimeMillis

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
        this.initializeLocales()
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent?) {
        this.handleServerDiscovery()
        this.handlePlayerSessions()
        this.handlePlayerDistribution()

        this.logger.info("Big bridges befall big balled boys.")
    }

    // TODO: make configurable
    private fun initializeLocales() {
        LocalizeService.provideLocale("en_US", JsonHttpLocaleProvider(
            "https://raw.githubusercontent.com/CowNetwork/translations/main/common/en_US.json",
            "https://raw.githubusercontent.com/CowNetwork/translations/main/proxy/en_US.json",
            "https://raw.githubusercontent.com/CowNetwork/translations/main/session/en_US.json"
        ))

        LocalizeService.provideLocale("de_DE", JsonHttpLocaleProvider(
            "https://raw.githubusercontent.com/CowNetwork/translations/main/common/de_DE.json",
            "https://raw.githubusercontent.com/CowNetwork/translations/main/proxy/de_DE.json",
            "https://raw.githubusercontent.com/CowNetwork/translations/main/session/de_DE.json"
        ))

        // Always load english and german locales.
        LocalizeService.setCoreLocale("en_US")
        LocalizeService.setCoreLocale("de_DE")

        LocalizeService.fallbackLocale = "en_US"
    }

    // TODO: logging
    private fun handleServerDiscovery() {
        // Get all servers currently registered.
        this.discoveryService.getServers().forEach(this.proxy::registerServer)

        // Listen for servers registering.
        this.discoveryService.addRegisterServerListener(this.proxy::registerServer)

        // Listen for servers unregistering.
        this.discoveryService.addUnregisterServerListener(this.proxy::unregisterServer)
    }

    private fun handlePlayerSessions() {
        // When a player joins the proxy, start a new session.
        this.proxy.eventManager.register(this, PostLoginEvent::class.java, PostOrder.FIRST) {
            val player = it.player
            val result = this.sessionService.startSession(player)

            // TODO: find another source for the locale as it has not yet been provided by the player.
            //       maybe use the locale of the last session, if there is one.
            LocalizeService.setLocale(player, player.getLocale())

            if (result is SessionInitialized) {
                this.logger.info("Session has been initialized for player ${player.username} (${player.uniqueId}).")
                return@register
            }

            if (result is SessionRejected) {
                // If the session could not be initialized, kick the player with the corresponding message.
                player.disconnect(result.cause.buildMessage(player))
                this.logger.info("Session couldn't be initialized for player ${player.username} (${player.uniqueId}). Cause: ${result.cause}.")
                return@register
            }
        }

        // When a player disconnected, stop the current session.
        this.proxy.eventManager.register(this, DisconnectEvent::class.java, PostOrder.FIRST) {
            this.sessionService.stopSession(it.player)
        }

        this.sessionService.addStopSessionListener { uuid, cause, session ->
            this.logger.info("Session has been stopped for player ${session.username} ($uuid) with reason $cause: $session")

            // If the session has been stopped because the player disconnected (see listener above), or the player is no longer online, do nothing.
            if (cause is SessionStopPlayerDisconnected) return@addStopSessionListener
            val player = this.proxy.getPlayer(uuid).orElse(null) ?: return@addStopSessionListener

            // Otherwise, build the message to kick the player with.
            player.disconnect(cause.buildMessage(player))
        }

        // Handle locale updates.
        this.proxy.eventManager.register(this, PlayerSettingsChangedEvent::class.java, PostOrder.FIRST) {
            val locale = it.getLocale()
            val player = it.player

            LocalizeService.setLocale(player, locale)
            this.sessionService.updateLocale(player.uniqueId, locale)
        }
    }

    // TODO: logging
    private fun handlePlayerDistribution() {
        this.proxy.eventManager.register(this, PlayerChooseInitialServerEvent::class.java, PostOrder.FIRST) {
            // Request the initial server for this player.
            val server = this.playerService.getInitialServer(it.player.uniqueId)?.let(this.proxy::getServer)

            // If the initial server does not exist, kick the player.
            if (server == null || !server.isPresent) {
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
            if (!server.isPresent) {
                val message = Component.text(Translations.ERROR_SERVER_NOT_FOUND).color(NamedTextColor.RED) // TODO: translate
                player.sendMessage(message, MessageType.SYSTEM)
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
