package org.komapper.core.meta2

import kotlin.reflect.KClass

open class MetaFactory {

    fun <T : Any> get(clazz: KClass<T>): Meta<T> {
        val metaName = clazz.java.name + "Meta"
        val metaClass: Class<Meta<T>> = Class.forName(metaName) as Class<Meta<T>>
        return metaClass.kotlin.objectInstance ?: metaClass.newInstance()
    }
}
