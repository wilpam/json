package wilpam.json.serialize;

import java.util.Map;

/// Opts a record into emitting additional JSON object entries.
///
/// When a serializable record implements this interface, the entries of the
/// map returned by [JsonExtraFields#extraFields] are merged into the object alongside the
/// record's own components.
public interface JsonExtraFields {
    /// @return the extra key/value entries to merge into the serialized object. may be null, which is treated as an empty map
    Map<String, Object> extraFields();
}