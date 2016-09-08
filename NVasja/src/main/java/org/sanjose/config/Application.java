package org.sanjose.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
		SpringApplication.run(Application.class);
	}

	@Bean
	public CommandLineRunner demo(ScpCajaRep scpCajaRep) {
		return (args) -> {
			// save a couple of customers

			List<ScpCaja> scpCajaList = scpCajaRep.findAll();
			for (ScpCaja scpCaja : scpCajaRep.findAll()) {
				log.info(scpCaja.toString());
			}

			/*for (ScpTipodocumento scpTipodocumento : scpTipodocumentoRep.findAll()) {
				log.info(scpTipodocumento.toString());
			} */

/*
			for (GenDepartamento genDepartamento: genDepartamentoRep.findAll()) {
				log.info(genDepartamento.toString());
			}
*/

/*			Customer c1 = new Customer("Jack", "Bauer");
			Customer c2 = new Customer("Chloe", "O'Brian");
			repository.save(c1);
			repository.save(c2);
			repository.save(new Customer("Kim", "Bauer"));
			repository.save(new Customer("David", "Palmer"));
			repository.save(new Customer("Michelle", "Dessler"));

			accountRepository.save(new SavingsAccount(c1));
			accountRepository.save(new DebitAccount(c1));

			accountRepository.save(new SavingsAccount(c2));
			accountRepository.save(new DebitAccount(c2));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (Customer customer : repository.findAll()) {
				log.info(customer.toString());
			}
            log.info("");

			// fetch all customers
			log.info("Accounts found with findAll():");
			log.info("-------------------------------");
			for (Account acc: accountRepository.findAll()) {
				log.info(acc.toString());
			}
			log.info("");

			// fetch all accounts for customer
			log.info("Accounts found with findByCustomer():");
			log.info("-------------------------------");
			for (Account acc: accountRepository.findByCustomer(c1)) {
				log.info(acc.toString());
			}
			log.info("");


			// fetch an individual customer by ID
			Customer customer = repository.findOne(1L);
			log.info("Customer found with findOne(1L):");
			log.info("--------------------------------");
			log.info(customer.toString());
            log.info("");

			// fetch customers by last name
			log.info("Customer found with findByLastName('Bauer'):");
			log.info("--------------------------------------------");
			for (Customer bauer : repository.findByLastName("Bauer")) {
				log.info(bauer.toString());
			}
            log.info("");
			// fetch customers by last name
			log.info("Customer found with findByLastNameStartsWithIgnoreCase('Bauer'):");
			log.info("--------------------------------------------");
			for (Customer bauer : repository
					.findByLastNameStartsWithIgnoreCase("bau")) {
				log.info(bauer.toString());
			}
			log.info("");
  */
		};
	}

}
