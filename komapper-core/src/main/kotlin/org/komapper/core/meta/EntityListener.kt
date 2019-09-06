package org.komapper.core.meta

interface EntityListener {

    fun <T> preDelete(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> postDelete(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> preInsert(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> postInsert(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> preUpdate(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> postUpdate(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> preMerge(entity: T, meta: EntityMeta<T>): T {
        return entity
    }

    fun <T> postMerge(entity: T, meta: EntityMeta<T>): T {
        return entity
    }
}

open class DefaultEntityListener : EntityListener
