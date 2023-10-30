package ru.misterpotz.simulation.structure

class RunningSimulatableOcNet(
    val composedOcNet : SimulatableComposedOcNet<*>,
    val state : SimulatableComposedOcNet.State
)
