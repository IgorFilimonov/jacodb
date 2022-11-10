package org.utbot.jcdb.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob

val BACKGROUND_PARALLELISM
    get() = Integer.getInteger(
        "jcdb.background.parallelism",
        64.coerceAtLeast(Runtime.getRuntime().availableProcessors())
    )

class BackgroundScope : CoroutineScope {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val coroutineContext = Dispatchers.IO.limitedParallelism(BACKGROUND_PARALLELISM) + SupervisorJob()
}