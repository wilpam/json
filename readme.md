## wilpam-json
it will be the json library of the hour, probably
### serialization
```java
import wilpam.json.serialize.Serializer;

// ...

void main() {
    Serializer.serialize(1.0f); // or any other object supported by Serializer
}
```
### deserialization
```java
import wilpam.json.deserialize.Deserializer;
import wilpam.json.obj.*;

// ...

void main() {
    JsonType jt = Deserializer.serialize("\"I'm a String\"");
}
```
for more info check javadocs