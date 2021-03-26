package network.cow.velocity.bridge.session

import network.cow.velocity.bridge.getCurrentDate
import java.time.ZonedDateTime

/**
 * @author Benedikt Wüller
 */
data class Session(val username: String, val startedAt: ZonedDateTime = getCurrentDate(), var stoppedAt: ZonedDateTime = getCurrentDate())
