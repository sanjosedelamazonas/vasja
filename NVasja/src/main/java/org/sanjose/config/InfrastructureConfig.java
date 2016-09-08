/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sanjose.config;

import org.sanjose.config.ApplicationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Common infrastructure configuration class to setup a Spring container and infrastructure components like a
 * {@link DataSource}, a {@link EntityManagerFactory} and a {@link PlatformTransactionManager}. Will be used by the
 * configuration activating the Spring Data JPA based one (see {@link ApplicationConfig}).
 * 
 * @author Oliver Gierke, Pawel Rubach
 */
@Configuration
@EnableTransactionManagement
public class InfrastructureConfig {

	/**
	 * Bootstraps an in-memory HSQL database.
	 * 
	 * @return
	 * @see http 
	 *      ://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/jdbc.html#jdbc-embedded-database
	 *      -support
	 */
/*	@Bean
	public DataSource dataSource() {		
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.HSQL).build();
	}*/
	
	@Bean
	public DataSource persistentDataSource() {		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		/*spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=testdb
		spring.datasource.username=sa
		spring.datasource.password=myPassword
		spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerConnection
		spring.datasource.initialize=true

		spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=testdb;integratedSecurity=false

        */
		dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerConnection");
		dataSource.setUrl("jdbc:sqlserver://192.168.1.114:1433;databaseName=SCP");
		dataSource.setUsername("vasja");
		dataSource.setPassword("vasja123");

		//EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return dataSource;
	}
	
	
	/**
	 * Sets up a {@link LocalContainerEntityManagerFactoryBean} to use Hibernate. Activates picking up entities from the
	 * project's base package.
	 * 
	 * @return
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.SQL_SERVER);
		//vendorAdapter.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(getClass().getPackage().getName());
		
		factory.setDataSource(persistentDataSource());

		return factory;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return txManager;
	}
}
