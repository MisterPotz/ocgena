package ru.misterpotz.ocgena.simulation.structure

class RunningSimulatableOcNet(
    val composedOcNet : SimulatableComposedOcNet<*>,
    val state : SimulatableComposedOcNet.State
)
