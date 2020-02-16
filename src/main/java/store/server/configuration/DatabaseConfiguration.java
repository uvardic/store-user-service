package store.server.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan("store.server")
@EnableTransactionManagement
@EnableJpaRepositories("store.server")
public class DatabaseConfiguration {
}
