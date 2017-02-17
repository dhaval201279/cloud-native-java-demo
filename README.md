# Reservation Service

**A faceless application which allows user to manage Reservatioins**

This is a POC application which does not have a UI, with a purpose of demonstrating [Microservice Architecture Pattern](http://martinfowler.com/microservices/) using [Spring Boot](https://projects.spring.io/spring-boot/) and leveraging [Cloud Native Architecture](https://pivotal.io/cloud-native) via [Spring Cloud](http://projects.spring.io/spring-cloud/).

## Functional service(s)

Since this application is a POC, so from a functional standpoint it just has a single service called Reservation Service
<p align=center>
<img alt="Functional services" src="https://cloud.githubusercontent.com/assets/3782824/22767852/22d5ecae-eea4-11e6-8026-818383af8e1e.png">
</p>

#### Account service
Comprises of business flow which allows user to add and edit reservations

Method	| Path	| Description	| User authenticated	| Available from UI
------------- | ------------------------- | ------------- |:-------------:|:----------------:|
GET	| /reservations/names	| Gets entire list of reservations done by user	| × | × 	
POST	| /reservations/	| Creates reservations for a given user	|   | ×

## Infrastructure services
There are industry standard [cloud patterns] (http://cloudpatterns.org/) which can help us to ease out infrastructure and operational concerns. 
[Spring cloud](http://projects.spring.io/spring-cloud/) provides tools for developers to quickly build some of the common patterns in distributed systems (e.g. configuration management, service discovery, circuit breakers, intelligent routing etc.)

I will cover some of them as we proceed further.
<p align=center>
<img alt="Infrastructure services" src="https://cloud.githubusercontent.com/assets/3782824/22853336/d1099d62-f079-11e6-885f-c7835f0a5f89.png">
</p>

### Config service
[Spring Cloud Config](http://cloud.spring.io/spring-cloud-config/spring-cloud-config.html) is horizontally scalable centralized configuration service for distributed systems. It uses a pluggable repository layer that currently supports Git and local storage.

This POC simply loads config files from the local classpath. These files are available at `config-files` directory in [Config service resources](https://github.com/dhaval201279/cloud-native-java-demo/tree/master/config-service/src/main/resources). By following the convention whenever reservation-service requests it's configuration, this Config service responds with `config-files/reservation-service.properties` and `config-files/application.properties`.

##### Client side usage
For using above configurations just build Spring Boot application that depends on `spring-cloud-config-client`. The most easiest and straight forward way to add this dependency via `spring-cloud-starter-config` POM.

With this you don't need any properties to be managed via your application. Just provide `bootstrap.properties` with Config service url and application name :
```bootstrap.properties
spring.cloud.config.uri = http://localhost:8888
spring.application.name = reservation-service
```

##### With Spring Cloud Config, how to change app configuration dynamically? 
In our application, `MessageRestController` within [reservation-service](https://github.com/dhaval201279/cloud-native-java-demo/tree/master/reservation-service) is annotated with `@RefreshScope`; which means, it can not only get updated value of `message` but also rest of the values from `reservation-service.properties` without rebuild and application (i.e. reservation-service) restart.

First, change required properties in Config server. Then, perform refresh request to reservation-service:
`curl -d{} http://localhost:8000/refresh` and there after try accessing `http://localhost:8000/message`

#### Notes
This can be primarily be used for :
- Feature flags and toggle for disabling a given functionality
- Dynamic reconfiguration which allows us to do [A/B Testing] (https://en.wikipedia.org/wiki/A/B_testing)
- [Branch by abstraction] (https://martinfowler.com/bliki/BranchByAbstraction.html)

### Service discovery

One of the key tenats of Microservice Architecture pattern is Service discovery. With Microservice Architecture one would generally have myriad set of services and keeping track of each of them in distributed topology would be too cumbersome and time consuming. Hence we need a service registry which can keep track of all the services - not only the ones which are newly created but also ones which have been deleted. It allows automatic detection of service instances.

Most significant part of Service discovery is Registry. We will be using Netflix Eureka in this application. With Spring Boot, you can easily build Eureka Registry with `spring-cloud-starter-eureka-server` dependency, `@EnableEurekaServer` annotation and simple configuration properties.
``` bootstrap.properties
spring.cloud.config.uri = http://localhost:8888
spring.application.name = eureka-service
```
Client support can be enabled with `@EnableDiscoveryClient` annotation an `bootstrap.properties` with application name:
``` `bootstrap.properties`
spring.cloud.config.uri = http://localhost:8888
spring.application.name = reservation-service
```

Now, on application startup, Eureka Server will register itself. It also provide meta-data such as host and port, health indicator URL etc. Eureka receives heartbeat messages from each instance belonging to a service. If the heartbeat fails over a configurable timetable, the instance will be removed from the registry.

#### Important endpoints
Endpoint	| Description	| 
:-------------:|:-------------------------:|
http://localhost:8761	| simple interface, where you can track running services and number of available instances |
http://localhost:8761/metrics | Provides detailed metric report |
