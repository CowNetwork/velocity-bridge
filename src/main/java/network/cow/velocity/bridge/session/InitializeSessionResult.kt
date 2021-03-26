package network.cow.velocity.bridge.session

/**
 * @author Benedikt Wüller
 */
interface InitializeSessionResult

class SessionInitialized : InitializeSessionResult

data class SessionRejected(val cause: SessionStopCause) : InitializeSessionResult
