# blocklist-api
Exposes an API service to check if an IP is in a blocklist. This could be used to prevent abuse in different applications to ban IPs that are known to be used for malicious purposes.

Uses [IPSum](https://github.com/stamparm/ipsum) as data source.

## How to use
* Start the application, perform a GET request to the `/v1/ips/{IPv4 address}` route to check if an IP is in the blocklist. 
```
curl http://localhost:8080/v1/ips/{IPv4 address}
```
* It'll return `true` if it's in the blocklist or `false` if it's not.

## How to start the application
* Clone this repository.
* Ensure you have JDK 17 (or greater) installed, you can check executing `$ java -version`. 
* If you don't have it, there're several implementations you can get: e.g. [OpenJDK](https://jdk.java.net/archive/), [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html), [AdoptOpenJDK](https://adoptium.net/temurin/releases/).
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

To customize the port of this API service: `server.port=8080`

To enable request/response logs to this API service: `logging.level.org.springframework.web=DEBUG`

To enable external API client logs: `logging.level.org.springframework.web.client.RestTemplate=DEBUG`

To enable cache logs:
`logging.level.org.springframework.cache=TRACE`

To refresh the blocklist from IPSum using a cron schedule (e.g. refresh everyday at 10:15:00): `blocklist.refresher.cron=0 15 10 * * *`

To configure external API client timeouts and retry attempts:
```
blocklist.rest.client.connectTimeout=5000
blocklist.rest.client.readTimeout=5000
blocklist.rest.client.retry.maxAttempts=10
blocklist.rest.client.retry.backoff.delay=3000
blocklist.rest.client.retry.backoff.multiplier=2.0
```

## Design choices
* SpringBoot + Kotlin were chosen to build this service.

* SpringBoot is a library/framework that already has out of the box solutions for common problems.
  * Allows to write web controllers exposing API services. E.g. To expose the `/v1/ips/{IPv4 address}` url.
  * Includes RestTemplate to perform remote requests. E.g. Client to retrieve the blocklist data from IPSum.
  * Allows to schedule tasks. E.g. Retrieve the blocklist every 24 hs.
  * Cache abstraction to easily manipulate cache. E.g. Cache the blocklist.

* The following SpringBoot compatible dependencies were also added:
  * spring-boot-starter-validation dependency To validate that GET requests to `/v1/ips/{IPv4 address}` contain a valid IPv4 address.
  * spring-retry dependency To retry remote API requests (to IPSum) in case the network or the remote resource is down.

* When the node starts, it'll retrieve the blocklist from IPSum and put it into cache.

* Whenever the node receives a GET request to `/v1/ips/{IPv4 address}` it will get the blocklist from cache and check if the address is in the blocklist. If it finds it, it'll return `true`, otherwise it'll return `false`.

Note: If the node was just started and immediately receives a GET request to `/v1/ips/{IPv4 address}` but the blocklist hasn't been cached yet, it'll respond with a 503 Service Unavailable status code as it cannot assume if the IP is or isn't in the blocklist. The client should retry later.

* The node schedules a task to retrieve the blocklist from IPSum and put it into cache, by default, at 00:00:00 everyday.
If the node receives a request while this process (of retrieving the blocklist and updating the cache) is running, it'll respond using the previous cache, resulting in no downtime.

* The blocklist is just cached (in-memory), it's not persisted to a file nor a DB.
Currently, the blocklist is less than 4 MB, therefore, even when starting a node for the first time, it'll retrieve the blocklist from IPSum and cache it pretty fast.
The blocklist retrieved from IPSum is converted to a HashSet because finding an element is O(1).
