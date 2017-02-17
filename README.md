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

