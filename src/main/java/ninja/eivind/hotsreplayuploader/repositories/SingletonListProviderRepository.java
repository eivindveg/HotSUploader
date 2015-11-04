package ninja.eivind.hotsreplayuploader.repositories;

import ninja.eivind.hotsreplayuploader.providers.HotsLogsProvider;
import ninja.eivind.hotsreplayuploader.providers.Provider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SingletonListProviderRepository implements ProviderRepository {

    private List<Provider> all = Collections.singletonList(new HotsLogsProvider());
    @Override
    public Collection<Provider> getAll() {
        return all;
    }
}
