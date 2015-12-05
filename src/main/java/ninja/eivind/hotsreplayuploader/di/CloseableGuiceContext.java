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

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Simple extension of {@link GuiceContext} that can be closed. Useful for application shutdown.
 */
public class CloseableGuiceContext extends GuiceContext {

    private static final Logger LOG = LoggerFactory.getLogger(CloseableGuiceContext.class);

    /**
     * Simple flag, that gets set to true, if this context was already disposed.
     * Used to prevent multiple closes and therefore spamming the logfile.
     */
    private boolean disposed;

    /**
     * Create the Guice context
     *
     * @param contextRoot root object to inject
     * @param modules     custom Guice modules
     */
    public CloseableGuiceContext(Object contextRoot, Supplier<Collection<Module>> modules) {
        super(contextRoot, modules);
    }

    @Override
    public void dispose() {
        if(disposed)
            return;

        injector.getAllBindings()
                .values()
                .stream()
                .map(binding -> binding.getProvider().get())
                .filter(obj -> obj instanceof Closeable)
                .forEach(obj -> {
                    try {
                        LOG.info("Closing " + obj.getClass().getSimpleName());
                        ((Closeable) obj).close();
                    } catch (IOException e) {
                        LOG.error("Could not close closeable item", e);
                    }
                });

        disposed = true;
    }
}
