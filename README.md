# json-parser
非严格规则的json解析器

# example

```java
String json = "{\"key\": {\"key2\": [6, true, 99, 0.6]}}";
bytes = json.getBytes();
UTF8Reader reader = new UTF8Reader(bytes);
// GBKReader reader = new GBKReader(new String(bytes).getBytes("GBK"));
JSONParser parser = new JSONParser(reader);
JSONObject jsonObject = parser.parseObject();
System.out.println(jsonObject.get("key.key2[2]"));
```