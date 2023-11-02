package ru.misterpotz.input.converter

import model.PlaceTyping
import model.Places
import model.Transitions

interface OCDotCompressedAST {
    val placeTyping: PlaceTyping
    val transitions: Transitions
    val places: Places
}
