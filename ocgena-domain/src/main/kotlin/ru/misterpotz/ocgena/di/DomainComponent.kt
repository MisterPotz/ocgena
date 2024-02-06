package ru.misterpotz.ocgena.di

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.SequenceStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dagger.Component
import dagger.Component.Factory
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import ru.misterpotz.DBLogger
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.ObjectTypeRegistry
import ru.misterpotz.ocgena.registries.ObjectTypeRegistryMap
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.PetriAtomRegistryStruct
import ru.misterpotz.ocgena.serialization.*
import ru.misterpotz.ocgena.simulation.config.TransitionsSpec
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.di.SimulationComponentDependencies
import javax.inject.Scope


@Module
class DomainModule {
    companion object {

        @Provides
        @DomainScope
        @JvmSuppressWildcards
        fun serializersModuleBlock(): SerializersModuleBuilder.() -> Unit {
            return {
                contextual(IntRangeSerializer("interval"))
                contextual(DurationSerializer("duration"))
                contextual(TimeUntilNextInstanceIsAllowedSerializer("timeUntilNextInstanceIsAllowed"))
                contextual(PeriodSerializer("period"))
                polymorphic(OCNet::class) {
                    subclass(OCNetStruct::class, OCNetStruct.serializer())
                    defaultDeserializer {
                        OCNetStruct.serializer()
                    }
                }
                polymorphic(ObjectTypeRegistry::class) {
                    defaultDeserializer {
                        ObjectTypeRegistryMap.serializer()
                    }
                    subclass(ObjectTypeRegistryMap::class, ObjectTypeRegistryMap.serializer())
                }
                polymorphic(PetriAtomRegistry::class) {
                    defaultDeserializer { type ->
                        PetriAtomRegistryStruct.serializer()
                    }
                    subclass(PetriAtomRegistryStruct::class, PetriAtomRegistryStruct.serializer())
                }
                polymorphic(PetriAtom::class) {
                    subclass(Place.serializer())
                    subclass(NormalArc.serializer())
                    subclass(VariableArc.serializer())
                    subclass(Transition.serializer())
                }
                polymorphic(ImmutablePlaceToObjectMarking::class) {
                    subclass(ImmutablePlaceToObjectMarkingMap.serializer())
                }
                polymorphic(PlaceToObjectMarking::class) {
                    subclass(PlaceToObjectMarkingMap.serializer())
                }
                polymorphic(TransitionsSpec::class) {
                    subclass(TransitionsOriginalSpec.serializer())
                    subclass(TransitionsTimePNSpec.serializer())
                }
            }
        }

        @Provides
        @DomainScope
        fun json(serializersModuleBlock: @JvmSuppressWildcards SerializersModuleBuilder.() -> Unit): Json {
            return Json {
                classDiscriminator = "type"
                prettyPrint = true
                encodeDefaults = false
                isLenient = true
                ignoreUnknownKeys = true
                serializersModule = SerializersModule {
                    serializersModuleBlock()
                }
            }
        }

        @Provides
        @DomainScope
        fun yaml(serializersModuleBlock: @JvmSuppressWildcards SerializersModuleBuilder.() -> Unit): Yaml {
            return Yaml(
                configuration = YamlConfiguration(
                    polymorphismPropertyName = "type",
                    sequenceStyle = SequenceStyle.Flow,
                    polymorphismStyle = PolymorphismStyle.Property,
                    strictMode = false,
                    encodeDefaults = false
                ),
                serializersModule = SerializersModule {
                    serializersModuleBlock()
                },
            )
        }
    }
}

@DomainScope
@Component(modules = [DomainModule::class])
interface DomainComponent {
    fun json(): Json
    fun yaml(): Yaml

    @Component.Factory
    interface Factory {
        fun create(): DomainComponent
    }

    companion object {
        fun create(): DomainComponent {
            return DaggerDomainComponent.factory().create()
        }
    }
}

@Scope
annotation class DomainScope
