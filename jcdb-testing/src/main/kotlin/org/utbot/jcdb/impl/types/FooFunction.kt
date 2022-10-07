package org.utbot.jcdb.impl.types

open class FooFunction<T> {

    private var state: T? = null
    private var stateList: ArrayList<T> = arrayListOf()

    fun run1(incoming: T): T {
        state = incoming
        stateList.add(incoming)
        return incoming
    }

    fun <W : T> run2(incoming: List<W>): W {
        state = incoming.firstOrNull()
        stateList.addAll(incoming)
        return incoming.firstOrNull()!!
    }

}

class SuperFoo : FooFunction<String>()




