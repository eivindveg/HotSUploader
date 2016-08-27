// Copyright 2016 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package ninja.eivind.hotsreplayuploader.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXMLLoader;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
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
    public FXMLLoader fxmlLoader(ControllerFactory controllerFactory, BuilderFactory builderFactory) {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(controllerFactory);
        loader.setBuilderFactory(builderFactory);
        return loader;
    }

    @Bean
    public StatusBinder statusBinder() {
        return new StatusBinder();
    }
}
