package configuration

import java.time.LocalDate

object WorkingHoursConfiguration {
    var currentDate: LocalDate? = null
    var daysPerEmployee: MutableMap<String, MutableMap<LocalDate, Int>> = mutableMapOf()
}