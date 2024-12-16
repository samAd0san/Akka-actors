## Important Terms / Functionalities used in application
- **Eviction:** It's like clearing out old or unused items from a crowded room to make space for new ones. In caches, eviction removes older or less important data when the cache is full, based on rules like "remove the least recently used" or "remove the oldest."
- **Supervision Strategy:** Think of it as a manager watching over workers. It defines how to handle failures in a system, like restarting a failed part, ignoring errors, or stopping everything to prevent bigger problems. It ensures the system keeps running smoothly despite issues.
- **Rehydrate:** Imagine refilling a dried-out sponge with water. In a cache, rehydration means loading data back into the cache (e.g., after a restart or error) to make it ready for use again.

## Connected methods in brm
### cacheConfigStream (contains) - PART I
   // kafkaSource creates a Kafka consumer source that reads messages from the configured topics using the settings defined in ConfigLoader.<br>
   1. val **kafkaSource** = b.add(SourceUtil.kafkaConsumerPlainSource(generateId,
          ConfigLoader.kafkaTopicConfigManager,
          ConfigLoader.bootStrapServers,
          ConfigLoader.kafkaProps)).out
          
   // converts JSON messages from Kafka into CacheManagerOp objects<br>
   2. val **MessageUnMarshallFlow** = b.add(FlowUtil.unMarshallFlowWithCallerName[CacheManagerOp]())
   
   3. **validationFlow** (filters cache op (e.g add, remove) by validating their configuration)
   
   4. **processFlow** (according to the 'op' it add, remove) == ProcessSink
     - **addLookup** with (lookupIndexMap) -> **addCache** (ADD)
     - **removeLookup** with (lookupIndexMap) -> **removeCache** (REMOVE)

   // stream<br>
   **GraphDSL:** **kafkaSource ~> MessageUnMarshallFlow ~> ValidationFlow ~> ProcessSink**

### cacheDataStream - PART II
   1. val **kafkaSource** = b.add(SourceUtil.kafkaConsumerPlainSource(generateId,
          ConfigLoader.kafkaTopicConfigManager,
          ConfigLoader.bootStrapServers,
          ConfigLoader.kafkaProps)).out

   2. FilterFlow // filters incoming Kafka records, allowing only those with a defined key (e.g IP) and a valid corresponding cache configuration, and applies a supervision strategy.

   3. qSink = queueSink // the Kafka records are added to a queue based on the cache configuration key (e.g., IP) retrieved from the Kafka record's key.

   **GraphDSL:** **kafkaSrouce ~> filterFlow ~> qSink**

### initialize() - PART III
  1. rehydrate (from utilitiy function)
  2. cacheConfigStream.run()
  3. cacheDataStream.run()

### ProcessInputAttributes and lookupOnCache - PART IV

## Working of cacheKey method in GenericCacheManager.scala
```scala
// GenericCacheManager.scala
def cacheKey(payload: Map[String, Any], cacheConfig: CacheConfig): String = {
    cacheConfig.key.headOption match {
      case Some(k) => payload.getOrElse(k, throw MangoPlainException(s"cacheId: ${cacheConfig.id} key: ${cacheConfig.key} not found in payload: ${payload}")).toString
      case None => throw MangoPlainException(s"cacheId: ${cacheConfig.id} key: ${cacheConfig.key} not found in payload: $payload")
    }
}
```
- In this function, cacheKey retrieves a value from the payload map based on the first key defined in the cacheConfig.key list.
- Example
```scala
val cacheconfig = {
    CacheConfig(id = "CacheId1", name = "CompromisedIp",
      cache_type = "MANGO", kafka_topic = Set("mango_plain_test"),
      eviction_policy = Some(CacheEvictionPolicy("time", Some(1231), None)),
      lookup_attributes = Set("IP", "IpAddress"),
      output_attributes = List(), category = "IP",
      persist = true, snapshot = false,
      key = List("IP"), active = true,
      case_sensitive = false)
}
// Here key = List("IP") is taken as the cacheKey
// cacheConfig.key = List("IP") (this is the key to look for).
// payload = Map("IP" -> "192.168.0.1") (this contains the actual data).
```
### How it works:
1. ```cacheConfig.key.headOption``` retrieves ```"IP"``` (the first key in the list).
2. ```payload.getOrElse("IP", ...)``` tries to find ```"IP"``` in the payload map.
   - If found: it returns ```192.168.0.1``` as a string.
   - If not found: it throws a ```MangoPlainException``` with a descriptive error.
