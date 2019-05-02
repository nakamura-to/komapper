package koma.sql

import koma.sql.SqlTokenType.*
import java.nio.CharBuffer

class SqlTokenizer(private val sql: String) {

    private var lineNumber: Int = 1
    private var position: Int = 0
    private var peekLineNumber: Int = 1
    private var lineStartPosition: Int = 0
    private var type: SqlTokenType = EOF
    var token: String = ""
        private set
    private val buf: CharBuffer = CharBuffer.wrap(sql)
    private val tokenBuf: CharBuffer = buf.asReadOnlyBuffer()

    val location
        get() = SqlLocation(sql, lineNumber, position)

    init {
        peek()
    }

    fun next(): SqlTokenType {
        when (type) {
            EOF -> {
                token = ""
                return EOF
            }
            EOL -> {
                lineStartPosition = buf.position()
            }
            else -> {
            }
        }
        val result = type
        prepare()
        peek()
        return result
    }

    private fun prepare() {
        lineNumber = peekLineNumber
        position = buf.position() - lineStartPosition
        tokenBuf.limit(buf.position())
        token = tokenBuf.toString()
        tokenBuf.position(buf.position())
    }

    private fun peek() {
        if (buf.hasRemaining()) {
            val c = buf.get()
            if (buf.hasRemaining()) {
                val c2 = buf.get()
                if (buf.hasRemaining()) {
                    val c3 = buf.get()
                    if (buf.hasRemaining()) {
                        val c4 = buf.get()
                        if (buf.hasRemaining()) {
                            val c5 = buf.get()
                            if (buf.hasRemaining()) {
                                val c6 = buf.get()
                                if (buf.hasRemaining()) {
                                    val c7 = buf.get()
                                    if (buf.hasRemaining()) {
                                        val c8 = buf.get()
                                        if (buf.hasRemaining()) {
                                            val c9 = buf.get()
                                            if (buf.hasRemaining()) {
                                                val c10 = buf.get()
                                                peekTenChars(c, c2, c3, c4, c5, c6, c7, c8, c9, c10)
                                            } else {
                                                peekNineChars(c, c2, c3, c4, c5, c6, c7, c8, c9)
                                            }
                                        } else {
                                            peekEightChars(c, c2, c3, c4, c5, c6, c7, c8)
                                        }
                                    } else {
                                        peekSevenChars(c, c2, c3, c4, c5, c6, c7)
                                    }
                                } else {
                                    peekSixChars(c, c2, c3, c4, c5, c6)
                                }
                            } else {
                                peekFiveChars(c, c2, c3, c4, c5)
                            }
                        } else {
                            peekFourChars(c, c2, c3, c4)
                        }
                    } else {
                        peekThreeChars(c, c2, c3)
                    }
                } else {
                    peekTwoChars(c, c2)
                }
            } else {
                peekOneChar(c)
            }
        } else {
            type = EOF
        }
    }

