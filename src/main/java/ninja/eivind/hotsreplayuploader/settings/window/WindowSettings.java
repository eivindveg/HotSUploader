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

package ninja.eivind.hotsreplayuploader.settings.window;

import javafx.beans.property.SimpleBooleanProperty;

public class WindowSettings {

    private final SimpleBooleanProperty startMinimized = new SimpleBooleanProperty(false);

    public boolean isStartMinimized() {
        return startMinimized.get();
    }

    public void setStartMinimized(boolean startMinimized) {
        this.startMinimized.set(startMinimized);
    }

    public SimpleBooleanProperty startMinimizedProperty() {
        return startMinimized;
    }

}
