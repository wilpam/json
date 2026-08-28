package wilpam.json.deserialize;

import wilpam.json.obj.*;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeserializerTest {

    @Test
    void deserializesNull() {
        assertEquals(new JsonNull(), Deserializer.deserialize("null"));
    }

    @Test
    void deserializesBooleans() {
        assertEquals(new JsonBool(true), Deserializer.deserialize("true"));
        assertEquals(new JsonBool(false), Deserializer.deserialize("false"));
    }

    @Test
    void deserializesString() {
        assertEquals(new JsonString("hello"), Deserializer.deserialize("\"hello\""));
    }

    @Test
    void deserializesEmptyString() {
        assertEquals(new JsonString(""), Deserializer.deserialize("\"\""));
    }

    @Test
    void deserializesIntegers() {
        assertEquals(new JsonNumber("0"), Deserializer.deserialize("0"));
        assertEquals(new JsonNumber("42"), Deserializer.deserialize("42"));
        assertEquals(new JsonNumber("-17"), Deserializer.deserialize("-17"));
    }

    @Test
    void deserializesDoubles() {
        assertEquals(new JsonNumber("3.14"), Deserializer.deserialize("3.14"));
        assertEquals(new JsonNumber("-0.5"), Deserializer.deserialize("-0.5"));
    }

    @Test
    void deserializesExponents() {
        assertEquals(new JsonNumber("1e10"), Deserializer.deserialize("1e10"));
        assertEquals(new JsonNumber("2.5E-3"), Deserializer.deserialize("2.5E-3"));
        assertEquals(new JsonNumber("-1.5e+2"), Deserializer.deserialize("-1.5e+2"));
    }

    @Test
    void deserializesEmptyObject() {
        assertEquals(new JsonObject(Map.of()), Deserializer.deserialize("{}"));
    }

    @Test
    void deserializesEmptyArray() {
        assertEquals(new JsonArray(List.of()), Deserializer.deserialize("[]"));
    }

    @Test
    void deserializesObject() {
        JsonObject expected = new JsonObject(Map.of(
                new JsonString("name"), new JsonString("wilpam"),
                new JsonString("age"), new JsonNumber("30"),
                new JsonString("active"), new JsonBool(true),
                new JsonString("nothing"), new JsonNull()));
        assertEquals(expected, Deserializer.deserialize(
                "{\"name\":\"wilpam\",\"age\":30,\"active\":true,\"nothing\":null}"));
    }

    @Test
    void preservesObjectOrder() {
        JsonObject obj = (JsonObject) Deserializer.deserialize("{\"b\":1,\"a\":2}");
        assertEquals(List.of("b", "a"), obj.map().keySet().stream()
                .map(JsonString::string).toList());
    }

    @Test
    void deserializesArray() {
        JsonArray expected = new JsonArray(List.of(
                new JsonString("a"), new JsonNumber("2"), new JsonBool(false), new JsonNull()));
        assertEquals(expected, Deserializer.deserialize("[\"a\", 2, false, null]"));
    }

    @Test
    void deserializesNested() {
        JsonArray expected = new JsonArray(List.of(
                new JsonObject(Map.of(new JsonString("x"), new JsonNumber("1"))),
                new JsonArray(List.of(new JsonString("y")))));
        assertEquals(expected, Deserializer.deserialize("[{\"x\":1}, [\"y\"]]"));
    }

    @Test
    void handlesSurroundingWhitespace() {
        assertEquals(new JsonNumber("42"), Deserializer.deserialize("  \n\t 42 \r\n"));
    }

    @Test
    void handlesWhitespaceInside() {
        assertEquals(new JsonObject(Map.of(new JsonString("a"), new JsonNumber("1"))),
                Deserializer.deserialize("{ \"a\" : 1 }"));
    }

    @Test
    void deserializesEscapes() {
        assertEquals(new JsonString("a\"b"), Deserializer.deserialize("\"a\\\"b\""));
        assertEquals(new JsonString("a\\b"), Deserializer.deserialize("\"a\\\\b\""));
        assertEquals(new JsonString("a/b"), Deserializer.deserialize("\"a\\/b\""));
        assertEquals(new JsonString("a\bb"), Deserializer.deserialize("\"a\\bb\""));
        assertEquals(new JsonString("a\fb"), Deserializer.deserialize("\"a\\fb\""));
        assertEquals(new JsonString("a\nb"), Deserializer.deserialize("\"a\\nb\""));
        assertEquals(new JsonString("a\rb"), Deserializer.deserialize("\"a\\rb\""));
        assertEquals(new JsonString("a\tb"), Deserializer.deserialize("\"a\\tb\""));
        //noinspection UnnecessaryUnicodeEscape
        assertEquals(new JsonString("a\u00e9b"), Deserializer.deserialize("\"a\\u00e9b\""));
    }

    @Test
    void deserializesEmoji() {
        assertEquals(new JsonString("\ud83d\ude00"), Deserializer.deserialize("\"\ud83d\ude00\""));
    }

    @Test
    void deserializesFromReader() {
        assertEquals(new JsonNumber("1"), Deserializer.deserialize(new StringReader("1")));
    }

    @Test
    void rejectsTrailingCharacters() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("true false"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("42x"));
    }

    @Test
    void rejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize(""));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("   "));
    }

    @Test
    void rejectsMalformedLiterals() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("tru"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("TRUE"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("nul"));
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("01"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("1."));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("1e"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("-"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize(".5"));
    }

    @Test
    void rejectsUnterminatedString() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("\"abc"));
    }

    @Test
    void rejectsInvalidEscape() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("\"\\x\""));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("\"\\u12g4\""));
    }

    @Test
    void rejectsMalformedObjects() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("{"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("{\"a\"}"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("{\"a\":}"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("{a:1}"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("{\"a\":1 \"b\":2}"));
    }

    @Test
    void rejectsMalformedArrays() {
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("["));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("]"));
        assertThrows(IllegalArgumentException.class, () -> Deserializer.deserialize("[1 2]"));
    }
}