package az.jet.logregis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LogRegisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogRegisApplication.class, args);
    }

}
