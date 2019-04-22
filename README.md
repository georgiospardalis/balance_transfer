# Backend Revolut Assignment

## Description
This is an assignment for the Revolut Backend Software Engineer Position. It provides a RESTful endpoint with the ability
to perform balance transfers from an account to another.

## How to test it
- For unit tests execute `mvn clean test`
- For integration tests `mvn clean integration-test`

## How to run it
Before being able to execute the jar, you must first create it. Run `mvn clean package` under the root folder of this
repository. This will create the ``jar`` file under the `target` folder.

Run `java -jar ./target/revolut-assignment-${project.version}.jar` without providing any extra arguments or environment
variables. This will start the embedded server, the database, and preload it with the following sample data
(both for "production" and testing purposes):

```sql
INSERT INTO account(balance, version) VALUES(1554.00, 1);  /*Account id is 1... */
INSERT INTO account(balance, version) VALUES(2000.50, 1);  /*Account id is 2... */
INSERT INTO account(balance, version) VALUES(3000.00, 1);  /* ... */
INSERT INTO account(balance, version) VALUES(10000.00, 1); /*Account id is 4 */
``` 

The endpoint should be under `http://localhost:28960/transaction/balance_transfer`. The request method is `PUT`,
the content-type `application/json` and the body requires the three following fields:
```json
{
  "source-account": "1",
  "target-account": "2",
  "amount": "2000.00"
}
```

Request example:
`curl -H 'Content-Type: application/json' -X PUT -d '[JSON]' http://localhost:28960/transaction/balance_transfer`
where you can replace `[JSON]` with the request body of your like, as described above.

Response example:
```json
  "transfer-status": "Successful Transaction", 
  "timestamp": 1555936642109,
  "transferId": "4356667a-bb78-4a42-8095-e47022e29f4a"
```
- `transfer-status` -> transaction message,
- `timestamp` -> timestamp from `System.currentTimeMillis()`,
- `transferId` -> a "stringified" random UUID for the successfully completed transaction.
 
In the case of an error (insufficient balance, validation errors for the sent request, etc) the server will respond with
a `200 status code`, and notify the user accordingly from within the `transfer-status` attribute, omitting the rest two.

## Technologies Used
Respecting the requirement to produce a result as a standalone executable, without having the user/tester/developer to
provide the dependencies (application server, database, etc) led to using the following technologies:

- Jetty Embedded
- H2 In Memory (Embedded) Database
- Jersey
- Guice
- Hibernate with HikariCP
- Logback (logging)
- JUnit (testing scope)
- Mockito (testing scope)
- Hamcrest Matchers (testing scope)

## Design Preferences
### Regarding the packaging
The notion would be to have a "Controller/Resource - Service - Repository" structure, to distinguish between layers.
In the process of development the Repository proved to be an "one-liner", hence it's absence.

### Transactionality
You can see that the application manages the `EntityManager`
(also seen through the `persistence.xml` - `RESOURCE-LOCAL` tag) and not the container. An `EntityManagerFactory` is created
once, and is injected to the singleton Service. `EntityManager` instances are thread-local. You can also characterise them
as "RequestScoped" for this example.

### Why Jetty
Because at the time, it was easy to embed it to the application. Moreover it provided a quick way to setup a pool of workers
to speed up any throughput.

### H2 Database
No need to dockerize the application or provide extra configuration alongside the source code.