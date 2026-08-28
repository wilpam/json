package wilpam.json;

import wilpam.json.deserialize.Deserializer;
import wilpam.json.obj.*;
import wilpam.json.serialize.FieldName;
import wilpam.json.serialize.Serializer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoundTripTest {

    @Test
    void roundTripsPrimitives() {
        assertRoundTrips("null");
        assertRoundTrips("true");
        assertRoundTrips("false");
        assertRoundTrips("\"hello\"");
        assertRoundTrips("42");
        assertRoundTrips("-17");
        assertRoundTrips("3.14");
        assertRoundTrips("-1.5e+2");
    }

    @Test
    void roundTripsNestedStructure() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("list", new int[]{1, 2, 3});
        nested.put("deep", Map.of("a", "b", "c", true));

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", "wilpam");
        root.put("nested", nested);
        root.put("empty", Map.of());
        root.put("none", null);

        String json = Serializer.serialize(root);
        JsonType parsed = Deserializer.deserialize(json);

        JsonObject expected = new JsonObject(Map.of(
                new JsonString("name"), new JsonString("wilpam"),
                new JsonString("nested"), new JsonObject(Map.of(
                        new JsonString("list"), new JsonArray(List.of(
                                new JsonNumber("1"), new JsonNumber("2"), new JsonNumber("3"))),
                        new JsonString("deep"), new JsonObject(Map.of(
                                new JsonString("a"), new JsonString("b"),
                                new JsonString("c"), new JsonBool(true))))),
                new JsonString("empty"), new JsonObject(Map.of()),
                new JsonString("none"), new JsonNull()));

        assertEquals(expected, parsed);

        assertEquals(json, Serializer.serialize(parsed));
    }

    @Test
    void roundTripsRecordWithFieldName() {
        record Point(@FieldName("x") int first,
                     @FieldName("y") int second,
                     String label) {}
        record Result(Point point, List<String> tags) {}

        String json = Serializer.serialize(new Result(new Point(1, 2, "p1"), new ArrayList<>(List.of("a", "b"))));
        JsonObject obj = (JsonObject) Deserializer.deserialize(json);
        JsonObject point = (JsonObject) obj.map().get(new JsonString("point"));
        assertNotNull(point.map().get(new JsonString("x")));
        assertNotNull(point.map().get(new JsonString("y")));
        assertNull(point.map().get(new JsonString("first")));
    }

    private static void assertRoundTrips(String json) {
        assertEquals(json, Serializer.serialize(Deserializer.deserialize(json)));
    }
}