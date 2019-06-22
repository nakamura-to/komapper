package org.komapper.expr

enum class ExprTokenType {
    WHITESPACE,
    OPEN_PAREN,
    CLOSE_PAREN,
    VALUE,
    CHAR,
    STRING,
    INT,
    FLOAT,
    DOUBLE,
    LONG,
    BIG_DECIMAL,
    ILLEGAL_NUMBER,
    NULL,
    TRUE,
    FALSE,
    NOT,
    AND,
    OR,
    SAFE_CALL_FUNCTION,
    SAFE_CALL_PROPERTY,
    FUNCTION,
    PROPERTY,
    COMMA,
    EQ,
    NE,
    GT,
    LT,
    GE,
    LE,
    OTHER,
    EOE
}
