package com.silkroad.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the SilkRoad marketplace backend application.
 * <p>
 * Boots the Spring application context and starts the embedded server.
 */
@SpringBootApplication
public class MarketApplication {

	/**
	 * Starts the application.
	 *
	 * @param args startup arguments forwarded to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(MarketApplication.class, args);
	}

}
