package network.cow.velocity.bridge

import dev.benedikt.localize.LocalizeService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

/**
 * @author Benedikt Wüller
 */

object Translations {
    const val COMMON_ID = "common.id"
    const val COMMON_REASON = "common.reason"
    const val COMMON_ERROR = "common.error"

    const val COMMON_DATETIME = "common.date_time.date_time_format"

    const val ERROR_INITIAL_SERVER_NOT_FOUND = "proxy.errors.initial_server_not_found"
    const val ERROR_SERVER_NOT_FOUND = "proxy.errors.server_not_found"
    const val ERROR_SERVER_CONNECTION_FAILED = "proxy.errors.server_connection_failed"

    const val SESSION_ID = "session.id"
    const val SESSION_BAN_ID = "session.ban_id"
    const val SESSION_STOPPED_KICKED = "session.stop_causes.kicked"
    const val SESSION_STOPPED_BANNED = "session.stop_causes.banned"
    const val SESSION_STOPPED_MAINTENANCE = "session.stop_causes.maintenance"
    const val SESSION_STOPPED_ERROR = "session.stop_causes.error"
    const val SESSION_STOPPED_UNKNOWN = "session.stop_causes.unknown"

    const val SESSION_BANNED_DURATION_PERMANENT = "session.banned.duration_permanent"
    const val SESSION_BANNED_DURATION_UNTIL = "session.banned.duration_until"
}

fun Any.translateComponent(key: String, vararg params: Component): Component {
    return this.translateComponent(key, NamedTextColor.WHITE, *params)
}

fun Any.translateComponent(key: String, color: TextColor, vararg params: Component): Component {
    val locale = LocalizeService.getLocale(this)
    val format = LocalizeService.getFormatSync(locale, key)

    val placeholderRegex = Regex("(%\\d+\\\$s)")
    val placeholders = placeholderRegex.findAll(format).map(MatchResult::value).toList()
    val sections = format.split(placeholderRegex)

    val serializedParams = params.map { GsonComponentSerializer.gson().serialize(it) }.toTypedArray()
    var component = Component.empty()
    sections.forEachIndexed { index, section ->
        component = component.append(Component.text(section).color(color))
        val placeholder = placeholders.getOrNull(index) ?: return@forEachIndexed
        component = component.append(GsonComponentSerializer.gson().deserialize(placeholder.format(*serializedParams)))
    }
    return component
}

fun Any.toComponent(): Component = Component.text(this.toString())
