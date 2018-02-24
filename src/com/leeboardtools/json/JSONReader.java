/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leeboardtools.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader for reading JSON objects, arrays, and values from a {@link Reader}.
 */
public class JSONReader {

    private final BufferedReader reader;
    private TokenType tokenType;
    private String tokenString;
    private double tokenNumber;
    private int cachedCodePoint;
    private int charactersRead;
    private int linesRead;
    private int charactersReadThisLine;

    /**
     * Constructor.
     * @param reader    The reader.
     */
    public JSONReader(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    /**
     * Constructor.
     * @param text The text to be read.
     */
    public JSONReader(String text) {
        this(new StringReader(text));
    }

    /**
     * Constructor.
     * @param stream The input stream to be read.
     */
    public JSONReader(InputStream stream) {
        this(new InputStreamReader(stream));
    }

    static enum ObjectState {
        OPENING_BRACE, NAME_OR_CLOSING_BRACE, NAME, COLON, VALUE, COMMA_OR_CLOSING_BRACE, DONE
    }

    /**
     * Reads a {@link JSONObject}.
     * @return  The object.
     * @throws IOException on I/O errors.
     * @throws ParsingException on parsing errors.
     */
    public JSONObject readJSONObject() throws IOException {
        JSONObject object = JSONLite.newJSONObject();
        ObjectState state = ObjectState.OPENING_BRACE;
        String currentName = null;
        while (true) {
            switch (state) {
                case OPENING_BRACE:
                    if (getTokenType() != TokenType.OBJECT_START) {
                        throwException("Invalid object, opening '{' not encountered.");
                    }
                    state = ObjectState.NAME_OR_CLOSING_BRACE;
                    advanceToken();
                    break;
                case NAME_OR_CLOSING_BRACE:
                case NAME:
                    if (getTokenType() == TokenType.OBJECT_END) {
                        if (state != ObjectState.NAME_OR_CLOSING_BRACE) {
                            throwException("Invalid object, closing '}' not allowed after a ','.");
                        }
                        advanceToken();
                        state = ObjectState.DONE;
                        break;
                    }
                    if (getTokenType() != TokenType.STRING) {
                        throwException("Invalid object, expected 'name'.");
                    }
                    currentName = getTokenString();
                    state = ObjectState.COLON;
                    advanceToken();
                    break;
                case COLON:
                    if (getTokenType() != TokenType.COLON) {
                        throwException("Invalid object, expected ':' after the 'name'.");
                    }
                    state = ObjectState.VALUE;
                    advanceToken();
                    break;
                case VALUE:
                    JSONValue value = readJSONValue();
                    object.add(currentName, value);
                    state = ObjectState.COMMA_OR_CLOSING_BRACE;
                    break;
                case COMMA_OR_CLOSING_BRACE:
                    switch (getTokenType()) {
                        case COMMA:
                            state = ObjectState.NAME;
                            break;
                        case OBJECT_END:
                            state = ObjectState.DONE;
                            break;
                        default:
                            throwException("Invalid object, expected either ',' or '}'.");
                            break;
                    }
                    advanceToken();
                    break;
                case DONE:
                    return object;
                default:
                    throw new AssertionError(state.name());
            }
        }
    }

    /**
     * Reads a {@link JSONValue}.
     * @return  The value.
     * @throws IOException on I/O errors.
     * @throws ParsingException on parsing errors.
     */
    public JSONValue readJSONValue() throws IOException {
        JSONValue value = null;
        switch (getTokenType()) {
            case ARRAY_START:
                JSONValue[] values = readJSONArray();
                return new JSONValue(values);
            case OBJECT_START:
                JSONObject object = readJSONObject();
                return new JSONValue(object);
            case STRING:
                value = new JSONValue(getTokenString());
                break;
            case NUMBER:
                value = new JSONValue(getTokenNumber());
                break;
            case TRUE:
                value = new JSONValue(true);
                break;
            case FALSE:
                value = new JSONValue(false);
                break;
            case NULL:
                value = new JSONValue();
                break;
            default:
                throwException("Invalid value, expected a string in '\"', a number, 'true', 'false', or 'null'.");
        }
        advanceToken();
        return value;
    }

    static enum ArrayState {
        OPENING_BRACKET, VALUE_OR_CLOSING_BRACKET, VALUE, COMMA_OR_CLOSING_BRACKET, DONE
    }

    /**
     * Reads a JSON array.
     * @return  The array.
     * @throws IOException on I/O errors.
     * @throws ParsingException on parsing errors.
     */
    public JSONValue[] readJSONArray() throws IOException {
        ArrayState state = ArrayState.OPENING_BRACKET;
        List<JSONValue> values = new ArrayList<>();
        while (true) {
            switch (state) {
                case OPENING_BRACKET:
                    if (getTokenType() != TokenType.ARRAY_START) {
                        throwException("Invalid array, expected '['.");
                    }
                    state = ArrayState.VALUE_OR_CLOSING_BRACKET;
                    advanceToken();
                    break;
                case VALUE_OR_CLOSING_BRACKET:
                case VALUE:
                    if (getTokenType() == TokenType.ARRAY_END) {
                        if (state != ArrayState.VALUE_OR_CLOSING_BRACKET) {
                            throwException("Invalid array, expected a value after ','.");
                        }
                        advanceToken();
                        return values.toArray(new JSONValue[values.size()]);
                    }
                    values.add(readJSONValue());
                    state = ArrayState.COMMA_OR_CLOSING_BRACKET;
                    break;
                case COMMA_OR_CLOSING_BRACKET:
                    if (getTokenType() == TokenType.ARRAY_END) {
                        advanceToken();
                        return values.toArray(new JSONValue[values.size()]);
                    } else if (getTokenType() != TokenType.COMMA) {
                        throwException("Invalid array, expected ',' or ']'.");
                    }
                    state = ArrayState.VALUE;
                    advanceToken();
                    break;
                case DONE:
                    break;
                default:
                    throw new AssertionError(state.name());
            }
        }
    }
    // Want to have the ability to read individual elements of an array.

    // Public so we could do something like detect arrays.
    public final TokenType getTokenType() throws IOException {
        if (this.tokenType == null) {
            advanceToken();
        }
        return this.tokenType;
    }

    public final String getTokenString() {
        return this.tokenString;
    }

    public final double getTokenNumber() {
        return this.tokenNumber;
    }

    final boolean advanceToken() throws IOException {
        // Skip white space...
        if (cachedCodePoint == 0) {
            readCodePoint();
        }
        while (JSONLite.isWhiteSpace(cachedCodePoint)) {
            readCodePoint();
        }
        int codePoint = cachedCodePoint;
        cachedCodePoint = 0;
        this.tokenType = codePointToTokenType(codePoint);
        if (this.tokenType == null) {
            throwException("Invalid token encountered.");
        }
        switch (this.tokenType) {
            case STRING:
                readJSONString();
                break;
            case NUMBER:
                readJSONNumber(codePoint);
                break;
            case TRUE:
                if ((readCodePoint() != 'r') || (readCodePoint() != 'u') || (readCodePoint() != 'e')) {
                    throwException("Invalid value, expected 'true'.");
                }
                this.cachedCodePoint = 0;
                break;
            case FALSE:
                if ((readCodePoint() != 'a') || (readCodePoint() != 'l') || (readCodePoint() != 's') || (readCodePoint() != 'e')) {
                    throwException("Invalid value, expected 'false'.");
                }
                this.cachedCodePoint = 0;
                break;
            case NULL:
                if ((readCodePoint() != 'u') || (readCodePoint() != 'l') || (readCodePoint() != 'l')) {
                    throwException("Invalid value, expected 'null'.");
                }
                this.cachedCodePoint = 0;
                break;
        }
        return true;
    }

    final void readJSONString() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        readCodePoint();
        while (cachedCodePoint != '"') {
            if (cachedCodePoint == -1) {
                throwException("Unterminated string encountered, end of file reached.");
            }
            if (cachedCodePoint == '\\') {
                cachedCodePoint = readCodePoint();
                switch (cachedCodePoint) {
                    case -1:
                        throwException("Unterminated string encountered in escape sequence, end of file reached.");
                    case '"':
                    case '\\':
                    case '/':
                        stringBuilder.appendCodePoint(cachedCodePoint);
                        break;
                    case 'b':
                        stringBuilder.append('\b');
                        break;
                    case 'f':
                        stringBuilder.append('\f');
                        break;
                    case 'n':
                        stringBuilder.append('\n');
                        break;
                    case 'r':
                        stringBuilder.append('\r');
                        break;
                    case 't':
                        stringBuilder.append('\t');
                        break;
                    case 'u':
                        int value = 0;
                        for (int i = 0; i < 4; ++i) {
                            value *= 16;
                            int codePoint = readCodePoint();
                            if (codePoint == -1) {
                                throwException("End of file reached reading '\\u' escape sequence.");
                            }
                            int digit = codePointToDigit(codePoint);
                            if (digit >= 0) {
                                value += digit;
                            } else if ((codePoint >= 0x0041) && (codePoint <= 0x0046)) {
                                value += 10 + (codePoint - 0x0041);
                            } else if ((codePoint >= 0x0061) && (codePoint <= 0x0066)) {
                                value += 10 + (codePoint - 0x0061);
                            } else {
                                throwException("Invalid hexadecimal character in the '\\u' escape sequence encountered.");
                            }
                        }
                        stringBuilder.appendCodePoint(value);
                        break;
                    default:
                        throwException("Invalid string escape character encountered.");
                }
            } else if ((cachedCodePoint >= 0) && (cachedCodePoint <= 0x01F)) {
                throwException("Invalid code point encountered in string, control characters must be escaped.");
            } else {
                stringBuilder.appendCodePoint(cachedCodePoint);
            }
            readCodePoint();
        }
        cachedCodePoint = 0;
        tokenString = stringBuilder.toString();
    }

