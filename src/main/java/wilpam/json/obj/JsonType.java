package wilpam.json.obj;

/// The common supertype for all JSON value types.
public sealed interface JsonType permits JsonArray, JsonBool, JsonNull, JsonNumber, JsonString, JsonObject {}