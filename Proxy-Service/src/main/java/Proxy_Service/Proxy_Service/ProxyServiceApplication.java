package Proxy_Service.Proxy_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ProxyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyServiceApplication.class, args);
	}

}
