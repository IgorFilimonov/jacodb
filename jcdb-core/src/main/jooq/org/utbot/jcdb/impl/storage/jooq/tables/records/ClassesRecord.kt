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
package org.utbot.jcdb.impl.storage.jooq.tables.records


import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record9
import org.jooq.Row9
import org.jooq.impl.UpdatableRecordImpl
import org.utbot.jcdb.impl.storage.jooq.tables.Classes


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class ClassesRecord() : UpdatableRecordImpl<ClassesRecord>(Classes.CLASSES), Record9<Long?, Int?, Long?, String?, ByteArray?, Long?, Long?, Long?, Long?> {

    var id: Long?
        set(value) = set(0, value)
        get() = get(0) as Long?

    var access: Int?
        set(value) = set(1, value)
        get() = get(1) as Int?

    var name: Long?
        set(value) = set(2, value)
        get() = get(2) as Long?

    var signature: String?
        set(value) = set(3, value)
        get() = get(3) as String?

    var bytecode: ByteArray?
        set(value) = set(4, value)
        get() = get(4) as ByteArray?

    var locationId: Long?
        set(value) = set(5, value)
        get() = get(5) as Long?

    var packageId: Long?
        set(value) = set(6, value)
        get() = get(6) as Long?

    var outerClass: Long?
        set(value) = set(7, value)
        get() = get(7) as Long?

    var outerMethod: Long?
        set(value) = set(8, value)
        get() = get(8) as Long?

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row9<Long?, Int?, Long?, String?, ByteArray?, Long?, Long?, Long?, Long?> = super.fieldsRow() as Row9<Long?, Int?, Long?, String?, ByteArray?, Long?, Long?, Long?, Long?>
    override fun valuesRow(): Row9<Long?, Int?, Long?, String?, ByteArray?, Long?, Long?, Long?, Long?> = super.valuesRow() as Row9<Long?, Int?, Long?, String?, ByteArray?, Long?, Long?, Long?, Long?>
    override fun field1(): Field<Long?> = Classes.CLASSES.ID
    override fun field2(): Field<Int?> = Classes.CLASSES.ACCESS
    override fun field3(): Field<Long?> = Classes.CLASSES.NAME
    override fun field4(): Field<String?> = Classes.CLASSES.SIGNATURE
    override fun field5(): Field<ByteArray?> = Classes.CLASSES.BYTECODE
    override fun field6(): Field<Long?> = Classes.CLASSES.LOCATION_ID
    override fun field7(): Field<Long?> = Classes.CLASSES.PACKAGE_ID
    override fun field8(): Field<Long?> = Classes.CLASSES.OUTER_CLASS
    override fun field9(): Field<Long?> = Classes.CLASSES.OUTER_METHOD
    override fun component1(): Long? = id
    override fun component2(): Int? = access
    override fun component3(): Long? = name
    override fun component4(): String? = signature
    override fun component5(): ByteArray? = bytecode
    override fun component6(): Long? = locationId
    override fun component7(): Long? = packageId
    override fun component8(): Long? = outerClass
    override fun component9(): Long? = outerMethod
    override fun value1(): Long? = id
    override fun value2(): Int? = access
    override fun value3(): Long? = name
    override fun value4(): String? = signature
    override fun value5(): ByteArray? = bytecode
    override fun value6(): Long? = locationId
    override fun value7(): Long? = packageId
    override fun value8(): Long? = outerClass
    override fun value9(): Long? = outerMethod

    override fun value1(value: Long?): ClassesRecord {
        this.id = value
        return this
    }

    override fun value2(value: Int?): ClassesRecord {
        this.access = value
        return this
    }

    override fun value3(value: Long?): ClassesRecord {
        this.name = value
        return this
    }

    override fun value4(value: String?): ClassesRecord {
        this.signature = value
        return this
    }

    override fun value5(value: ByteArray?): ClassesRecord {
        this.bytecode = value
        return this
    }

    override fun value6(value: Long?): ClassesRecord {
        this.locationId = value
        return this
    }

    override fun value7(value: Long?): ClassesRecord {
        this.packageId = value
        return this
    }

    override fun value8(value: Long?): ClassesRecord {
        this.outerClass = value
        return this
    }

    override fun value9(value: Long?): ClassesRecord {
        this.outerMethod = value
        return this
    }

    override fun values(value1: Long?, value2: Int?, value3: Long?, value4: String?, value5: ByteArray?, value6: Long?, value7: Long?, value8: Long?, value9: Long?): ClassesRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        this.value7(value7)
        this.value8(value8)
        this.value9(value9)
        return this
    }

    /**
     * Create a detached, initialised ClassesRecord
     */
    constructor(id: Long? = null, access: Int? = null, name: Long? = null, signature: String? = null, bytecode: ByteArray? = null, locationId: Long? = null, packageId: Long? = null, outerClass: Long? = null, outerMethod: Long? = null): this() {
        this.id = id
        this.access = access
        this.name = name
        this.signature = signature
        this.bytecode = bytecode
        this.locationId = locationId
        this.packageId = packageId
        this.outerClass = outerClass
        this.outerMethod = outerMethod
    }
}
