package ninja.eivind.hotsreplayuploader.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXMLLoader;
import ninja.eivind.hotsreplayuploader.Client;
import ninja.eivind.hotsreplayuploader.models.stringconverters.StatusBinder;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan()
public class HotSReplayUploaderConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HotSReplayUploaderConfiguration.class);

    @Bean
    public ObjectMapper objectMapper() {
        LOG.info("Building ObjectMapper");
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public SimpleHttpClient httpClient() {
        return new SimpleHttpClient();
    }

    @Bean
    public FXMLLoader fxmlLoader(ControllerFactory controllerFactory) {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(controllerFactory);
        return loader;
    }

    @Bean
    public StatusBinder statusBinder() {
        return new StatusBinder();
    }
}
