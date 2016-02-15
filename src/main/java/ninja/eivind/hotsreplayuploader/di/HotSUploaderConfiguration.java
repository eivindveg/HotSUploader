package ninja.eivind.hotsreplayuploader.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotSUploaderConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HotSUploaderConfiguration.class);

    @Bean
    public ObjectMapper objectMapper() {
        LOG.info("Building ObjectMapper");
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public SimpleHttpClient httpClient() {
        return new SimpleHttpClient();
    }
}
