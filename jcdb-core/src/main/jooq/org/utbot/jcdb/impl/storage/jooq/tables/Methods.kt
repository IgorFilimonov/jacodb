/*
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * This file is generated by jOOQ.
 */
package org.utbot.jcdb.impl.storage.jooq.tables


import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Index
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row7
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.utbot.jcdb.impl.storage.jooq.DefaultSchema
import org.utbot.jcdb.impl.storage.jooq.indexes.METHODS_CLASS_ID_NAME_DESC
import org.utbot.jcdb.impl.storage.jooq.keys.FK_METHODS_CLASSES_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_METHODS_SYMBOLS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_METHODS_SYMBOLS_2
import org.utbot.jcdb.impl.storage.jooq.keys.PK_METHODS
import org.utbot.jcdb.impl.storage.jooq.tables.records.MethodsRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Methods(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, MethodsRecord>?,
    aliased: Table<MethodsRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<MethodsRecord>(
    alias,
    DefaultSchema.DEFAULT_SCHEMA,
    child,
    path,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table()
) {
    companion object {

        /**
         * The reference instance of <code>Methods</code>
         */
        val METHODS = Methods()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<MethodsRecord> = MethodsRecord::class.java

    /**
     * The column <code>Methods.id</code>.
     */
    val ID: TableField<MethodsRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Methods.access</code>.
     */
    val ACCESS: TableField<MethodsRecord, Int?> = createField(DSL.name("access"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>Methods.name</code>.
     */
    val NAME: TableField<MethodsRecord, Long?> = createField(DSL.name("name"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Methods.signature</code>.
     */
    val SIGNATURE: TableField<MethodsRecord, String?> = createField(DSL.name("signature"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>Methods.desc</code>.
     */
    val DESC: TableField<MethodsRecord, String?> = createField(DSL.name("desc"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>Methods.return_class</code>.
     */
    val RETURN_CLASS: TableField<MethodsRecord, Long?> = createField(DSL.name("return_class"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Methods.class_id</code>.
     */
    val CLASS_ID: TableField<MethodsRecord, Long?> = createField(DSL.name("class_id"), SQLDataType.BIGINT.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<MethodsRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<MethodsRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>Methods</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>Methods</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>Methods</code> table reference
     */
    constructor(): this(DSL.name("Methods"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, MethodsRecord>): this(Internal.createPathAlias(child, key), child, key, METHODS, null)
    override fun getSchema(): Schema = DefaultSchema.DEFAULT_SCHEMA
    override fun getIndexes(): List<Index> = listOf(METHODS_CLASS_ID_NAME_DESC)
    override fun getPrimaryKey(): UniqueKey<MethodsRecord> = PK_METHODS
    override fun getKeys(): List<UniqueKey<MethodsRecord>> = listOf(PK_METHODS)
    override fun getReferences(): List<ForeignKey<MethodsRecord, *>> = listOf(FK_METHODS_SYMBOLS_2, FK_METHODS_SYMBOLS_1, FK_METHODS_CLASSES_1)

    private lateinit var _fkMethodsSymbols_2: Symbols
    private lateinit var _fkMethodsSymbols_1: Symbols
    private lateinit var _classes: Classes
    fun fkMethodsSymbols_2(): Symbols {
        if (!this::_fkMethodsSymbols_2.isInitialized)
            _fkMethodsSymbols_2 = Symbols(this, FK_METHODS_SYMBOLS_2)

        return _fkMethodsSymbols_2;
    }
    fun fkMethodsSymbols_1(): Symbols {
        if (!this::_fkMethodsSymbols_1.isInitialized)
            _fkMethodsSymbols_1 = Symbols(this, FK_METHODS_SYMBOLS_1)

        return _fkMethodsSymbols_1;
    }
    fun classes(): Classes {
        if (!this::_classes.isInitialized)
            _classes = Classes(this, FK_METHODS_CLASSES_1)

        return _classes;
    }
    override fun `as`(alias: String): Methods = Methods(DSL.name(alias), this)
    override fun `as`(alias: Name): Methods = Methods(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Methods = Methods(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Methods = Methods(name, null)

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row7<Long?, Int?, Long?, String?, String?, Long?, Long?> = super.fieldsRow() as Row7<Long?, Int?, Long?, String?, String?, Long?, Long?>
}
