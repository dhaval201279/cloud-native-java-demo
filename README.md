# Reservation Service

**An application which allows user to manage Reservatioins**

This is a POC application which does not have a UI, with a purpose of demonstrating [Microservice Architecture Pattern](http://martinfowler.com/microservices/) using [Spring Boot](https://projects.spring.io/spring-boot/) and leveraging [Cloud Native Architecture](https://pivotal.io/cloud-native) via [Spring Cloud](http://projects.spring.io/spring-cloud/).

## Functional service(s)

Since this application is a POC, so from a functional standpoint it just has a single service called Reservation Service which allows to create and view reservations for user.

<p align=center>
<img alt="Functional services" src="https://cloud.githubusercontent.com/assets/3782824/22767852/22d5ecae-eea4-11e6-8026-818383af8e1e.png">
</p>

#### Reservation service
Comprises of business flow which allows user to add and edit reservations

Method	| Path	| Description	| User authenticated	| Available from UI
------------- | ------------------------- | ------------- |:-------------:|:----------------:|
GET	| /reservations/names	| Gets entire list of reservations done by user	| × | × 	
POST	| /reservations/	| Creates reservations for a given user	|   | ×

## Infrastructure Services
There are industry standard [cloud patterns](http://cloudpatterns.org/) which can help us to ease out infrastructure and operational concerns. 
[Spring cloud](http://projects.spring.io/spring-cloud/) provides tools for developers to quickly build some of the common patterns in distributed systems (e.g. configuration management, service discovery, circuit breakers, intelligent routing etc.)

I will cover some of them as we proceed further.
<p align=center>
<img alt="Infrastructure services" src="https://cloud.githubusercontent.com/assets/3782824/23105418/f3e61252-f704-11e6-90ee-4f1db157d7f1.png">
</p>

### Config service
[Spring Cloud Config](http://cloud.spring.io/spring-cloud-config/spring-cloud-config.html) is horizontally scalable centralized configuration service for distributed systems. It uses a pluggable repository layer that currently supports Git and local storage.

This POC simply loads config files from the local classpath. These files are available at `config-files` directory in [Config service resources](https://github.com/dhaval201279/cloud-native-java-demo/tree/master/config-service/src/main/resources). By following the rule of convention whenever reservation-service requests it's configuration, this Config service responds with `config-files/reservation-service.properties` and `config-files/application.properties`.

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

##### Notes
[Spring Cloud Config](http://cloud.spring.io/spring-cloud-config/spring-cloud-config.html) can be primarily be used for :
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
``` bootstrap.properties
spring.cloud.config.uri = http://localhost:8888
spring.application.name = reservation-service
```

Now, on application startup, Eureka Server will register itself. It also provide meta-data such as host and port, health indicator URL etc. Eureka receives heartbeat messages from each instance belonging to a service. If the heartbeat fails over a configurable timetable, the instance will be removed from the registry.

#### Important endpoints
Endpoint	| Description	| 
-------------|-------------------------|
http://localhost:8761	| simple interface, where you can track running services and number of available instances |
http://localhost:8761/metrics | Provides detailed metric report |

### API Gateway
For an enterprise application you would want to keep your core domain completely decoupled from its actors (i.e. end user or a service). This is in a way aligning with [Hexagonal Architecture](http://alistair.cockburn.us/Hexagonal+architecture) where actors which are liable to induce changes within system are not directly dependent on core business domain.

In principle, there will be myriad clients viz. Android app, iOS app, HTML5, IOT device etc who can make requests to each of the microservices directly. But obviously, following [Hexagonal Architecture](http://alistair.cockburn.us/Hexagonal+architecture) we would certainly not like to expose our core domain i.e. reservation-service directly to these clients. Also each of the clients will have specialized concerns and requirements considering their UI/UX capabilities. So rather than retrofitting core business service for each of the clients it is advisable to set up an [Edge service] (http://techblog.netflix.com/2013/06/announcing-zuul-edge-service-in-cloud.html), which is client specific. Key advantage to this approach is - it will act as a single entry point into the system, which will also be used to handle requests for a specific client by routing them to the appropriate backend service or by invoking multiple backend services and [aggregating the results](http://techblog.netflix.com/2013/01/optimizing-netflix-api.html)

Hence we can set up a micro proxy by enabling it with one `@EnableZuulProxy` annotation. In this project, we use Zuul to route requests to appropriate microservices. To augment or change the proxy routes, you can add external configuration within `application.yml` like the following:

```application.yml
zuul:
  routes:
    reservation-client:
        path: /reservations/**
        serviceId: reservation-service
        stripPrefix: false

```

That means all requests starting with `/reservations` will be routed to Reservation service.

How does an edge service know, which instance of downstream service to invoke - It does it via client side load balancing using Ribbon whose use within the context of application is explained below.

### Ribbon
Ribbon is a client side load balancer which not only gives you a lot of control over the behaviour of HTTP and TCP clients but also implements various load balancing strategies. It in a way has java based implementation which is responsible for doing the lookup via Ribbon Client configurations

Out of the box, it integrates with Spring Cloud and Service Discovery. To include Ribbon in your project use the starter with group `org.springframework.cloud` and artifact id `spring-cloud-starter-ribbon`

One can still do a declarative way of doing load balancing explicitly by injecting [`RestTemplate`](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) along with [`@LoadBalanced`] (http://cloud.spring.io/spring-cloud-static/spring-cloud.html#_spring_resttemplate_as_a_load_balancer_client) annotation. Since this looks like a boiler plate code, it can be avoided by using [Feign](https://github.com/OpenFeign/feign) whose use within the context of application is explained below.

### Feign
Feign is a declarative web service / Http client, which seamlessly integrates with Ribbon, Eureka and Hystrix to facilitate resilient load balanced client. So with just `spring-cloud-starter-feign` dependency and `@EnableFeignClients` annotation you have a complete set of Load balancer, Circuit breaker and Http client with sensible ready-to-go default configurations.

Here is an example from Reservation Client:

``` java
@FeignClient("reservation-service")
interface ReservationReader {
	@RequestMapping(method = RequestMethod.GET, value="/reservations")
	Resources<Reservation> read();
}
```
Each feign client is part of an ensemble of components `ApplicationContext` for each named client using `FeignClientsConfiguration` which contains (amongst other things) a feign.Decoder, a feign.Encoder, and a feign.Contract.

### Hystrix
Hystrix is an implementation of [Circuit Breaker pattern](http://martinfowler.com/bliki/CircuitBreaker.html), which allows to have  control over latency and failure of dependencies accessed over the network. In high performing enterprise application, failures are inevitable and hence there has to be mechanisms for gracefully handling them. The main idea is to gracefully handle cascading effect of failures in a distributed environment; With Microservice Architecture this becomes extremely imperative as it helps to fail fast with gracefull degradation - which is a fundamental characteristic of any fault-tolerant systems i.e. They self-heal!

With Hystrix you can add a fallback method that will be executed in case the main command fails.

Moreover, Hystrix generates metrics via `/health` and this can be used to monitor health of each circuit breaker efficiently.
To include the Hystrix Dashboard in our project we have used the starter with group `org.springframework.cloud` and artifact id `spring-cloud-starter-hystrix-dashboard`. For running the Hystrix Dashboard annotate your Spring Boot main class with `@EnableHystrixDashboard`. You then visit `/hystrix` and point the dashboard to an individual instances `/hystrix.stream` endpoint in a Hystrix client application. It initiates heart beat stream which comes off from Edge Service

<p align=center>
<img alt="Hystrix Dashboard" src="https://cloud.githubusercontent.com/assets/3782824/23101227/b1227faa-f6b4-11e6-970b-0532f11a415d.png">
</p>

Can we achieve similar kind of behavior for insert / update operations? Answer is YES, and hence POC application makes use of [RabbitMQ](https://www.rabbitmq.com/) as message broker. Underlying prcinciple is to model our system such that transactions are inter-leavable, such that each of those transaction has compensatory transactions, so that it can be replayed as many times as necessary. This in a way ensures that system remains in semantically consistent state - which is also know as [eventual consistency](https://en.wikipedia.org/wiki/Eventual_consistency).

This can be implemented using [Spring Integration](https://projects.spring.io/spring-integration/) as it uses message channels to connect with different systems via [Enterprise Integration Patterns](http://www.enterpriseintegrationpatterns.com/). So in our application we will use [Spring Cloud stream](https://cloud.spring.io/spring-cloud-stream/) which provides a framework for building message driven microservice applications. Implicitly it uses Spring Integration for providing connectivity to underlying message brokers

### Zipkin
Considering the fact that there will be myriad set of microservices, distributed tracing becomes an inevitable characteristic of the infrastructure. Distributed tracing in a way will assist us in having better systemic view and observability of system in whole. So Zipkin will ensure that request is traced from one service to another till the response is sent back. We will be using [Spring Cloud Sleuth](http://cloud.spring.io/spring-cloud-static/Camden.SR5/#_spring_cloud_sleuth) for enabling distributed tracing within our solution.

<p align=center>
<img alt="Zipkin Tracing" src="https://cloud.githubusercontent.com/assets/3782824/23101228/b8b951bc-f6b4-11e6-912d-cb67d70ed38c.png">
</p>

### Authentication & Authorization
In order to provide Authentication and Authorization, we will be leveraging [OAuth2](https://oauth.net/2/). Spring provides Spring cloud OAuth module via `spring-cloud-starter-oauth2` to achieve the same with some pre configured user along with their credentials. As an underlying framework it leverages [Spring Security](https://projects.spring.io/spring-security/).

##### How to use this service for accessing Reservation services
- Send a POST to http://localhost:9191/uaa/oauth/token to fetch access token. Response would look like
JSON - Auth Response
	{
	  "access_token": "f1175786-e525-40d3-8389-fffa133c8d84",
	  "token_type": "bearer",
	  "expires_in": 43199,
	  "scope": "openid"
	
- Use above access token whilst invoking read / update Reservation Service i.e.

- curl -H"authorization: bearer fd3444b6-0e2a-4d54-ab9b-778146b599d5" http://localhost:9999/reservations/names
- curl -X POST -H "authorization: bearer fd3444b6-0e2a-4d54-ab9b-778146b599d5" -H "Content-Type: application/json" -d "{\"reservationName\" : \"Dr. Jigar Patel\"}" http://localhost:9999/reservations

##### Note
We can remove authentication and authorization module by removing Spring Oauth dependency from [Reservation client pom](https://github.com/dhaval201279/cloud-native-java-demo/blob/master/reservation-client/pom.xml)

### Microservices Dashboard
Since with Microservices Architecture, an application will have myriad set of services (i.e. > 100). Hence it is quiet important to have bird's eye view of the entire architecture. Thanks to [JWorks](https://github.com/ordina-jworks) for providing dashboard application which can provide visual representation of microservices architecture and its ecosystem. Systemic view of microservice can be be mainly categorized into -
- UI
- Resources
- Microservices
- Backends

It mainly collates information from Spring Boot Actuator health endpoints.

Primarily it comprises of 3 components -
- Spring Boot Admin Server
- Spring Boot Admin UI Server
- Microservices Dashboard Server

Having said that, for our POC application we will be implementing Admin server and Admin UI Server as a single microservice application i.e. [Spring Boot Admin](https://github.com/dhaval201279/cloud-native-java-demo/tree/master/spring-boot-admin) and a separate application for [Microservices Dashboard](https://github.com/dhaval201279/cloud-native-java-demo/tree/master/microservices-dashboard). A sample screenshot of Microservice Dasboard for our POC application is as shown below :

<p align=center>
<img alt="Microservice Dashboard" src="https://cloud.githubusercontent.com/assets/3782824/23105287/f7f122d0-f702-11e6-9f13-9f3eb584c8a2.png">
</p>

### Open for feedback
Feel free to contact me in case of queries or clarifications if any.
