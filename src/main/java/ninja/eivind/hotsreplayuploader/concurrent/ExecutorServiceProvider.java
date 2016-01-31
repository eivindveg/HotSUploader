package ninja.eivind.hotsreplayuploader.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

/**
 * {@link Provider} for getting a globally available {@link ExecutorService}.<br>
 */
public class ExecutorServiceProvider implements Provider<ExecutorService>
{
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceProvider.class);

    @Override
    public ExecutorService get()
    {
        LOG.info("Building ExecutorService");
        return Executors.newCachedThreadPool();
    }
}
