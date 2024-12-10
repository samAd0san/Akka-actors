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
