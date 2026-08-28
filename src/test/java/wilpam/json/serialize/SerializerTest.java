package wilpam.json.serialize;

import wilpam.json.obj.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest {

    @Test
    void serializesNull() {
        assertEquals("null", Serializer.serialize(null));
    }

    @Test
    void serializesBooleans() {
        assertEquals("true", Serializer.serialize(true));
        assertEquals("false", Serializer.serialize(false));
    }

    @Test
    void serializesStrings() {
        assertEquals("\"hello\"", Serializer.serialize("hello"));
        assertEquals("\"\"", Serializer.serialize(""));
    }

    @Test
    void serializesChars() {
        assertEquals("\"a\"", Serializer.serialize('a'));
    }

    @Test
    void serializesNumbers() {
        assertEquals("42", Serializer.serialize(42));
        assertEquals("-17", Serializer.serialize(-17L));
        assertEquals("3.5", Serializer.serialize(3.5));
        assertEquals("1.25", Serializer.serialize(1.25f));
        assertEquals("7", Serializer.serialize((byte) 7));
        assertEquals("300", Serializer.serialize((short) 300));
    }

    @Test
    void serializesMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", "two");
        assertEquals("{\"a\":1,\"b\":\"two\"}", Serializer.serialize(map));
    }

    @Test
    void serializesMapWithNullAndBoolValues() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", null);
        map.put("y", true);
        assertEquals("{\"x\":null,\"y\":true}", Serializer.serialize(map));
    }

    @Test
    void serializesEmptyMap() {
        assertEquals("{}", Serializer.serialize(Map.of()));
    }

    @Test
    void serializesList() {
        assertEquals("[\"a\",2,false,null]", Serializer.serialize(Arrays.asList("a", 2, false, null)));
    }

    @Test
    void serializesEmptyList() {
        assertEquals("[]", Serializer.serialize(List.of()));
    }

    @Test
    void serializesArrays() {
        assertEquals("[1,2,3]", Serializer.serialize(new int[]{1, 2, 3}));
        assertEquals("[\"a\",\"b\"]", Serializer.serialize(new String[]{"a", "b"}));
        assertEquals("[[1],[2]]", Serializer.serialize(new int[][]{{1}, {2}}));
    }

    @Test
    void serializesNestedStructure() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("k", List.of(1, 2));
        Map<String, Object> outer = new LinkedHashMap<>();
        outer.put("inner", inner);
        outer.put("arr", new double[]{1.5});
        assertEquals("{\"inner\":{\"k\":[1,2]},\"arr\":[1.5]}", Serializer.serialize(outer));
    }

    @Test
    void serializesJsonType() {
        assertEquals("{\"a\":[1,\"b\",true,null]}", Serializer.serialize(
                new JsonObject(Map.of(new JsonString("a"), new JsonArray(List.of(
                        new JsonNumber("1"), new JsonString("b"), new JsonBool(true), new JsonNull()))))));
    }

    @Test
    void serializesRecord() {
        record Point(int x, double y, String label) {}
        assertEquals("{\"x\":1,\"y\":2.5,\"label\":\"p\"}", Serializer.serialize(new Point(1, 2.5, "p")));
    }

    @Test
    void serializesNestedRecord() {
        record Inner(String name) {}
        record Outer(String key, Inner inner) {}
        assertEquals("{\"key\":\"a\",\"inner\":{\"name\":\"b\"}}",
                Serializer.serialize(new Outer("a", new Inner("b"))));
    }

    @Test
    void serializesRecordWithFieldNameAnnotation() {
        record User(@FieldName("username") String name, int age) {}
        assertEquals("{\"username\":\"bob\",\"age\":30}", Serializer.serialize(new User("bob", 30)));
    }

    @Test
    void serializesRecordWithExtraFields() {
        record User(String name, int age) implements JsonExtraFields {
            @Override
            public Map<String, Object> extraFields() {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("email", "bob@example.com");
                m.put("verified", true);
                return m;
            }
        }
        assertEquals("{\"name\":\"bob\",\"age\":30,\"email\":\"bob@example.com\",\"verified\":true}",
                Serializer.serialize(new User("bob", 30)));
    }

    @Test
    void serializesRecordWithNullExtraFields() {
        record User(String name) implements JsonExtraFields {
            @Override
            public Map<String, Object> extraFields() {
                return null;
            }
        }
        assertEquals("{\"name\":\"x\"}", Serializer.serialize(new User("x")));
    }

    @Test
    void serializesJsonSerializable() {
        record Custom(int id) implements JsonSerializable {
            @Override
            public JsonType jsonSerialize() {
                return new JsonObject(Map.of(new JsonString("id"), new JsonNumber("7")));
            }
        }
        assertEquals("{\"id\":7}", Serializer.serialize(new Custom(7)));
    }

    @Test
    void escapesStringCharacters() {
        assertEquals("\"a\\\"b\"", Serializer.serialize("a\"b"));
        assertEquals("\"a\\\\b\"", Serializer.serialize("a\\b"));
        assertEquals("\"\\b\\f\\n\\r\\t\"", Serializer.serialize("\b\f\n\r\t"));
    }

    @Test
    void escapesControlCharacters() {
        assertEquals("\"\\u0001\"", Serializer.serialize("\u0001"));
    }

    @Test
    void serializesUnicode() {
        //noinspection UnnecessaryUnicodeEscape
        assertEquals("\"caf\u00e9\"", Serializer.serialize("caf\u00e9"));
    }

    @Test
    void rejectsUnsupportedTypes() {
        assertThrows(IllegalArgumentException.class, () -> Serializer.serialize(new Object()));
        assertThrows(IllegalArgumentException.class, () -> Serializer.serialize(new ByteArrayOutputStream()));
    }

    @Test
    void rejectsNonFiniteNumbers() {
        assertThrows(IllegalArgumentException.class, () -> Serializer.serialize(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Serializer.serialize(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> Serializer.serialize(Float.NaN));
    }
}