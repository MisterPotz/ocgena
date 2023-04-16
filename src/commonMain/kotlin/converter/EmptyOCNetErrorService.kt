package converter

import error.ConsistencyCheckError

object EmptyOCNetErrorService : OCNetErrorService {
    override fun errorsForPetriAtomId(petriAtomId: String): List<ConsistencyCheckError> {
        return emptyList()
    }
}
