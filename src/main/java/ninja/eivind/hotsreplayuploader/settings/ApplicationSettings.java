/*
 * Copyright 2015-2017 Eivind Vegsundvåg
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

import ninja.eivind.hotsreplayuploader.settings.window.WindowSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * JavaFX property object for propagating events on changes to settings
 */
@ConfigurationProperties("settings")
public class ApplicationSettings {

    @NestedConfigurationProperty
    private final WindowSettings window = new WindowSettings();

    public WindowSettings getWindow() {
        return window;
    }

}
