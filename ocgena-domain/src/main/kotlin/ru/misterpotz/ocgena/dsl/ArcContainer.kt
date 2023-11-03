package ru.misterpotz.ocgena.dsl

import dsl.ArcDSL

interface ArcContainer {
    val arcs : MutableList<ArcDSL>
}
