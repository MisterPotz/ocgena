package ru.misterpotz.ocgena.di

import com.charleskorn.kaml.SequenceStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.OCNetImpl
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.ObjectTypeRegistry
import ru.misterpotz.ocgena.registries.ObjectTypeRegistryMap
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.PetriAtomRegistryImpl
import ru.misterpotz.ocgena.serialization.DurationSerializer
import ru.misterpotz.ocgena.serialization.IntRangeSerializer
import ru.misterpotz.ocgena.serialization.PeriodSerializer
import ru.misterpotz.ocgena.serialization.TimeUntilNextInstanceIsAllowedSerializer
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
                    subclass(OCNetImpl::class, OCNetImpl.serializer())
                }
                polymorphic(ObjectTypeRegistry::class) {
                    subclass(ObjectTypeRegistryMap::class, ObjectTypeRegistryMap.serializer())
                }
                polymorphic(PetriAtomRegistry::class) {
                    subclass(PetriAtomRegistryImpl.serializer())
                }
                polymorphic(Arc::class) {
                    subclass(NormalArc.serializer())
                    subclass(VariableArc.serializer())
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
            }
        }

        @Provides
        @DomainScope
        fun json(serializersModuleBlock: @JvmSuppressWildcards SerializersModuleBuilder.() -> Unit): Json {
            return Json {
                prettyPrint = true
                serializersModule = SerializersModule {
                    serializersModuleBlock()
                }
            }
        }

        @Provides
        @DomainScope
        fun yaml(serializersModuleBlock: @JvmSuppressWildcards SerializersModuleBuilder.() -> Unit): Yaml {
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

    companion object {
        fun create(): DomainComponent {
            return DaggerDomainComponent.create()
        }
    }
}

@Scope
annotation class DomainScope
