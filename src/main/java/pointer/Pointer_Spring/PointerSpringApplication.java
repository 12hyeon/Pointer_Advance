package pointer.Pointer_Spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import pointer.Pointer_Spring.config.AppProperties;

@EnableCaching
@EnableJpaAuditing
@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
@EnableFeignClients
public class PointerSpringApplication {
	public static void main(String[] args) {
		SpringApplication.run(PointerSpringApplication.class, args);
	}

}
