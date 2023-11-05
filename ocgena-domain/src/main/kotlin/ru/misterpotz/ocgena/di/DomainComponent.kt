package ru.misterpotz.ocgena.di

import com.charleskorn.kaml.SequenceStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import ru.misterpotz.ocgena.serialization.DurationSerializer
import ru.misterpotz.ocgena.serialization.IntRangeSerializer
import ru.misterpotz.ocgena.serialization.TimeUntilNextInstanceIsAllowedSerializer
import ru.misterpotz.ocgena.simulation.di.SimulationComponentDependencies
import javax.inject.Scope

@Module
class DomainModule {
    companion object {

        @Provides
        @DomainScope
        fun serializersModuleBlock() : SerializersModuleBuilder.() -> Unit {
            return {
                contextual(IntRangeSerializer("interval"))
                contextual(DurationSerializer("duration"))
                contextual(TimeUntilNextInstanceIsAllowedSerializer("timeUntilNextInstanceIsAllowed"))
            }
        }
        @Provides
        @DomainScope
        fun json(serializersModuleBlock : SerializersModuleBuilder.() -> Unit): Json {
            return Json {
                prettyPrint = true
                serializersModule = SerializersModule {
                    serializersModuleBlock()
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
            return Yaml(
                serializersModule = SerializersModule {
                    serializersModuleBlock()
                },
                configuration = YamlConfiguration(
                    sequenceStyle = SequenceStyle.Flow
                )
            )
        }
    }
}

@DomainScope
@Component(modules = [DomainModule::class])
interface DomainComponent : SimulationComponentDependencies {

}

@Scope
annotation class DomainScope
