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
    }
}
