/*
 * Copyright 2016-2017 Eivind Vegsundvåg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ninja.eivind.hotsreplayuploader.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Optional;

@Configuration
public class SettingsConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SettingsConfiguration.class);

    @Bean
    public JavaFXApplicationSettings applicationSettings(File applicationHome) {
        final SimpleApplicationSettings settings = readFromYamlFile(new File(applicationHome, "settings.yml"))
                .orElseGet(() -> {
                    logger.info("Received no settings object from reader. Generating new settings.");
                    return new SimpleApplicationSettings();
                });

        return new JavaFXApplicationSettings(settings);
    }

    private Optional<SimpleApplicationSettings> readFromYamlFile(File file) {
        try(InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            SimpleApplicationSettings value = yaml.loadAs(inputStream, SimpleApplicationSettings.class);
            return Optional.ofNullable(value);
        } catch (FileNotFoundException e) {
            logger.info("No settings file found.");
        } catch (IOException e) {
            logger.warn("Settings seem to be corrupted.", e);
        }
        return Optional.empty();
    }
}
