package ru.misterpotz.ocgena.ocnet.primitives

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OcNetType {
    @SerialName("aalst")
    AALST,
    @SerialName("lomazova")
    LOMAZOVA
}
