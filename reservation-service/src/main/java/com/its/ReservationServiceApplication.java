package com.its;

import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {
	
}

@Entity
class Reservation {
	
	
	public Reservation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	@Id @GeneratedValue
	private Long id;
	
	private String reservationName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	@Override
	public String toString() {
		return "Reservation [id=" + id + ", reservationName=" + reservationName + "]";
	}
		
}

@Component
class sampleDataCLR implements CommandLineRunner {

	private ReservationRepository reservationRepository;
	
	@Autowired
	public sampleDataCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}


	@Override
	public void run(String... args) throws Exception {
		Stream.of("Dhaval S", "Bhavin", "Dhaval P", "Jigar", "Sharad", "Vishal")
			.forEach(name -> reservationRepository.save(new Reservation(name)));
		
		reservationRepository.findAll().forEach(System.out :: println);
		
	}
	
}



