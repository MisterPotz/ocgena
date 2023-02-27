package dsl

class NestedIdIssuer {
    private var idIssuer = IdIssuer()

    fun <R> build(block: Block.() -> R): R {
        val idStack = IdStack()
        val rootBlock = RootBlockImpl(idIssuer, idStack)
        return rootBlock.block()
    }

    fun getInlineBuilder(): Inline {
        val idStack = IdStack()
        return RootBlockImpl(idIssuer, idStack)
    }

    fun resetIdCounter() {
        idIssuer = IdIssuer()
    }

    interface Inline {
        val id : String
        val fullId : String
        fun enterNewRootCellScope() : String
        fun exitLastRootCellScope()
        fun newIdString() : String

        fun removeLast()
        fun resetScope()
    }

    interface Block : Inline {
        override val id : String
        override val fullId : String
        fun <R> cell(cellId: String, formBlock: Block.() -> R) : R
        fun <R> cell(formBlock: Block.() -> R) : R
        override fun enterNewRootCellScope() : String
        override fun exitLastRootCellScope()
        override fun newIdString() : String
        override fun resetScope()

        override fun removeLast()
    }

    private class RootBlockImpl(
        protected val idIssuer: IdIssuer,
        protected val idStack: IdStack,
    ) : Block {

        override val fullId: String
            get() = idStack.toId()

        override val id : String get() {
            return idStack.peek()
        }

        override fun <R> cell(cellId: String, formBlock: Block.() -> R) : R {
            idStack.push(cellId)
            val formBlock = RootBlockImpl(idIssuer, idStack)
            val toRet = formBlock.formBlock()
            idStack.pop()
            return toRet
        }

        override fun <R> cell(formBlock: Block.() -> R) : R {
            idStack.push(idIssuer.createIdString())
            val formBlock = RootBlockImpl(idIssuer, idStack)
            val toRet = formBlock.formBlock()
            idStack.pop()
            return toRet
        }

        override fun enterNewRootCellScope() : String {
            idStack.push(idIssuer.createIdString())
            val parentId = id
            return parentId
        }

        override fun exitLastRootCellScope() {
            if (idStack.hasElements()) {
                idStack.pop()
            }
        }

        override fun newIdString(): String {
            idStack.push(idIssuer.createIdString())
            val toRet = id
            idStack.pop()
            return toRet
        }

        override fun resetScope() {
            while (idStack.hasElements()) {
                idStack.pop()
            }
        }

        override fun removeLast() {
            idIssuer.decrease()
        }
    }

    class IdStack(val separator: String = "/") {
        private val stringBuilder = StringBuilder()
        private val idList = mutableListOf<String>()

        fun push(idSegment: String) {
            idList.add(idSegment)
        }

        fun pop(): String {
            return idList.removeLast()
        }

        fun peek() : String {
            return idList.last()
        }

        fun hasMoreThan1() : Boolean {
            return idList.size > 1
        }

        fun hasElements() : Boolean {
            return idList.isNotEmpty()
        }
        fun toId(): String {
            stringBuilder.clear()
            return idList.joinTo(stringBuilder, separator = separator).toString()
        }
    }
}

