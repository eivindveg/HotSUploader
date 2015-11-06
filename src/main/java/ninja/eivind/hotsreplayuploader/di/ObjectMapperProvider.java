package ninja.eivind.hotsreplayuploader.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eivind Vegsundvåg
 */
public class ObjectMapperProvider implements Provider<ObjectMapper> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectMapperProvider.class);

    @Override
    public ObjectMapper get() {
        LOG.info("Building ObjectMapper");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
