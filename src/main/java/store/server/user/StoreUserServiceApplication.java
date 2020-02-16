package store.server.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class StoreUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreUserServiceApplication.class, args);
    }

}
