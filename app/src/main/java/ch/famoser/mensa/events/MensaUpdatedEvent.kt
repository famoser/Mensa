package ch.famoser.mensa.events

import ch.famoser.mensa.models.Mensa

data class MensaUpdatedEvent(val mensa: Mensa)
data class MensasUpdatedEvent(val mensas: List<Mensa>)