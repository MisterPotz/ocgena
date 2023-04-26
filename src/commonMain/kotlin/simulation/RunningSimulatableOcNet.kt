package simulation

class RunningSimulatableOcNet(
    val composedOcNet : SimulatableComposedOcNet<*>,
    val state : SimulatableComposedOcNet.State
)
