package org.komapper.tx

class TransactionException : Exception {
    constructor(message: String) : super(message)
    constructor(e: Exception) : super(e)
}
