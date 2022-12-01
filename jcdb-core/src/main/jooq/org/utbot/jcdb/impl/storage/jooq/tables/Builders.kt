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
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row5
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.utbot.jcdb.impl.storage.jooq.DefaultSchema
import org.utbot.jcdb.impl.storage.jooq.keys.FK_BUILDERS_BYTECODELOCATIONS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_BUILDERS_SYMBOLS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_BUILDERS_SYMBOLS_2
import org.utbot.jcdb.impl.storage.jooq.tables.records.BuildersRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Builders(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, BuildersRecord>?,
    aliased: Table<BuildersRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<BuildersRecord>(
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
         * The reference instance of <code>Builders</code>
         */
        val BUILDERS = Builders()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<BuildersRecord> = BuildersRecord::class.java

    /**
     * The column <code>Builders.class_symbol_id</code>.
     */
    val CLASS_SYMBOL_ID: TableField<BuildersRecord, Long?> = createField(DSL.name("class_symbol_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Builders.builder_class_symbol_id</code>.
     */
    val BUILDER_CLASS_SYMBOL_ID: TableField<BuildersRecord, Long?> = createField(DSL.name("builder_class_symbol_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Builders.priority</code>.
     */
    val PRIORITY: TableField<BuildersRecord, Int?> = createField(DSL.name("priority"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>Builders.offset</code>.
     */
    val OFFSET: TableField<BuildersRecord, Int?> = createField(DSL.name("offset"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>Builders.location_id</code>.
     */
    val LOCATION_ID: TableField<BuildersRecord, Long?> = createField(DSL.name("location_id"), SQLDataType.BIGINT.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<BuildersRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<BuildersRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>Builders</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>Builders</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>Builders</code> table reference
     */
    constructor(): this(DSL.name("Builders"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, BuildersRecord>): this(Internal.createPathAlias(child, key), child, key, BUILDERS, null)
    override fun getSchema(): Schema = DefaultSchema.DEFAULT_SCHEMA
    override fun getReferences(): List<ForeignKey<BuildersRecord, *>> = listOf(FK_BUILDERS_SYMBOLS_2, FK_BUILDERS_SYMBOLS_1, FK_BUILDERS_BYTECODELOCATIONS_1)

    private lateinit var _fkBuildersSymbols_2: Symbols
    private lateinit var _fkBuildersSymbols_1: Symbols
    private lateinit var _bytecodelocations: Bytecodelocations
    fun fkBuildersSymbols_2(): Symbols {
        if (!this::_fkBuildersSymbols_2.isInitialized)
            _fkBuildersSymbols_2 = Symbols(this, FK_BUILDERS_SYMBOLS_2)

        return _fkBuildersSymbols_2;
    }
    fun fkBuildersSymbols_1(): Symbols {
        if (!this::_fkBuildersSymbols_1.isInitialized)
            _fkBuildersSymbols_1 = Symbols(this, FK_BUILDERS_SYMBOLS_1)

        return _fkBuildersSymbols_1;
    }
    fun bytecodelocations(): Bytecodelocations {
        if (!this::_bytecodelocations.isInitialized)
            _bytecodelocations = Bytecodelocations(this, FK_BUILDERS_BYTECODELOCATIONS_1)

        return _bytecodelocations;
    }
    override fun `as`(alias: String): Builders = Builders(DSL.name(alias), this)
    override fun `as`(alias: Name): Builders = Builders(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Builders = Builders(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Builders = Builders(name, null)

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row5<Long?, Long?, Int?, Int?, Long?> = super.fieldsRow() as Row5<Long?, Long?, Int?, Int?, Long?>
}