    final void readJSONNumber(int firstCodePoint) throws IOException {
        double value = 0;
        int valuePower10 = 0;
        int exponentSign = 1;
        int exponent = 0;
        double valueSign = 1.;
        int codePoint = firstCodePoint;
        if (codePoint == '-') {
            valueSign = -1.;
            codePoint = readCodePoint();
        }
        int digit = codePointToDigit(codePoint);
        if (digit < 0) {
            throwException("Invalid number, a leading digit was expected.");
        } else if (digit > 0) {
            // Only a single leading digit is allowed.
            while (digit >= 0) {
                value *= 10.;
                value += digit;
                codePoint = readCodePoint();
                digit = codePointToDigit(codePoint);
            }
        } else {
            codePoint = readCodePoint();
        }
        if (codePoint == '.') {
            codePoint = readCodePoint();
            digit = codePointToDigit(codePoint);
            if (digit <= 0) {
                throwException("Invalid number, a digit is required after the decimal.");
            }
            while (digit >= 0) {
                value *= 10;
                value += digit;
                ++valuePower10;
                codePoint = readCodePoint();
                digit = codePointToDigit(codePoint);
            }
        }
        if ((codePoint == 'e') || (codePoint == 'E')) {
            codePoint = readCodePoint();
            if (codePoint == '+') {
                codePoint = readCodePoint();
            } else if (codePoint == '-') {
                codePoint = readCodePoint();
                exponentSign = -1;
            }
            digit = codePointToDigit(codePoint);
            if (codePoint == -1) {
                throwException("End of file reached reading the exponent.");
            } else if (digit < 0) {
                throwException("Invalid number, one or more digits are required for the exponent.");
            }
            while (digit >= 0) {
                exponent *= 10;
                exponent += digit;
                codePoint = readCodePoint();
                digit = codePointToDigit(codePoint);
            }
        }
        while (JSONLite.isWhiteSpace(codePoint)) {
            codePoint = readCodePoint();
        }
        TokenType nextTokenType = codePointToTokenType(codePoint);
        if (nextTokenType == null) {
            throwException("Invalid number, an invalid code point was encountered at the end of the number.");
        }
        switch (nextTokenType) {
            case COMMA:
            case ARRAY_END:
            case OBJECT_END:
            case END:
                break;
            default:
                throwException("Invalid number, an invalid code point was encountered at the end of the number.");
        }
        exponent *= exponentSign;
        exponent -= valuePower10;
        if (exponent < Double.MIN_EXPONENT) {
            throwException("Invalid number, the exponent is too small.");
        } else if (exponent > Double.MAX_EXPONENT) {
            throwException("Invalid number, the exponent is too large.");
        }
        this.tokenNumber = valueSign * value;
        if (exponent != 0) {
            this.tokenNumber *= Math.pow(10., exponent);
        }
    }

