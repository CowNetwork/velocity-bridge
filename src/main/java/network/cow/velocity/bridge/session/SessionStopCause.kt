package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.Player
import dev.benedikt.localize.translateSync
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.cow.velocity.bridge.Translations
import network.cow.velocity.bridge.toComponent
import network.cow.velocity.bridge.translateComponent
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
abstract class SessionStopCause(private val sessionId: UUID) {

    fun buildMessage(player: Player): Component {
        val sessionIdComponent = Component.text(this.sessionId.toString()).color(NamedTextColor.WHITE)
        return this.getComponent(player)
            .append(Component.newline())
            .append(Component.newline())
            .append(player.translateComponent(Translations.SESSION_ID, NamedTextColor.GRAY, sessionIdComponent))
    }

    protected open fun getComponent(player: Player): Component = Component.empty()

}

class SessionStopPlayerDisconnected(sessionId: UUID) : SessionStopCause(sessionId)

class SessionStopMaintenance(sessionId: UUID) : SessionStopCause(sessionId) {
    override fun getComponent(player: Player) = player.translateComponent(Translations.SESSION_STOPPED_MAINTENANCE, NamedTextColor.RED)
}

class SessionStopUnknown(sessionId: UUID) : SessionStopCause(sessionId) {
    override fun getComponent(player: Player) = player.translateComponent(Translations.SESSION_STOPPED_UNKNOWN, NamedTextColor.RED)
}

class SessionStopError(sessionId: UUID, private val error: String) : SessionStopCause(sessionId) {

    override fun getComponent(player: Player): Component {
        return player.translateComponent(Translations.SESSION_STOPPED_ERROR, NamedTextColor.RED)
            .append(Component.newline())
            .append(Component.newline())
            .append(player.translateComponent(Translations.COMMON_ERROR, NamedTextColor.GRAY, this.error.toComponent().color(NamedTextColor.WHITE)))
    }

}

class SessionStopPlayerKicked(sessionId: UUID, private val reason: String) : SessionStopCause(sessionId) {

    override fun getComponent(player: Player): Component {
        return player.translateComponent(Translations.SESSION_STOPPED_KICKED, NamedTextColor.RED)
            .append(Component.newline())
            .append(Component.newline())
            .append(player.translateComponent(Translations.COMMON_REASON, NamedTextColor.GRAY, this.reason.toComponent().color(NamedTextColor.WHITE)))
    }

}

class SessionStopPlayerBanned(
    sessionId: UUID,
    private val banId: UUID,
    private val reason: String,
    private val bannedAt: ZonedDateTime,
    private val duration: Duration
) : SessionStopCause(sessionId) {

    override fun getComponent(player: Player): Component {
        val banDurationComponent = if (duration.isNegative) {
            player.translateComponent(Translations.SESSION_BANNED_DURATION_PERMANENT, NamedTextColor.WHITE)
        } else {
            val dateTimeFormat = player.translateSync(Translations.COMMON_DATETIME)
            val formattedDate = DateTimeFormatter.ofPattern(dateTimeFormat).format(bannedAt.plusSeconds(duration.seconds))
            player.translateComponent(Translations.SESSION_BANNED_DURATION_UNTIL, formattedDate.toComponent())
        }

        return player.translateComponent(Translations.SESSION_STOPPED_BANNED, NamedTextColor.RED, banDurationComponent)
            .append(Component.newline())
            .append(Component.newline())
            .append(player.translateComponent(Translations.SESSION_BAN_ID, NamedTextColor.GRAY, this.banId.toComponent().color(NamedTextColor.WHITE)))
            .append(Component.newline())
            .append(player.translateComponent(Translations.COMMON_REASON, NamedTextColor.GRAY, this.reason.toComponent().color(NamedTextColor.WHITE)))
    }

}
