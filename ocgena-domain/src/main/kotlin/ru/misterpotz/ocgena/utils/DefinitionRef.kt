package ru.misterpotz.ocgena.utils

annotation class DefinitionRef(val ref: String) {
}

annotation class TimePNRef(val ref: String, val author: String = "Popova-Zeugmann")

annotation class OCAalstRef(val ref: String, val author: String = "Aalst")

annotation class OCLomazovaRef(val ref: String, val author: String = "Lomazova")