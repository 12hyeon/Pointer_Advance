package pointer.Pointer_Spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
@EnableFeignClients
public class PointerSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointerSpringApplication.class, args);
    }

}
