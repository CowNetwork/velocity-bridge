package network.cow.velocity.bridge

import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * @author Benedikt Wüller
 */

private val zone = ZoneId.of("Europe/Berlin")

fun getTimeZone(): ZoneId = zone

fun getCurrentDate(): ZonedDateTime = ZonedDateTime.now(getTimeZone())
