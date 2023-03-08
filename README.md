# blocklist-api
Exposes an API service to check if an IP is in a blocklist. This could be used to prevent abuse in different applications to ban IPs that are known to be used for malicious purposes.

Uses [IPSum](https://github.com/stamparm/ipsum) as data source.

## How to use
* [Start the application](#how-to-start-the-application) and perform a GET request to the `/v1/ips/{IPv4 address}` route to check if an IP is in the blocklist. 
```
curl http://localhost:8080/v1/ips/{IPv4 address}
```
* It'll return `true` if it's in the blocklist or `false` if it's not.

## How to start the application
* Clone this repository.
* Ensure you have JDK 17 (or greater) installed, you can check executing:
```
java -version
``` 
* If you don't have it installed, there're several implementations you can get: e.g. [OpenJDK](https://jdk.java.net/archive/), [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html), [AdoptOpenJDK](https://adoptium.net/temurin/releases/).
* Compile & run the application executing:
```
./gradlew bootRun
```

or generate the jar and execute it:

```
./gradlew bootJar
java -jar build/libs/blocklist-0.0.1-SNAPSHOT.jar
```

## Node Types

### Standalone
* This is the default type.
* This node will retrieve the blocklist from IPSum, schedule a refresh task periodically and cache it.
* It isn't aware of other nodes running the same application.

### Cluster-Refresher
* This node will be part of a cluster of nodes running the same application.
* It'll retrieve the blocklist from IPSum, schedule a refresh task periodically and cache it. 
* It'll share the cache with the rest of the nodes in the cluster.

### Cluster-Member
* This node will be part of a cluster of nodes running the same application.
* It won't retrieve the blocklist from IPSum nor execute the refresh task periodically. 
* Once the node starts, it'll discover other nodes and get the shared cache.

## Configurable Properties
You can optionally set the following properties in `src/main/resources/application.properties`:

```
# Customize the port of this API service
server.port=8888

# Enable request/response logs to this API service and to external API (IPSum) service
logging.level.org.springframework.web=DEBUG

# Enable cache logs
logging.level.org.springframework.cache=TRACE

# Refresh the blocklist from IPSum using a cron schedule (e.g. refresh everyday at 10:15:00)
blocklist.refresher.cron=0 15 10 * * *

# Configure external API client (IPSum) timeouts and retry attempts
blocklist.rest.client.connectTimeout=5000
blocklist.rest.client.readTimeout=5000
blocklist.rest.client.retry.maxAttempts=10
blocklist.rest.client.retry.backoff.delay=3000
blocklist.rest.client.retry.backoff.multiplier=2.0

# Configure if this is a standalone, cluster-refresher or cluster-member 
blocklist.nodeType=standalone
```

You can also set these properties using the command line:

E.g.
```
./gradlew bootRun --args='--server.port=8888 --logging.level.org.springframework.web=DEBUG'
```
or if you're using the jar:
```
java -jar -Dserver.port=8888 -Dlogging.level.org.springframework.web=DEBUG build/libs/blocklist-0.0.1-SNAPSHOT.jar
```

## Design choices
* [Spring Boot](https://spring.io/projects/spring-boot) + [Kotlin](https://kotlinlang.org/) were chosen to build this service.

* Spring Boot is a library/framework that already has out of the box solutions for common problems:
  * Allows to write web controllers exposing API services. E.g. To expose the GET `/v1/ips/{IPv4 address}` route.
  * Includes `RestTemplate` to perform remote requests. E.g. Client to retrieve the blocklist data from IPSum.
  * Allows to schedule tasks. E.g. Retrieve the blocklist every 24 hs.
  * Cache abstraction to easily manipulate cache. E.g. Cache the blocklist.

* The following Spring Boot compatible dependencies were also added:
  * `spring-boot-starter-validation`. E.g. To validate that GET requests to `/v1/ips/{IPv4 address}` contain a valid IPv4 address.
  * `spring-retry`. E.g. To retry remote API requests (to IPSum) in case the network or the remote resource is down.
  * `hazelcast-all`. To share the cached blocklist between nodes. 

* When the Standalone/Cluster-Refresher node starts, it'll retrieve the blocklist from IPSum and put it into cache as a HashSet. HashSet was chosen because finding an element is O(1).

* The blocklist is just cached (in-memory), it's not persisted to a file nor a DB.
  * Currently, the blocklist is less than 4 MB, therefore, even when starting a node for the first time, it'll retrieve the blocklist from IPSum and cache it pretty fast.

* Whenever the node receives a GET request to `/v1/ips/{IPv4 address}` it will get the blocklist from cache and check if the address is in the blocklist. If it finds it, it'll return `true`, otherwise it'll return `false`.

Note: If the node was just started and immediately receives a GET request to `/v1/ips/{IPv4 address}` but the blocklist hasn't been cached yet, it'll respond with a `503 - Service Unavailable` status code as it cannot assume if the IP is or isn't in the blocklist. The client should retry later.

* Whenever the node receives a GET request to `/v1/ips/{IPv4 address}` with an invalid IPv4 address, it'll respond with a `400 - Bad Request` status code.

* The Standalone/Cluster-Refresher node schedules a task to retrieve the blocklist from IPSum and put it into cache, by default, at 00:00:00 everyday. To change it, see the [Configurable Properties](#configurable-properties) section.

Note: If the node receives a GET request to `/v1/ips/{IPv4 address}` while this process (of retrieving the blocklist and updating the cache) is running, it'll respond using the previous cache, resulting in no downtime. The cache is thread-safe, Spring Boot uses a `ConcurrentHashMap`.

* Having N Standalone nodes would mean there will be N requests to IPSum periodically, instead, it's possible to configure one node as Cluster-Refresher and the rest as Cluster-Member. Only the Cluster-Refresher will retrieve the blocklist from IPSum, cache it and share it to the rest of the nodes.
  * If the Cluster-Refresher is down, the rest of the Cluster-Member nodes will continue working with their blocklist cache, however, they won't get updates until a Cluster-Refresher is up again.

* To quickly see the Cluster-Refresher/Cluster-Member nodes in action you can run some nodes in a single computer like this and perform GET requests:
```
./gradlew bootRun --args='--server.port=8080 --blocklist.nodeType=cluster-refresher'
./gradlew bootRun --args='--server.port=8081 --blocklist.nodeType=cluster-member'
./gradlew bootRun --args='--server.port=8082 --blocklist.nodeType=cluster-member'
./gradlew bootRun --args='--server.port=8083 --blocklist.nodeType=cluster-member'

curl http://localhost:8080/v1/ips/{IPv4 address}
curl http://localhost:8081/v1/ips/{IPv4 address}
curl http://localhost:8082/v1/ips/{IPv4 address}
curl http://localhost:8083/v1/ips/{IPv4 address}
```

## Responsibilities
These are the main classes and a brief summary of what they do, you can also review the tests in the `src/test` folder.

### BlocklistApplication
Main entry point to start the application.

### IPSumClient
Performs the request to IPSum to retrieve the list of IPs in a multi-line `String`.

### IPv4Extractor
Parses a multi-line `String` to extract the list of IPv4 addresses and converts it into a `HashSet`.

### BlocklistService
Collaborates with `IPSumClient` and `IPv4Extractor` to get the blocklist and cache it.

### BlocklistRefresher
Schedules a task to refresh the blocklist using the `BlocklistService`. It also executes this task when the application starts.

### BlocklistController
Exposes the GET `/v1/ips/{IPv4 address}` route, returns `true`/`false` depending if it's or it's not in the blocklist obtained from the `BlocklistService`.