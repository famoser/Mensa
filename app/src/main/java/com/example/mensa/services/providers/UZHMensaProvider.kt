package com.example.mensa.services.providers

import java.time.DayOfWeek
import java.time.LocalDate

class UZHMensaProvider() {
    fun stuff() {
        var dayOfWeek = dayOfWeekToApiFormat(LocalDate.now().dayOfWeek)
    }


    private fun dayOfWeekToApiFormat(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "mo"
            DayOfWeek.TUESDAY -> "di"
            DayOfWeek.WEDNESDAY -> "mi"
            DayOfWeek.THURSDAY -> "do"
            DayOfWeek.FRIDAY -> "fre"
            DayOfWeek.SATURDAY -> "sa"
            DayOfWeek.SUNDAY -> "so"
        }
    }
}