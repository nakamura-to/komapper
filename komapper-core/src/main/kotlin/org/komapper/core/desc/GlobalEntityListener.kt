package org.komapper.core.desc

interface GlobalEntityListener {

    fun <T : Any> preDelete(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> postDelete(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> preInsert(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> postInsert(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> preUpdate(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> postUpdate(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> preMerge(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T : Any> postMerge(entity: T, desc: EntityDesc<T>): T {
        return entity
    }
}

open class DefaultGlobalEntityListener : GlobalEntityListener
