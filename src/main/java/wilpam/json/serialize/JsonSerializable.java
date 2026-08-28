package wilpam.json.serialize;

import wilpam.json.obj.JsonType;

/// Allows an object to define its own JSON representation.
///
/// When an object implementing this interface is serialized, its [JsonSerializable#jsonSerialize]
/// result is used instead. This replaces the default serialization for records/lists/etc.
public interface JsonSerializable {
    /// @return the JSON value to emit for this object
    JsonType jsonSerialize();
}