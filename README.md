##Test  task

This project is a system consisting of 2 microservices. First microservice is a `Client` which accepts words through API and publishes them to message queue(Kafka). Second microservice(`Processor`) consumes messages from messaging queue and joins all words that are received in one minute to a sentence, then posts back this sentence to message queue. Client in its turn receives these sentences from the message queue and stores them in NoSQL Database(Cassandra). Also Client exposes service to perform a search.

`TestTaskClient` - folder with `Client` application.<br/>
`TestTaskProcessor` - folder with `Processor` application.

First, 2 Kafka topics should be created(each should have only ONE partition) - for words and sentences. Currently 
they are called `words_topic` and `sentences_topic` but may be modified(see appropriate settings in `application.properties` of both apps). Address of Kafka brokers and Cassandra settings(`Client` app) are configurable as well.

To run both `Client/Processors`apps, just move to their folders sequentially and execute in each one console command: 
```
mvn spring-boot:run
```
After starting of both apps, `Client` endpoints are ready for use - they are described in file 
`resources/api-doc/swagger.yaml`, it
 may be opened in any suitable editor(e.g., http://editor.swagger.io). Comments in `Client`'s `ClientController.java`
  also contains necessary info.

Logging is organised with SLF4J, currently file and console outputs are used(see `logback.xml`).

Logic for sentences full-text search endpoint is based on SASI(https://docs.datastax.com/en/dse/5.1/cql/cql/cql_using/useSASIIndexConcept.html) but as it's warned that these indexes are experimental, makes sense to continue research - Elassandra (https://github.com/strapdata/elassandra) could be checked as alternative. 

Application was tested on Kafka 2.0.0 and Cassandra 3.11.6.

If imagine what else could be done for further development of the project, health checks for both apps is a good 
point to consider. Apache Camel health checks mechanism (https://camel.apache.org/manual/latest/health-check.html), for 
example, could be taken into account.
