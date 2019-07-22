package com.example.mensa.events

import java.util.*

data class RefreshMensaProgressEvent(val taskId: UUID, val max: Int, val progress: Int)