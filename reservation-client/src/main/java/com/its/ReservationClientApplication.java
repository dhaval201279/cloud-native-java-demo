package com.its;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.hateoas.Resources;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@EnableResourceServer
@IntegrationComponentScan
@EnableBinding (ReservationChannels.class)
@EnableFeignClients
@EnableZuulProxy
@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

interface ReservationChannels {
	@Output
	MessageChannel output(); // one can have separate message channel for different entities viz. product, customer order
	
}
@FeignClient("reservation-service")
interface ReservationReader {
	@RequestMapping(method = RequestMethod.GET, value="/reservations")
	Resources<Reservation> read();
}

@MessagingGateway
interface ReservationWriter {
	@Gateway(requestChannel = "output")
	public void write(String name);
}

class Reservation {
	private String reservationName;

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
	
	
}
@RestController
@RequestMapping("/reservations")
class ReservationAPIGateway {
	
	/*private final RestTemplate restTemplate;
	
	@Autowired
	public ReservationGateway(@LoadBalanced RestTemplate restTemplate) {
		super();
		this.restTemplate = restTemplate;
	}*/
	
	private final ReservationReader reservationReader;
	private final ReservationWriter reservationWriter;
	
	@Autowired
	public ReservationAPIGateway(ReservationReader reservationReader, ReservationWriter reservationWriter) {
		super();
		this.reservationReader = reservationReader;
		this.reservationWriter = reservationWriter;
	}
	
	@RequestMapping (method = RequestMethod.POST)
	public void write(@RequestBody Reservation reservation) {
		this.reservationWriter.write(reservation.getReservationName());
	}

	public Collection<String> fallback() {
		return new ArrayList<>();
	}


	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> names() {
		return this.reservationReader.read()
				.getContent()
				.stream()
				.map(Reservation :: getReservationName)
				.collect(Collectors.toList());
	}
}
