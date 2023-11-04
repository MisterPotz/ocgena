package ru.misterpotz.ocgena.di

import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import model.*
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import ru.misterpotz.ocgena.simulation.di.SimulationComponentDependencies
import javax.inject.Scope

@Module
class DomainModule {
    companion object {
        @Provides
        @DomainScope
        fun json(): Json {
            return Json {
                prettyPrint = true
                serializersModule = SerializersModule {
//                polymorphic(baseClass = SerializableAtom::class) {
//                    subclass(SerializablePlace::class, SerializablePlace.serializer())
//                    subclass(SerializableTransition::class, SerializableTransition.serializer())
//                    subclass(SerializableNormalArc::class, SerializableNormalArc.serializer())
//                    subclass(SerializableArcTypeL::class, SerializableArcTypeL.serializer())
//                    subclass(SerializableVariableArcTypeA::class, SerializableVariableArcTypeA.serializer())
//                }
//                polymorphic(SimulatableComposedOcNet.SerializableState::class) {
//                    subclass(SerializableState::class, SerializableState.serializer())
//                }
//                polymorphic(ObjectValuesMap::class) {
//                    subclass(EmptyObjectValuesMap::class, EmptyObjectValuesMap.serializer())
//                }
                }
            }
        }

        @Provides
        @DomainScope
        fun yaml(): Yaml {
            return Yaml {
                this.listSerialization = YamlBuilder.ListSerialization.AUTO
                this.mapSerialization = YamlBuilder.MapSerialization.BLOCK_MAP

                serializersModule = SerializersModule {
//                polymorphic(baseClass = SerializableAtom::class) {
//                    subclass(SerializablePlace::class, SerializablePlace.serializer())
//                    subclass(SerializableTransition::class, SerializableTransition.serializer())
//                    subclass(SerializableNormalArc::class, SerializableNormalArc.serializer())
//                    subclass(SerializableArcTypeL::class, SerializableArcTypeL.serializer())
//                    subclass(SerializableVariableArcTypeA::class, SerializableVariableArcTypeA.serializer())
//                }
//
//                polymorphic(SimulatableComposedOcNet.SerializableState::class) {
//                    subclass(SerializableState::class, SerializableState.serializer())
//                }
//                polymorphic(ObjectValuesMap::class) {
//                    subclass(EmptyObjectValuesMap::class, EmptyObjectValuesMap.serializer())
//                }
                }
            }
        }
    }
}

@DomainScope
@Component(modules = [DomainModule::class])
interface DomainComponent : SimulationComponentDependencies {

}

@Scope
annotation class DomainScope
