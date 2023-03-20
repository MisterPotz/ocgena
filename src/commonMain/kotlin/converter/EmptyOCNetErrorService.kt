package converter

import model.ConsistencyCheckError

object EmptyOCNetErrorService : OCNetErrorService {
    override fun errorsForPetriAtomId(petriAtomId: String): List<ConsistencyCheckError> {
        return emptyList()
    }
}
