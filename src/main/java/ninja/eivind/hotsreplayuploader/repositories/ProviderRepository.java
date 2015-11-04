package ninja.eivind.hotsreplayuploader.repositories;

import ninja.eivind.hotsreplayuploader.providers.Provider;

import java.util.Collection;

public interface ProviderRepository {

    Collection<Provider> getAll();

}
