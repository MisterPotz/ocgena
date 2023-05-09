package converter

import dsl.OCNetFacadeBuilder

expect class OCDotToDomainConverter(
    conversionParams: ConversionParams,
) {
    fun convert() : OCNetFacadeBuilder.BuiltOCNet
}
