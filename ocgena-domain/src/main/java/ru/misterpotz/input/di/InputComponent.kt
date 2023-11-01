package ru.misterpotz.input.di

import dagger.Component
import dagger.Module


interface InputComponentDependenies {

}

@Module
abstract class InputModule {

}

@Component(
    dependencies = [
        InputComponentDependenies::class
    ],
    modules = [
        InputModule::class
    ]
)
class InputComponent() {
}