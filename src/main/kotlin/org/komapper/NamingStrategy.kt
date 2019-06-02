package org.komapper

import java.nio.CharBuffer


interface NamingStrategy {

    fun fromKotlinToDb(name: String): String {
        val builder = StringBuilder()
        val buf = CharBuffer.wrap(name)
        while (buf.hasRemaining()) {
            val c1 = buf.get()
            builder.append(c1.toLowerCase())
            buf.mark()
            if (buf.hasRemaining()) {
                val c2 = buf.get()
                if (!c1.isUpperCase() && c2.isUpperCase()) {
                    builder.append("_")
                }
                buf.reset()
            }
        }
        return builder.toString()
    }

}
