package koma


interface NamingStrategy {

    fun fromKotlinToDb(name: String): String {
        val buf = StringBuilder()
        for ((i, c) in name.withIndex()) {
            if (i > 0 && c.isUpperCase()) {
                buf.append('_')
            }
            buf.append(c.toLowerCase())
        }
        return buf.toString()
    }

}