Result: For the given ```payload``` and ```cacheConfig```, the output will be ```"192.168.0.1".```.

### Test case for cacheKey
```scala
val cacheconfig = {
    CacheConfig(id = "CacheId1", name = "CompromisedIp",
      cache_type = "MANGO", kafka_topic = Set("mango_plain_test"),
      eviction_policy = Some(CacheEvictionPolicy("time", Some(1231), None)),
      lookup_attributes = Set("IP", "IpAddress"),
      output_attributes = List(), category = "IP",
      persist = true, snapshot = false,
      key = List("IP"), active = true,
      case_sensitive = false)
}
/** Test case for cacheKey */
"cacheKey" should "retrieve the correct key from the payload" in {
 val payload = Map("IP" -> "192.168.0.1")
 val result = GenericCacheManager.cacheKey(payload, cacheconfig)

 // Assert the result
 result shouldBe "192.168.0.1"
}
```

## Working of LookupOnCache
1. Create a cache
```json
{
    "name": "WHITELIST",
    "lookup_attributes": [
      "IP",
      "IpAddress"
    ],
    "eviction_policy": {
      "eviction_type": "time",
      "time": 10
    },
    "cache_type": "MANGO",
    "kafka_topic": [
      "cache_manager_config_updates"
    ],
    "key": [
      "IP"
    ],
    "persist": true,
    "id": "9beebb8a-3b07-4009-b0bc-1dd6da764e33",
    "snapshot": false,
    "category": "IPCategories",
    "case_sensitive": false,
    "output_attributes": [],
    "active": true
}
```

2. After adding the cache populate it with some data
```json
{
    "cache_id": "9beebb8a-3b07-4009-b0bc-1dd6da764e33",
    "operation": "add",
    "data": {
        "IP": "127.0.0.1"
    }
}
```
- After populating it with IP -> 127.0.0.1
```json
// It'll look like this
{
  "name": "WHITELIST",
  "lookup_attributes": [
    "IP",
    "IpAddress"
  ],
  "eviction_policy": {
    "eviction_type": "time",
    "time": 10
  },
  "cache_type": "MANGO",
  "kafka_topic": [
    "cache_manager_config_updates"
  ],
  "key": [
    "IP"
  ],
  "persist": true,
  "id": "9beebb8a-3b07-4009-b0bc-1dd6da764e33",
  "snapshot": false,
  "category": "IPCategories",
  "case_sensitive": false,
  "output_attributes": [],
  "active": true,
  "data": {
    "127.0.0.1": {
      "IP": "127.0.0.1"
    }
  }
}
// this particular 'data' field was added from the api (refer brm)
```
3. Now doing lookupOnCache
```json
{
    "lookupOn" : "IP", 
    "keyToSearch" : "127.0.0.1"
}
```
- **keyToSearch** matches the value in the **data** field (e.g., 127.0.0.1), while **lookupOn: IP** specifies the attribute (e.g., IP) in **lookup_attributes** to compare against.
- In simple words, **keyToSearch** is compared by the **key** of **data** i.e 127.0.0.1 and **lookupOn: IP** is compared by the **IP** of **lookup_attributes**.
- And if it matches we are returning 'cacheName', 'cacheCategory' and cacheAttr i.e outputAttrubutes.
```json
[
    {
        "cacheCategory": "IPCategories",
        "cacheName": "WHITELIST",
        "cacheAttr": {}
    }
]
```
- for now in the context of lookupOnCache, the field of "key": ["IP"] is not used, the **key** field is not directly involved; the lookup is based on the **lookup_attributes** and **keyToSearch.**
