package wilpam.json.serialize;

import wilpam.json.obj.*;

import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Map;

/// Serializes Java values into JSON text.
///
/// The following types are supported:
///  * `null`, booleans, strings, characters and numbers
///  * maps (string keys only) serialized as objects
///  * arrays and collections serialized as JSON arrays
///  * records serialized as objects via their components
///    * They may additionally implement [JsonExtraFields] to have extra entries
///    * They may annotate components with [FieldName] to rename keys.
///  * [JsonType] values written out directly
///  * any object implementing [JsonSerializable], whose returned [JsonType] is used
public class Serializer {
    /// Serializes the given value to a JSON string.
    ///
    /// @param value the value to serialize
    /// @return the JSON text
    /// @throws IllegalArgumentException if the value (or something reachable
    ///         from it) is of an unsupported type or a non-finite number \[e.g. NaN\]
    public static String serialize(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof JsonNull) {
            sb.append("null");
        } else if (value instanceof JsonType jsonType) {
            writeJsonType(sb, jsonType);
        } else if (value instanceof String s) {
            writeString(sb, s);
        } else if (value instanceof Character c) {
            writeString(sb, String.valueOf(c));
        } else if (value instanceof Boolean b) {
            sb.append(b);
        } else if (isNumber(value)) {
            writeNumber(sb, (Number) value);
        } else if (value instanceof JsonSerializable serializable) {
            write(sb, serializable.jsonSerialize());
        } else if (value instanceof Map<?, ?> map) {
            writeObject(sb, map);
        } else if (value instanceof Collection<?> collection) {
            writeArray(sb, collection);
        } else if (value.getClass().isArray()) {
            writeArray(sb, value);
        } else if (value.getClass().isRecord()) {
            writeRecord(sb, value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    private static boolean isNumber(Object value) {
        return value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double;
    }

    private static void writeJsonType(StringBuilder sb, JsonType jsonType) {
        switch (jsonType) {
            case JsonObject obj -> writeObject(sb, obj.map());
            case JsonArray array -> {
                sb.append('[');
                for (int i = 0; i < array.list().size(); i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    write(sb, array.list().get(i));
                }
                sb.append(']');
            }
            case JsonString string -> writeString(sb, string.string());
            case JsonNumber num -> sb.append(num.value());
            case JsonBool bool -> sb.append(bool.bool());
            case JsonNull ignored -> sb.append("null");
        }
    }

    private static void writeObject(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeKey(sb, entry.getKey());
            sb.append(':');
            write(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeKey(StringBuilder sb, Object key) {
        switch (key) {
            case null -> writeString(sb, "null");
            case JsonString jsonString -> writeString(sb, jsonString.string());
            case Character c -> writeString(sb, String.valueOf(c));
            case String s -> writeString(sb, s);
            default -> throw new IllegalArgumentException("Unsupported map key type: " + key.getClass().getName());
        }
    }

    private static void writeArray(StringBuilder sb, Object array) {
        sb.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            write(sb, Array.get(array, i));
        }
        sb.append(']');
    }

    private static void writeArray(StringBuilder sb, Collection<?> collection) {
        sb.append('[');
        boolean first = true;
        for (Object element : collection) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            write(sb, element);
        }
        sb.append(']');
    }

    private static void writeRecord(StringBuilder sb, Object record) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        sb.append('{');
        boolean first = true;
        for (RecordComponent component : components) {
            Object value = accessRecordValue(record, component);
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeString(sb, fieldName(component));
            sb.append(':');
            write(sb, value);
        }
        if (record instanceof JsonExtraFields extraFields) {
            Map<?, ?> extras = extraFields.extraFields();
            if (extras != null) {
                for (Map.Entry<?, ?> entry : extras.entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    writeKey(sb, entry.getKey());
                    sb.append(':');
                    write(sb, entry.getValue());
                }
            }
        }
        sb.append('}');
    }

    private static Object accessRecordValue(Object record, RecordComponent component) {
        try {
            java.lang.reflect.Method accessor = component.getAccessor();
            if (!accessor.canAccess(record)) {
                accessor.trySetAccessible();
            }
            return accessor.invoke(record);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to access record component '" +
                    component.getName() + "'", e);
        }
    }

    private static String fieldName(RecordComponent component) {
        FieldName override = component.getAnnotation(FieldName.class);
        return override != null ? override.value() : component.getName();
    }

    private static void writeNumber(StringBuilder sb, Number value) {
        if ((value instanceof Double || value instanceof Float)
                && (Double.isNaN(value.doubleValue()) || Double.isInfinite(value.doubleValue()))) {
            throw new IllegalArgumentException("Cannot serialize non-finite number: " + value);
        }
        sb.append(value);
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}