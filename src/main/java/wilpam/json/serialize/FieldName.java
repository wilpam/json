package wilpam.json.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Overrides the JSON key produced for a record component during serialization.
///
/// By default, a record component is serialized with the key having the same name as the field.
/// Annotating it `@FieldName("first_name")` would make the key `first_name` instead.
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldName {
    /// The JSON key to use instead of the component name.
    String value();
}