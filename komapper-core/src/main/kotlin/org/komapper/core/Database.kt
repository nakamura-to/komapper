package org.komapper.core

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.EntityQuery
import org.komapper.core.query.Query
import org.komapper.core.query.ScriptQuery
import org.komapper.core.query.scope.WhereDeclaration
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML

/**
 * A database.
 *
 * @property config the database configuration
 * @constructor creates a database instance
 */
class Database(val config: DefaultDatabaseConfig) {

    /**
     * A transaction scope initiator.
     */
    val transaction by lazy { config.transactionScopeInitiator }

    /**
     * A data type factory.
     */
    val factory = Factory(config)

    fun <ENTITY> find(metamodel: EntityMetamodel<ENTITY>, declaration: WhereDeclaration): ENTITY {
        return findOrNull(metamodel, declaration) ?: error("not found.")
    }

    fun <ENTITY> findOrNull(metamodel: EntityMetamodel<ENTITY>, declaration: WhereDeclaration): ENTITY? {
        val query = EntityQuery.from(metamodel).where(declaration).limit(1)
        return run(query).firstOrNull()
    }

    fun <ENTITY> first(block: () -> Query<List<ENTITY>>): ENTITY {
        return firstOrNull(block) ?: error("result is empty.")
    }

    fun <ENTITY> firstOrNull(block: () -> Query<List<ENTITY>>): ENTITY? {
        return run(block()).firstOrNull()
    }

    fun <ENTITY> first(query: Query<List<ENTITY>>): ENTITY {
        return firstOrNull(query) ?: error("result is empty.")
    }

    fun <ENTITY> firstOrNull(query: Query<List<ENTITY>>): ENTITY? {
        return run(query).firstOrNull()
    }

    fun <ENTITY> list(block: () -> Query<List<ENTITY>>): List<ENTITY> {
        return run(block())
    }

    fun <ENTITY> list(query: Query<List<ENTITY>>): List<ENTITY> {
        return run(query)
    }

    fun <ENTITY> insert(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY): ENTITY {
        val query = EntityQuery.insert(metamodel, entity)
        return run(query)
    }

    fun <ENTITY> update(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY): ENTITY {
        val query = EntityQuery.update(metamodel, entity)
        return run(query)
    }

    fun <ENTITY> delete(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY) {
        val query = EntityQuery.delete(metamodel, entity)
        run(query)
    }

    fun execute(block: () -> Query<Int>): Int {
        return run(block())
    }

    fun execute(query: Query<Int>): Int {
        return run(query)
    }

    fun script(sql: CharSequence) {
        val query = ScriptQuery.create(sql.toString())
        run(query)
    }

    private fun <T> run(query: Query<T>): T {
        return query.run(config)
    }

    class Factory(val config: DefaultDatabaseConfig) {
        /**
         * Creates Array objects.
         *
         * @param typeName the SQL name of the type the elements of the array map to
         * @param elements the elements that populate the returned object
         */
        fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array = config.connection.use {
            it.createArrayOf(typeName, elements.toTypedArray())
        }

        /**
         * Creates a Blob object.
         */
        fun createBlob(): Blob = config.connection.use {
            it.createBlob()
        }

        /**
         * Creates a Clob object.
         */
        fun createClob(): Clob = config.connection.use {
            it.createClob()
        }

        /**
         * Creates a NClob object.
         */
        fun createNClob(): NClob = config.connection.use {
            it.createNClob()
        }

        /**
         * Creates a SQLXML object.
         */
        fun createSQLXML(): SQLXML = config.connection.use {
            it.createSQLXML()
        }
    }
}
