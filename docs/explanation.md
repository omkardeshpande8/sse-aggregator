### Assumptions
1. The 1-second window interval is for the processing time, not event time.
2. The windowing strategy is fixed/tumbling windowing.  
3. Although not explicitly listed, using akka streams is also not allowed, since it is also a stream processing framework.

### Design
There are 3 main components
1. Event buffer - blocking queue, helps prevent out of memory error
2. Event source - Receives the events and adds to the event buffer
3. Event aggregator - Runs at the scheduled interval(1-second). Polls events from the buffer and aggregates them.

### Drawbacks
1. If the processing takes more than 1-second, then the subsequent aggregations will be delayed.
2. If shutdown hook cannot be invoked, the data in the buffer is lost.

### Backpressure
HTTP/TCP protocol supports backpressure, so instead of letting the size of the buffer grow, we can apply backpressure to the source.
There are two ways to apply backpressure -
1. Limit the size of the buffer - If we get more data than the limit(default for blocking queue is Integer.MAX_VALUE),
   then we won't read any more events till the buffer is freed up by the consumer.
2. Don't read records while calculating aggregates - This will also limit the number of records read into the buffer. 
   If we get a sudden influx of data, then time to calculate aggregates will increase. And for the next second we might have even more records to process.
   This might have a snowball effect. This is turned off by default, because it requires locking on the buffer.