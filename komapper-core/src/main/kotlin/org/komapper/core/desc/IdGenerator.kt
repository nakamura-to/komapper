package org.komapper.core.desc

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class IdGenerator(private val incrementBy: Int, private val callNextValue: () -> Long) {
    private val lock = ReentrantLock()
    private var base = 0L
    private var step = Long.MAX_VALUE

    fun next(): Long {
        return lock.withLock {
            if (step < incrementBy) {
                base + step++
            } else {
                callNextValue().also {
                    base = it
                    step = 1
                }
            }
        }
    }
}
