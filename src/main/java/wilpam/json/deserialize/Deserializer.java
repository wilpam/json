package wilpam.json.deserialize;

import wilpam.json.obj.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Parses JSON text into a [JsonType] tree.
public class Deserializer {
    /// Parses the given JSON content into a [JsonType] tree.
    ///
    /// @param content the JSON text
    /// @return the parsed value
    /// @throws IllegalArgumentException if the content is not valid JSON
    public static JsonType deserialize(String content){
        return deserialize(new StringReader(content));
    }

    /// Parses JSON read from the given [Reader] into a [JsonType] tree.
    ///
    /// The reader is read to end-of-stream, but not closed.
    ///
    /// @param reader the source of JSON text
    /// @return the parsed value
    /// @throws IllegalArgumentException if the input is not valid JSON
    public static JsonType deserialize(Reader reader){
        Parser p = new Parser(reader);
        return p.parse();
    }

    private static class Parser {
        private final Reader reader;
        private int current;

        private Parser(Reader reader) {
            this.reader = reader;
            this.current = -1;
        }

        public JsonType parse() {
            skipWhitespace();
            JsonType value = parseValue();
            skipWhitespace();
            if (peek() != -1) {
                throw new IllegalArgumentException("Unexpected trailing characters");
            }
            return value;
        }

        private JsonType parseValue() {
            int c = peek();
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> new JsonString(parseString());
                case 't' -> expectLiteral("true") ? new JsonBool(true) : error("Invalid literal");
                case 'f' -> expectLiteral("false") ? new JsonBool(false) : error("Invalid literal");
                case 'n' -> expectLiteral("null") ? new JsonNull() : error("Invalid literal");
                default -> {
                    if (c == -1) {
                        throw new IllegalArgumentException("Unexpected end of input");
                    }
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        yield new JsonNumber(parseNumber());
                    }
                    throw new IllegalArgumentException("Unexpected character: " + (char) c);
                }
            };
        }

        private JsonObject parseObject() {
            expect('{');
            Map<JsonString, JsonType> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() == '}') {
                next();
                return new JsonObject(map);
            }
            while (true) {
                skipWhitespace();
                if (peek() != '"') {
                    throw new IllegalArgumentException("Expected string key");
                }
                JsonString key = new JsonString(parseString());
                skipWhitespace();
                expect(':');
                skipWhitespace();
                JsonType value = parseValue();
                map.put(key, value);
                skipWhitespace();
                int c = next();
                if (c == '}') {
                    return new JsonObject(map);
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Expected ',' or '}'");
                }
            }
        }

        private JsonArray parseArray() {
            expect('[');
            List<JsonType> list = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') {
                next();
                return new JsonArray(list);
            }
            while (true) {
                skipWhitespace();
                list.add(parseValue());
                skipWhitespace();
                int c = next();
                if (c == ']') {
                    return new JsonArray(list);
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Expected ',' or ']'");
                }
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                int c = next();
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    int esc = next();
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> sb.append(parseUnicode());
                        default -> throw new IllegalArgumentException("Invalid escape: \\" + (char) esc);
                    }
                } else if (c == -1) {
                    throw new IllegalArgumentException("Unterminated string");
                } else {
                    sb.append((char) c);
                }
            }
        }

        private char parseUnicode() {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                int c = next();
                int digit = Character.digit(c, 16);
                if (digit < 0 || c == -1) {
                    throw new IllegalArgumentException("Invalid unicode escape");
                }
                value = (value << 4) | digit;
            }
            return (char) value;
        }

        private String parseNumber() {
            StringBuilder sb = new StringBuilder();
            if (peek() == '-') {
                sb.append((char) next());
            }
            int c = peek();
            if (c == '0') {
                sb.append((char) next());
            } else if (c >= '1' && c <= '9') {
                while (peek() >= '0' && peek() <= '9') {
                    sb.append((char) next());
                }
            } else {
                throw new IllegalArgumentException("Invalid number");
            }
            if (peek() == '.') {
                sb.append((char) next());
                if (peek() < '0' || peek() > '9') {
                    throw new IllegalArgumentException("Invalid number: missing fraction digits");
                }
                while (peek() >= '0' && peek() <= '9') {
                    sb.append((char) next());
                }
            }
            if (peek() == 'e' || peek() == 'E') {
                sb.append((char) next());
                if (peek() == '+' || peek() == '-') {
                    sb.append((char) next());
                }
                if (peek() < '0' || peek() > '9') {
                    throw new IllegalArgumentException("Invalid number: missing exponent digits");
                }
                while (peek() >= '0' && peek() <= '9') {
                    sb.append((char) next());
                }
            }
            return sb.toString();
        }

        private boolean expectLiteral(String literal) {
            for (int i = 0; i < literal.length(); i++) {
                if (next() != literal.charAt(i)) {
                    throw new IllegalArgumentException("Invalid literal");
                }
            }
            return true;
        }

        private void skipWhitespace() {
            while (peek() != -1) {
                int c = peek();
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    next();
                } else {
                    return;
                }
            }
        }

        private void expect(int expected) {
            int c = next();
            if (c != expected) {
                throw new IllegalArgumentException("Expected '" + (char) expected + "' but found '" + (char) c + "'");
            }
        }

        private int peek() {
            if (current == -1) {
                advance();
            }
            return current;
        }

        private int next() {
            int c = peek();
            advance();
            return c;
        }

        private void advance() {
            try {
                current = reader.read();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read input", e);
            }
        }

        private JsonType error(@SuppressWarnings("SameParameterValue") String message) {
            throw new IllegalArgumentException(message);
        }
    }
}