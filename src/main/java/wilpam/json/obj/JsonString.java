package wilpam.json.obj;

/// A JSON string.
///
/// Wrapped so strings are distinguishable from other [JsonType] values.
///
/// @param string the string value
public record JsonString(String string) implements JsonType {
}