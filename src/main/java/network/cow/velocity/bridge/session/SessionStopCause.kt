package network.cow.velocity.bridge.session

import net.kyori.adventure.text.Component
import network.cow.velocity.bridge.Translations
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
interface SessionStopCause {
    fun buildMessage(): Component = Component.empty()
}

class SessionStopPlayerDisconnected : SessionStopCause

class SessionStopMaintenance : SessionStopCause {
    override fun buildMessage() = Component.text(Translations.SESSION_STOPPED_MAINTENANCE) // TODO: translate
}

class SessionStopUnknown : SessionStopCause {
    override fun buildMessage() = Component.text(Translations.SESSION_STOPPED_UNKNOWN) // TODO: translate
}

class SessionStopError(val error: String) : SessionStopCause {
    override fun buildMessage(): Component {
        return Component.text(Translations.SESSION_STOPPED_ERROR) // TODO: translate
            .append(Component.newline())
            .append(Component.text(Translations.COMMON_ERROR)) // TODO: translate with error
    }
}

data class SessionStopPlayerKicked(val kickId: UUID, val reason: String) : SessionStopCause {

    override fun buildMessage(): Component {
        return Component.text(Translations.SESSION_STOPPED_KICKED) // TODO: translate
            .append(Component.newline())
            .append(Component.text(Translations.COMMON_REASON)) // TODO: translate with reason
            .append(Component.newline())
            .append(Component.text(Translations.COMMON_ID)) // TODO: translate with id
    }

}

data class SessionStopPlayerBanned(val banId: UUID, val reason: String, val bannedAt: ZonedDateTime, val duration: Duration) : SessionStopCause {

    override fun buildMessage(): Component {
        val durationKey = if (duration.isNegative) Translations.SESSION_BANNED_DURATION_PERMANENT else Translations.SESSION_BANNED_DURATION_UNTIL
        val bannedUntil = bannedAt.plusSeconds(if (duration.isNegative) Long.MAX_VALUE else duration.seconds)

        return Component.text(Translations.SESSION_STOPPED_BANNED) // TODO: translate with (durationKey and bannedUntil)
            .append(Component.newline())
            .append(Component.text(Translations.COMMON_REASON)) // TODO: translate with reason
            .append(Component.newline())
            .append(Component.text(Translations.COMMON_ID)) // TODO: translate with id
    }

}