    final int readCodePoint() throws IOException {
        cachedCodePoint = this.reader.read();
        if (cachedCodePoint != -1) {
            if (cachedCodePoint == '\n') {
                ++this.linesRead;
                this.charactersReadThisLine = 0;
            }
            ++this.charactersRead;
            ++this.charactersReadThisLine;
        }
        return cachedCodePoint;
    }

    final void throwException(String message) {
        throw new ParsingException(message + " Line: " + this.linesRead + "  Col: " + (this.charactersReadThisLine + 1));
    }
    
    
    
    /**
     * The token types.
     */
    public enum TokenType {
        ARRAY_START,
        ARRAY_END,
        OBJECT_START,
        OBJECT_END,
        STRING,
        NUMBER,
        TRUE,
        FALSE,
        NULL,
        COLON,
        COMMA,
        END,
    }
    
    /**
     * Determines the most likely token type represented by a code point, if any.
     * @param codePoint The code point.
     * @return The token type, <code>null</code> if the code point cannot represent a token.
     */
    public static TokenType codePointToTokenType(int codePoint) {
        switch (codePoint) {
            case '[' :
                return TokenType.ARRAY_START;
            case ']' :
                return TokenType.ARRAY_END;
            case '{' :
                return TokenType.OBJECT_START;
            case '}' :
                return TokenType.OBJECT_END;
            case '"' :
                return TokenType.STRING;
            case 't' :
                return TokenType.TRUE;
            case 'f' :
                return TokenType.FALSE;
            case 'n' :
                return TokenType.NULL;
            case ':' :
                return TokenType.COLON;
            case ',' :
                return TokenType.COMMA;
            case -1 :
                return TokenType.END;
                
            case '-' :
                return TokenType.NUMBER;
                
            default :
                if (codePointToDigit(codePoint) >= 0) {
                    return TokenType.NUMBER;
                }
                break;
        }
        
        return null;
    }
    
    /**
     * Converts a Unicode code point to a digit according to JSON's rules.
     * @param codePoint The code point.
     * @return The digit equivalent between 0 and 9 inclusive, -1 if the code point is
     * not a valid digit.
     */
    public static int codePointToDigit(int codePoint) {
        return (codePoint >= 0x0030) && (codePoint <= 0x0039) ? (codePoint - 0x0030) : -1;
    }
    
}
