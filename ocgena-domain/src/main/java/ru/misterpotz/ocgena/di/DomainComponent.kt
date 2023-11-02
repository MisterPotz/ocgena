package ru.misterpotz.ocgena.di

import dagger.Component
import dagger.Provides
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import model.*
import model.typea.SerializableVariableArcTypeA
import model.typel.SerializableArcTypeL
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import ru.misterpotz.marking.objects.EmptyObjectValuesMap
import ru.misterpotz.marking.objects.ObjectValuesMap
import ru.misterpotz.ocgena.ocnet.primitives.SerializableAtom
import ru.misterpotz.simulation.di.SimulationComponentDependencies
import ru.misterpotz.simulation.state.SerializableState
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import javax.inject.Scope

class DomainModule {
    @Provides
    @ru.misterpotz.ocgena.di.DomainScope
    fun json(): Json {
        return Json {
            prettyPrint = true
            serializersModule = SerializersModule {
                polymorphic(baseClass = SerializableAtom::class) {
                    subclass(SerializablePlace::class, SerializablePlace.serializer())
                    subclass(SerializableTransition::class, SerializableTransition.serializer())
                    subclass(SerializableNormalArc::class, SerializableNormalArc.serializer())
                    subclass(SerializableArcTypeL::class, SerializableArcTypeL.serializer())
                    subclass(SerializableVariableArcTypeA::class, SerializableVariableArcTypeA.serializer())
                }
                polymorphic(SimulatableComposedOcNet.SerializableState::class) {
                    subclass(SerializableState::class, SerializableState.serializer())
                }
                polymorphic(ObjectValuesMap::class) {
                    subclass(EmptyObjectValuesMap::class, EmptyObjectValuesMap.serializer())
                }
            }
        }
    }

    @Provides
    @ru.misterpotz.ocgena.di.DomainScope
    fun yaml(): Yaml {
        return Yaml {
            this.listSerialization = YamlBuilder.ListSerialization.AUTO
            this.mapSerialization = YamlBuilder.MapSerialization.BLOCK_MAP

            serializersModule = SerializersModule {
                polymorphic(baseClass = SerializableAtom::class) {
                    subclass(SerializablePlace::class, SerializablePlace.serializer())
                    subclass(SerializableTransition::class, SerializableTransition.serializer())
                    subclass(SerializableNormalArc::class, SerializableNormalArc.serializer())
                    subclass(SerializableArcTypeL::class, SerializableArcTypeL.serializer())
                    subclass(SerializableVariableArcTypeA::class, SerializableVariableArcTypeA.serializer())
                }

                polymorphic(SimulatableComposedOcNet.SerializableState::class) {
                    subclass(SerializableState::class, SerializableState.serializer())
                }
                polymorphic(ObjectValuesMap::class) {
                    subclass(EmptyObjectValuesMap::class, EmptyObjectValuesMap.serializer())
                }
            }
        }
    }
}

@ru.misterpotz.ocgena.di.DomainScope
@Component(modules = [ru.misterpotz.ocgena.di.DomainModule::class])
interface DomainComponent : SimulationComponentDependencies {

}

@Scope
annotation class DomainScope
