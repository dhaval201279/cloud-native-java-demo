package com.its;

import java.security.Principal;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableResourceServer
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}

@RestController
class PrincipalRestController {
	@RequestMapping("/user")
	Principal principal(Principal principal) {
		return principal;
	}
}

@Configuration
@EnableAuthorizationServer
class OAuthConfiguration extends AuthorizationServerConfigurerAdapter {

	private final AuthenticationManager authenticationManager;
	
	@Autowired
	public OAuthConfiguration(AuthenticationManager authenticationManager) {
		super();
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(this.authenticationManager);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		// TODO Auto-generated method stub
		super.configure(security);
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
			.withClient("acme")
			.secret("acmesecret")
			.authorizedGrantTypes("password")
			.scopes("openid");
	}
	
}

@Component
class AccountCLR implements CommandLineRunner {

	private final AccountRepository accountRepository;
	
	@Autowired
	public AccountCLR(AccountRepository accountRepository) {
		super();
		this.accountRepository = accountRepository;
	}


	@Override
	public void run(String... args) throws Exception {
		Stream.of("jlong,spring", "pweeb,boot", "dsyer,cloud")
			.map(x -> x.split(","))
			.forEach(tuple -> this.accountRepository.save(new Account(tuple[0], tuple[1], true)));
		
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
		return this.accountRepostiory.findByUserName(userName)
				.map(account -> new User(account.getUserName(), account.getPassword(), 
						account.isActive(), account.isActive(),	account.isActive(),account.isActive(),
						AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")))
				.orElseThrow(() -> new UsernameNotFoundException("No user name : " + userName + " !"));
				
		//return null;
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

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String toString() {
		return " Account { " + " Id = " + id + 
			" , User Name = " + userName +
			", Password = " + password +
			"active = " + isActive + "}";
	}
	
	
}
