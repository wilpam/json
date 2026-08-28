package wilpam.json.obj;

import java.util.List;

/// A JSON array.
///
/// @param list the elements of the array, in order
public record JsonArray(List<JsonType> list) implements JsonType {
}