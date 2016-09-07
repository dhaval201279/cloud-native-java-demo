package com.its;

import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}

@Service
class AccountUserDetailsService implements UserDetailsService {
	private final AccountRepository accountRepostiory;
		
	/**
	 * @param accountRepostiory
	 */
	@Autowired
	public AccountUserDetailsService(AccountRepository accountRepostiory) {
		this.accountRepostiory = accountRepostiory;
	}


	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		/*return this.accountRepostiory.findByUserName(userName)
				.map(account -> new User(account., password, authorities))
				.orelsethrow;*/
		return null;
	}
	
}

interface AccountRepository extends JpaRepository<Account, Long>{
	Optional<Account> findByUserName (String userName);
}

@Entity
class Account {
	@Id
	@GeneratedValue
	private Long id;
	private String userName, password;
	private boolean isActive;
	
	/**
	 * 
	 */
	public Account() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param userName
	 * @param password
	 */
	public Account(String userName, String password, Boolean isActive) {
		this.userName = userName;
		this.password = password;
		this.isActive = isActive;
	}

	public String toString() {
		return " Account { " + " Id = " + id + 
			" , User Name = " + userName +
			", Password = " + password +
			"active = " + isActive + "}";
	}
	
	
}
