package wilpam.json.obj;

/// A JSON number.
///
/// The raw representation is preserved as a [String] in [value].
///
/// Use the `as*` methods to convert to primitive types.
///
/// @param value the number as a string, e.g. `"-3.5e2"`
@SuppressWarnings("unused")
public record JsonNumber(String value) implements JsonType {
    /// @return this number parsed as a `long`
    public long asLong() {
        return Long.parseLong(value);
    }

    /// @return this number parsed as an `int`
    public int asInt() {
        return Integer.parseInt(value);
    }

    /// @return this number parsed as a `double`
    public double asDouble() {
        return Double.parseDouble(value);
    }

    /// @return this number parsed as a `float`
    public float asFloat() {
        return Float.parseFloat(value);
    }
}