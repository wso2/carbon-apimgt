/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.commons.json.staxon;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamToken;

/**
 * Default <code>JsonStreamSource</code> implementation.
 */
final class JsonStreamSourceImpl implements JsonStreamSource {
    /**
     * Scanner interface
     */
    interface Scanner extends Closeable {
        enum Symbol {
            START_OBJECT("START_OBJECT"),
            SO_ARRAY("SO_ARRAY"),
            SO_ELEMENT("SO_ELEMENT"),
            SO_COLON_1("SO_COLON_1"),
            SO_COLON_2("SO_COLON_2"),
            SO_ARRAY_END("SO_ARRAY_END"),
            SO_ARRAY_END_2("SO_ARRAY_END_2"),
            SO_END("SO_END"),
            SO_END_2("SO_END_2"),
            END_OBJECT("END_OBJECT"),
            START_ARRAY("START_ARRAY"),
            END_ARRAY("END_ARRAY"),
            COLON("COLON"),
            COMMA("COMMA"),
            STRING("STRING"),
            NUMBER("NUMBER"),
            TRUE("TRUE"),
            FALSE("FALSE"),
            NULL("NULL"),
            EOF("EOF");

            private String name = null;
            Symbol(String name) {
                this.name = name;
            }
            public String toString(){
                return name;
            }
        }
        Symbol nextSymbol() throws IOException;
        String getText();

        int getCharOffset();
        int getLineNumber();
        int getColumnNumber();
    }

    private final Scanner scanner;
    private final boolean[] arrays = new boolean[64];
    private final boolean closeScanner;

    private JsonStreamToken token = null;
    private Scanner.Symbol symbol = null;
    private int depth = 0;
    private boolean peeked = false;

    private int lineNumber;
    private int columnNumber;
    private int charOffset;


    public JsonStreamSourceImpl(Scanner scanner, boolean closeScanner) {
        this.scanner = scanner;
        this.closeScanner = closeScanner;
        this.lineNumber = scanner.getLineNumber();
        this.columnNumber = scanner.getColumnNumber();
        this.charOffset = scanner.getCharOffset();
    }

    private JsonStreamToken startJsonValue() throws IOException {
        switch (symbol) {
            case FALSE:
            case NULL:
            case NUMBER:
            case TRUE:
            case STRING:
                return JsonStreamToken.VALUE;
            case START_ARRAY:
                if (arrays[depth]) {
                    throw new IOException("Already in an array");
                }
                arrays[depth] = true;
                return JsonStreamToken.START_ARRAY;
            case START_OBJECT:
                depth++;
                return JsonStreamToken.START_OBJECT;
            default:
                throw new IOException("Unexpected symbol: " + symbol);
        }
    }

    private void require(Scanner.Symbol expected) throws IOException {
        if (symbol != expected) {
            throw new IOException("Unexpected symbol:" + symbol);
        }
    }

    private JsonStreamToken next() throws IOException {
        symbol = scanner.nextSymbol();
        if (symbol == Scanner.Symbol.EOF) {
            if (depth != 0 || arrays[depth]) {
                throw new IOException("Premature EOF");
            }
            return JsonStreamToken.NONE;
        }
        if (token == null) {
            return startJsonValue();
        }
        switch (token) {
            case NAME:
                require(Scanner.Symbol.COLON);
                symbol = scanner.nextSymbol();
                return startJsonValue();
            case END_OBJECT:
            case END_ARRAY:
            case VALUE:
                switch (symbol) {
                    case COMMA:
                        symbol = scanner.nextSymbol();
                        if (arrays[depth]) {
                            return startJsonValue();
                        } else {
                            require(Scanner.Symbol.STRING);
                            return JsonStreamToken.NAME;
                        }
                    case END_ARRAY:
                        if (!arrays[depth]) {
                            throw new IOException("Not in an array");
                        }
                        arrays[depth] = false;
                        return JsonStreamToken.END_ARRAY;
                    case END_OBJECT:
                        if (arrays[depth]) {
                            throw new IOException("Unclosed array");
                        }
                        if (depth == 0) {
                            throw new IOException("Not in an object");
                        }
                        depth--;
                        return JsonStreamToken.END_OBJECT;
                    default:
                        throw new IOException("Unexpected symbol: " + symbol);
                }
            case START_OBJECT:
                switch (symbol) {
                    case END_OBJECT:
                        depth--;
                        return JsonStreamToken.END_OBJECT;
                    case STRING:
                        return JsonStreamToken.NAME;
                    default:
                        throw new IOException("Unexpected symbol: " + symbol);
                }
            case START_ARRAY:
                switch (symbol) {
                    case END_ARRAY:
                        arrays[depth] = false;
                        return JsonStreamToken.END_ARRAY;
                    default:
                        return startJsonValue();
                }
            default:
                throw new IOException("Unexpected token: " + token);
        }
    }

    public void close() throws IOException {
        if (closeScanner) {
            scanner.close();
        }
    }

    /**
     * Make the next token the current token.
     * Save location info from scanner to prevent changing location by peek()
     * @param token expected token
     * @throws IOException
     */
    public void poll(JsonStreamToken token) throws IOException {
        if (token != peek()) {
            throw new IOException("Unexpected token: " + peek());
        }
        lineNumber = scanner.getLineNumber();
        columnNumber = scanner.getColumnNumber();
        charOffset = scanner.getCharOffset();
        peeked = false;
    }

    public String name() throws IOException {
        poll(JsonStreamToken.NAME);
        return scanner.getText();
    }

    public Value value() throws IOException {
        poll(JsonStreamToken.VALUE);
        switch (symbol) {
            case NULL:
                return NULL;
            case STRING:
                return new Value(scanner.getText());
            case TRUE:
                return TRUE;
            case FALSE:
                return FALSE;
            case NUMBER:
                if (scanner.getText().indexOf('.') < 0 && scanner.getText().toLowerCase().indexOf('e') < 0) {
                    return new Value(scanner.getText(), new BigInteger(scanner.getText()));
                } else {
                    return new Value(scanner.getText(), new BigDecimal(scanner.getText()));
                }
            default:
                throw new IOException("Not a value token: " + symbol);
        }
    }

    public void startObject() throws IOException {
        poll(JsonStreamToken.START_OBJECT);
    }

    public void endObject() throws IOException {
        poll(JsonStreamToken.END_OBJECT);
    }

    public void startArray() throws IOException {
        poll(JsonStreamToken.START_ARRAY);
    }

    public void endArray() throws IOException {
        poll(JsonStreamToken.END_ARRAY);
    }

    public JsonStreamToken peek() throws IOException {
        if (!peeked) {
            token = next();
            peeked = true;
        }
        return token;
    }

    public int getLineNumber() {
        return lineNumber + 1;
    }

    public int getColumnNumber() {
        return columnNumber + 1;
    }

    public int getCharacterOffset() {
        return charOffset;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return null;
    }
}
