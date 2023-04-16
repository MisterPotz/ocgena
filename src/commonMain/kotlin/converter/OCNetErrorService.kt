package converter

import error.ConsistencyCheckError

interface OCNetErrorService {
    // observed consistency errors for given petri id
    fun errorsForPetriAtomId(petriAtomId: String): List<ConsistencyCheckError>
}