    private fun peekTenChars(
        c: Char, c2: Char, c3: Char, c4: Char, c5: Char, c6: Char, c7: Char, c8: Char, c9: Char, c10: Char
    ) {
        if ((c == 'f' || c == 'F')
            && (c2 == 'o' || c2 == 'O')
            && (c3 == 'r' || c3 == 'R')
            && isWhitespace(c4)
            && (c5 == 'u' || c5 == 'U')
            && (c6 == 'p' || c6 == 'P')
            && (c7 == 'd' || c7 == 'D')
            && (c8 == 'a' || c8 == 'A')
            && (c9 == 't' || c9 == 'T')
            && (c10 == 'e' || c10 == 'E')
        ) {
            type = FOR_UPDATE
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        peekNineChars(c, c2, c3, c4, c5, c6, c7, c8, c9)
    }

    private fun peekNineChars(
        c: Char, c2: Char, c3: Char, c4: Char, c5: Char, c6: Char, c7: Char, c8: Char, c9: Char
    ) {
        if ((c == 'i' || c == 'I')
            && (c2 == 'n' || c2 == 'N')
            && (c3 == 't' || c3 == 'T')
            && (c4 == 'e' || c4 == 'E')
            && (c5 == 'r' || c5 == 'R')
            && (c6 == 's' || c6 == 'S')
            && (c7 == 'e' || c7 == 'E')
            && (c8 == 'c' || c8 == 'C')
            && (c9 == 't' || c9 == 'T')
        ) {
            type = INTERSECT
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        peekEightChars(c, c2, c3, c4, c5, c6, c7, c8)
    }

    private fun peekEightChars(
        c: Char, c2: Char, c3: Char, c4: Char, c5: Char, c6: Char, c7: Char, c8: Char
    ) {
        if ((c == 'g' || c == 'G')
            && (c2 == 'r' || c2 == 'R')
            && (c3 == 'o' || c3 == 'O')
            && (c4 == 'u' || c4 == 'U')
            && (c5 == 'p' || c5 == 'P')
            && isWhitespace(c6)
            && (c7 == 'b' || c7 == 'B')
            && (c8 == 'y' || c8 == 'Y')
        ) {
            type = GROUP_BY
            if (isWordTerminated()) {
                return
            }
        } else if ((c == 'o' || c == 'O')
            && (c2 == 'r' || c2 == 'R')
            && (c3 == 'd' || c3 == 'D')
            && (c4 == 'e' || c4 == 'E')
            && (c5 == 'r' || c5 == 'R')
            && Character.isWhitespace(c6)
            && (c7 == 'b' || c7 == 'B')
            && (c8 == 'y' || c8 == 'Y')
        ) {
            type = ORDER_BY
            if (isWordTerminated()) {
                return
            }
        } else if ((c == 'o' || c == 'O')
            && (c2 == 'p' || c2 == 'P')
            && (c3 == 't' || c3 == 'T')
            && (c4 == 'i' || c4 == 'I')
            && (c5 == 'o' || c5 == 'O')
            && (c6 == 'n' || c6 == 'N')
            && isWhitespace(c7)
            && c8 == '('
        ) {
            type = OPTION
            buf.position(buf.position() - 2)
            return
        }
        buf.position(buf.position() - 1)
        peekSevenChars(c, c2, c3, c4, c5, c6, c7)
    }

    private fun peekSevenChars(
        c: Char,
        c2: Char,
        c3: Char,
        c4: Char,
        c5: Char,
        c6: Char,
        @Suppress("UNUSED_PARAMETER") c7: Char
    ) {
        buf.position(buf.position() - 1)
        peekSixChars(c, c2, c3, c4, c5, c6)
    }

    private fun peekSixChars(c: Char, c2: Char, c3: Char, c4: Char, c5: Char, c6: Char) {
        if ((c == 's' || c == 'S')
            && (c2 == 'e' || c2 == 'E')
            && (c3 == 'l' || c3 == 'L')
            && (c4 == 'e' || c4 == 'E')
            && (c5 == 'c' || c5 == 'C')
            && (c6 == 't' || c6 == 'T')
        ) {
            type = SELECT
            if (isWordTerminated()) {
                return
            }
        } else if ((c == 'h' || c == 'H')
            && (c2 == 'a' || c2 == 'A')
            && (c3 == 'v' || c3 == 'V')
            && (c4 == 'i' || c4 == 'I')
            && (c5 == 'n' || c5 == 'N')
            && (c6 == 'g' || c6 == 'G')
        ) {
            type = HAVING
            if (isWordTerminated()) {
                return
            }
        } else if ((c == 'e' || c == 'E')
            && (c2 == 'x' || c2 == 'X')
            && (c3 == 'c' || c3 == 'C')
            && (c4 == 'e' || c4 == 'E')
            && (c5 == 'p' || c5 == 'P')
            && (c6 == 't' || c6 == 'T')
        ) {
            type = EXCEPT
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        peekFiveChars(c, c2, c3, c4, c5)
    }

    private fun peekFiveChars(c: Char, c2: Char, c3: Char, c4: Char, c5: Char) {
        if ((c == 'w' || c == 'W')
            && (c2 == 'h' || c2 == 'H')
            && (c3 == 'e' || c3 == 'E')
            && (c4 == 'r' || c4 == 'R')
            && (c5 == 'e' || c5 == 'E')
        ) {
            type = WHERE
            if (isWordTerminated()) {
                return
            }
        } else if ((c == 'u' || c == 'U')
            && (c2 == 'n' || c2 == 'N')
            && (c3 == 'i' || c3 == 'I')
            && (c4 == 'o' || c4 == 'O')
            && (c5 == 'n' || c5 == 'N')
        ) {
            type = UNION
            if (isWordTerminated()) {
                return
            }
        } else if ((c == 'm' || c == 'M')
            && (c2 == 'i' || c2 == 'I')
            && (c3 == 'n' || c3 == 'N')
            && (c4 == 'u' || c4 == 'U')
            && (c5 == 's' || c5 == 'S')
        ) {
            type = MINUS
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        peekFourChars(c, c2, c3, c4)
    }

    private fun peekFourChars(c: Char, c2: Char, c3: Char, c4: Char) {
        if ((c == 'f' || c == 'F')
            && (c2 == 'r' || c2 == 'R')
            && (c3 == 'o' || c3 == 'O')
            && (c4 == 'm' || c4 == 'M')
        ) {
            type = FROM
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        peekThreeChars(c, c2, c3)
    }

    private fun peekThreeChars(c: Char, c2: Char, c3: Char) {
        if ((c == 'a' || c == 'A') && (c2 == 'n' || c2 == 'N') && (c3 == 'd' || c3 == 'D')) {
            type = AND
            if (isWordTerminated()) {
                return
            }

        }
        buf.position(buf.position() - 1)
        peekTwoChars(c, c2)
    }

    private fun peekTwoChars(c: Char, c2: Char) {
        if ((c == 'o' || c == 'O') && (c2 == 'r' || c2 == 'R')) {
            type = OR
            if (isWordTerminated()) {
                return
            }
        } else if (c == '/' && c2 == '*') {
            type = MULTI_LINE_COMMENT
            if (buf.hasRemaining()) {
                val c3 = buf.get()
                if (isExpressionIdentifierStart(c3)) {
                    type = BIND_VALUE_DIRECTIVE
                } else if (c3 == '^') {
                    type = LITERAL_VALUE_DIRECTIVE
                } else if (c3 == '#') {
                    type = EMBEDDED_VALUE_DIRECTIVE
                } else if (c3 == '%') {
                    if (buf.hasRemaining()) {
                        val c4 = buf.get()
                        if (buf.hasRemaining()) {
                            val c5 = buf.get()
                            if (c4 == 'i' && c5 == 'f') {
                                if (isDirectiveTerminated()) {
                                    type = IF_DIRECTIVE
                                }
                            } else if (buf.hasRemaining()) {
                                val c6 = buf.get()
                                if (c4 == 'f' && c5 == 'o' && c6 == 'r') {
                                    if (isDirectiveTerminated()) {
                                        type = FOR_DIRECTIVE
                                    }
                                } else if (c4 == 'e' && c5 == 'n' && c6 == 'd') {
                                    if (isDirectiveTerminated()) {
                                        type = END_DIRECTIVE
                                    }
                                } else if (buf.hasRemaining()) {
                                    val c7 = buf.get()
                                    if (c4 == 'e' && c5 == 'l' && c6 == 's' && c7 == 'e') {
                                        if (isDirectiveTerminated()) {
                                            type = ELSE_DIRECTIVE
                                        } else {
                                            if (buf.hasRemaining()) {
                                                val c8 = buf.get()
                                                if (buf.hasRemaining()) {
                                                    val c9 = buf.get()
                                                    if (c8 == 'i' && c9 == 'f') {
                                                        if (isDirectiveTerminated()) {
                                                            type = ELSEIF_DIRECTIVE
                                                        }
                                                    } else {
                                                        buf.position(buf.position() - 6)
                                                    }
                                                } else {
                                                    buf.position(buf.position() - 5)
                                                }
                                            }
                                        }
                                    } else if (buf.hasRemaining()) {
                                        val c8 = buf.get()
                                        if (buf.hasRemaining()) {
                                            val c9 = buf.get()
                                            if (c4 == 'e' && c5 == 'x' && c6 == 'p' && c7 == 'a' && c8 == 'n'
                                                && c9 == 'd'
                                            ) {
                                                if (isDirectiveTerminated()) {
                                                    type = EXPAND_DIRECTIVE
                                                }
                                            } else {
                                                buf.position(buf.position() - 6)
                                            }
                                        } else {
                                            buf.position(buf.position() - 5)
                                        }
                                    } else {
                                        buf.position(buf.position() - 4)
                                    }
                                } else {
                                    buf.position(buf.position() - 3)
                                }
                            } else {
                                buf.position(buf.position() - 2)
                            }
                        } else {
                            buf.position(buf.position() - 1)
                        }
                    }
                    if (type !== IF_DIRECTIVE
                        && type !== FOR_DIRECTIVE
                        && type !== END_DIRECTIVE
                        && type !== ELSE_DIRECTIVE
                        && type !== ELSEIF_DIRECTIVE
                        && type !== EXPAND_DIRECTIVE
                    ) {
                        throw SqlException("Unsupported directive name is found at $location")
                    }
                }
                buf.position(buf.position() - 1)
            }
            while (buf.hasRemaining()) {
                val c3 = buf.get()
                if (buf.hasRemaining()) {
                    buf.mark()
                    val c4 = buf.get()
                    if (c3 == '*' && c4 == '/') {
                        return
                    }
                    if (c3 == '\r' && c4 == '\n' || c3 == '\r' || c3 == '\n') {
                        peekLineNumber++
                    }
                    buf.reset()
                }
            }
            throw SqlException("The token \"*/\" for the end of the multi-line comment is not found at $location")
        } else if (c == '-' && c2 == '-') {
            type = SINGLE_LINE_COMMENT
            while (buf.hasRemaining()) {
                buf.mark()
                val c3 = buf.get()
                if (c3 == '\r' || c3 == '\n') {
                    buf.reset()
                    return
                }
            }
            return
        } else if (c == '\r' && c2 == '\n') {
            type = EOL
            peekLineNumber++
            return
        }
        buf.position(buf.position() - 1)
        peekOneChar(c)
    }

    private fun peekOneChar(c: Char) {
        if (isWhitespace(c)) {
            type = WHITESPACE
        } else if (c == '(') {
            type = OPEN_BRACKET
        } else if (c == ')') {
            type = CLOSE_BRACKET
        } else if (c == ';') {
            type = DELIMITER
        } else if (c == '\'') {
            type = QUOTE
            var closed = false
            while (buf.hasRemaining()) {
                val c2 = buf.get()
                if (c2 == '\'') {
                    if (buf.hasRemaining()) {
                        buf.mark()
                        val c3 = buf.get()
                        if (c3 != '\'') {
                            buf.reset()
                            closed = true
                            break
                        }
                    } else {
                        closed = true
                    }
                }
            }
            if (closed) {
                return
            }
            throw SqlException("The token \"'\" for the end of the string literal is not found at $location")
        } else if (isWordStart(c)) {
            type = WORD
            while (buf.hasRemaining()) {
                buf.mark()
                val c2 = buf.get()
                if (c2 == '\'') {
                    var closed = false
                    while (buf.hasRemaining()) {
                        val c3 = buf.get()
                        if (c3 == '\'') {
                            if (buf.hasRemaining()) {
                                buf.mark()
                                val c4 = buf.get()
                                if (c4 != '\'') {
                                    buf.reset()
                                    closed = true
                                    break
                                }
                            } else {
                                closed = true
                            }
                        }
                    }
                    if (closed) {
                        return
                    }
                    throw SqlException("The token \"'\" for the end of the string literal is not found at $location")
                }
                if (!isWordPart(c2)) {
                    buf.reset()
                    return
                }
            }
        } else if (c == '\r' || c == '\n') {
            type = EOL
            peekLineNumber++
        } else {
            type = OTHER
        }
    }

    private fun isWordStart(c: Char): Boolean {
        if (c == '+' || c == '-') {
            buf.mark()
            if (buf.hasRemaining()) {
                val c2 = buf.get()
                buf.reset()
                if (Character.isDigit(c2)) {
                    return true
                }
            }
        }
        return isWordPart(c)
    }

    private fun isWordPart(c: Char): Boolean {
        if (Character.isWhitespace(c)) {
            return false
        }
        return when (c) {
            '=', '<', '>', '-', ',', '/', '*', '+', '(', ')', ';' -> false
            else -> true
        }
    }

    private fun isWordTerminated(): Boolean {
        buf.mark()
        if (buf.hasRemaining()) {
            val c = buf.get()
            buf.reset()
            if (!isWordPart(c)) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    private fun isExpressionIdentifierStart(c: Char): Boolean {
        return (Character.isJavaIdentifierStart(c)
                || Character.isWhitespace(c)
                || c == '"'
                || c == '\'')
    }

    private fun isDirectiveTerminated(): Boolean {
        buf.mark()
        if (buf.hasRemaining()) {
            val c = buf.get()
            buf.reset()
            if (!isWordPart(c)) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    private fun isWhitespace(c: Char): Boolean {
        return when (c) {
            '\u0009', '\u000B', '\u000C', '\u001C', '\u001D', '\u001E', '\u001F', '\u0020' -> true
            else -> false
        }
    }

}
