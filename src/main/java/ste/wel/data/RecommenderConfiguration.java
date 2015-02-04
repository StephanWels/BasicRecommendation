package ste.wel.data;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommenderConfiguration {

    private static final String BASKETS_CSV = "src/main/resources/baskets.csv";

    @Bean
    public ItemProvider itemProvider() throws IOException {
        return new ItemProvider(BASKETS_CSV);
    }

}
