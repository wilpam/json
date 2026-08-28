package wilpam.json.obj;

import java.util.Map;

/// A JSON object, holding an ordered mapping of string keys to values.
///
/// @param map the key/value pairs of the object; iteration order is preserved
///            as it was set
public record JsonObject(Map<JsonString, JsonType> map) implements JsonType {
}