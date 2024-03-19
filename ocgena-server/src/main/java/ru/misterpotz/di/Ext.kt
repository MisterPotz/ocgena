package ru.misterpotz.di

import org.sqlite.core.DB
import ru.misterpotz.db.DBConnectionSetupper


const val SIM_DB = "simdb"
const val OCEL_DB = "oceldb"


fun Map<@JvmSuppressWildcards String,
        @JvmSuppressWildcards DBConnectionSetupper.Connection>.getSimDB()
        : DBConnectionSetupper.Connection {
    return this[SIM_DB]!!
}

fun Map<@JvmSuppressWildcards String,
        @JvmSuppressWildcards DBConnectionSetupper.Connection>.getOcelDB()
        : DBConnectionSetupper.Connection {
    return this[OCEL_DB]!!
}
