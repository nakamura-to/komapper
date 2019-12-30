package org.komapper.core.desc

interface EntityListener {

    fun <T> preDelete(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> postDelete(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> preInsert(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> postInsert(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> preUpdate(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> postUpdate(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> preMerge(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun <T> postMerge(entity: T, desc: EntityDesc<T>): T {
        return entity
    }
}

open class DefaultEntityListener : EntityListener
