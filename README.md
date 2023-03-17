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
* In order to create this service as soon as possible/time constraints, [Spring Boot](https://spring.io/projects/spring-boot) + [Kotlin](https://kotlinlang.org/) were chosen to build this service, as these are known by the developer.

* Spring Boot is a library/framework that already has out of the box solutions for common problems:
  * Allows to write web controllers exposing API services. E.g. To expose the GET `/v1/ips/{IPv4 address}` route.
  * Includes `RestTemplate` to perform remote requests. E.g. Client to retrieve the blocklist data from IPSum.
  * Allows to schedule tasks. E.g. Retrieve the blocklist every 24 hs.
  * Cache abstraction to easily manipulate cache. E.g. Cache the blocklist.

* The following Spring Boot compatible dependencies were also added:
  * `spring-boot-starter-validation`. E.g. To validate that GET requests to `/v1/ips/{IPv4 address}` contain a valid IPv4 address.
  * `spring-retry`. E.g. To retry remote API requests (to IPSum) in case the network or the remote resource is down.

* When the node starts, it'll retrieve the blocklist from IPSum and put it into cache as a HashSet. HashSet was chosen because finding an element is O(1).

* The blocklist is just cached (in-memory), it's not persisted to a file nor a DB.
  * Currently, the blocklist is less than 4 MB, therefore, even when starting a node for the first time, it'll retrieve the blocklist from IPSum and cache it pretty fast.

* Whenever the node receives a GET request to `/v1/ips/{IPv4 address}` it will get the blocklist from cache and check if the address is in the blocklist. If it finds it, it'll return `true`, otherwise it'll return `false`.

Note: If the node was just started and immediately receives a GET request to `/v1/ips/{IPv4 address}` but the blocklist hasn't been cached yet, it'll respond with a `503 - Service Unavailable` status code as it cannot assume if the IP is or isn't in the blocklist. The client should retry later.

* Whenever the node receives a GET request to `/v1/ips/{IPv4 address}` with an invalid IPv4 address, it'll respond with a `400 - Bad Request` status code.

* The node schedules a task to retrieve the blocklist from IPSum and put it into cache, by default, at 00:00:00 everyday. To change it, see the [Configurable Properties](#configurable-properties) section.

Note: If the node receives a GET request to `/v1/ips/{IPv4 address}` while this process (of retrieving the blocklist and updating the cache) is running, it'll respond using the previous cache, resulting in no downtime. The cache is thread-safe, Spring Boot uses a `ConcurrentHashMap`.

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

## Possible Enhancements
* Currently, only IPSum, hosted in Github, is the blocklist provider. If Github is down, the client will retry, 5 times by default. To change it, see the [Configurable Properties](#configurable-properties) section.
  * `BlocklistService` could be modified to use a list of providers instead of just one (the `IPSumClient`), each of these providers could retrieve the data from other endpoints or even a local file.
  * When the node starts, it could get the blocklist from the local file provider first, then try to get the most updated blocklist from an external provider.
  * When the blocklist is retrieved from an external provider successfully, it should save it to local disk.

* If the service should start checking for IPv6 addresses in the blocklist, the following changes would have to be made:
  * Change/add another provider, currently IPSum just provides a blocklist of IPv4 addresses.
  * `IPv4Extractor` should be changed or replaced to use a IPv6 regex.

* If the blocklist is much greater than 4MB and the node cannot hold all of the IPs in memory, this service could be modified as follows:
  * Download the blocklist from an external provider and save it to a file.
  * Sort the IPs of the file and save it.
  * Save another file describing pages, e.g. IPs less than A correspond to the first N IPs of the file, IPs less than B correspond to the next M IPs of the file, and so on.
  * `BlocklistService` should be changed to get the corresponding page according to the IP that the client wants to check, e.g. if the IP is greater than A but less than B, page 1 of the blocklist should be load from disk to memory and check if the IP is in this page.
  * Page could be cached in memory just in case the next request uses the same page.
  * The result of the last R requests and their result (true/false if it's or it's not in the blocklist) could be cached: e.g. IP_X-true, IP_Y-false, etc.

* Having N running nodes means there will be N requests to the external IPSum blocklist provider periodically.
  * Currently it's just 4 MB per node, however if it gets bigger, downloading the bigger file multiplied by N might be redundant/unnecesary.
  * It'd be better if only one node downloads the blocklist periodically, cache it and share with the other nodes through the local network.

### Test Criteria

* Only the minimal tests were written. This means the code doesn't have 100% coverage.
  * Having a 100% coverage would require mantaining tests and some of them could be fragile if any of the possible enhancemente mentioned above is implemented.
  * Some of the methods really don't have much logic and use features of Spring, which are already tested.
  * The main focus is to test critical parts of the service such as:
    * IPv4 parsing.
    * Collaboration between the `BlocklistController` and `BlocklistService` to know if the IP is contained or not in the blocklist.
    * Ensuring the `BlocklistRefresher` collaborates with the `BlocklistService` whenever it needs to refresh the blocklist.
    * Putting the blocklist in cache and retrieving it later and verifying the expected behavior when there's no cache.