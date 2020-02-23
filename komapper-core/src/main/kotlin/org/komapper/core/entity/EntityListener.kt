package org.komapper.core.entity

interface EntityListener<T : Any> {
    fun preDelete(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun postDelete(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun preInsert(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun postInsert(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun preUpdate(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun postUpdate(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun preMerge(entity: T, desc: EntityDesc<T>): T {
        return entity
    }

    fun postMerge(entity: T, desc: EntityDesc<T>): T {
        return entity
    }
}
