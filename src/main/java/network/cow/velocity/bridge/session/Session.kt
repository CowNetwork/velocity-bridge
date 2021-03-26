package network.cow.velocity.bridge.session

import com.velocitypowered.api.proxy.player.PlayerSettings
import network.cow.velocity.bridge.getCurrentDate
import java.time.ZonedDateTime

/**
 * @author Benedikt Wüller
 */
data class Session(
    val username: String,
    var locale: String,
    var chatMode: PlayerSettings.ChatMode,
    val startedAt: ZonedDateTime = getCurrentDate(),
    var stoppedAt: ZonedDateTime = getCurrentDate()
)
