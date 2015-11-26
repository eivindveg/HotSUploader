// Copyright 2015 Eivind Vegsundvåg
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

import com.gluonhq.ignite.DIContext;
import javafx.concurrent.Task;

import java.util.Collections;

/**
 * Task to asynchronously load the DIContext. Used in the application start thread to avoid blocking the JavaFX thread
 * during the initialization phase.
 */
public class ContextTask extends Task<DIContext> {

    @Override
    protected DIContext call() throws Exception {
        DIContext context = new CloseableGuiceContext(this, () -> Collections.singletonList(new GuiceModule()));
        context.init();
        return context;
    }
}
