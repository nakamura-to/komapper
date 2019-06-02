package org.komapper.meta

interface EntityListener {

    fun <T> preDelete(entity: T, meta: org.komapper.meta.EntityMeta<T>): T {
        return entity
    }

    fun <T> postDelete(entity: T, meta: org.komapper.meta.EntityMeta<T>): T {
        return entity
    }

    fun <T> preInsert(entity: T, meta: org.komapper.meta.EntityMeta<T>): T {
        return entity
    }

    fun <T> postInsert(entity: T, meta: org.komapper.meta.EntityMeta<T>): T {
        return entity
    }

    fun <T> preUpdate(entity: T, meta: org.komapper.meta.EntityMeta<T>): T {
        return entity
    }

    fun <T> postUpdate(entity: T, meta: org.komapper.meta.EntityMeta<T>): T {
        return entity
    }
}
