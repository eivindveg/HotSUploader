package ninja.eivind.hotsreplayuploader.di;

import com.google.inject.AbstractModule;
import ninja.eivind.hotsreplayuploader.repositories.FileRepository;
import ninja.eivind.hotsreplayuploader.repositories.JsonStoreFileRepository;
import ninja.eivind.hotsreplayuploader.repositories.ProviderRepository;
import ninja.eivind.hotsreplayuploader.repositories.SingletonListProviderRepository;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformServiceProvider;

import javax.inject.Singleton;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PlatformService.class).toProvider(new PlatformServiceProvider()).asEagerSingleton();
        bind(FileRepository.class).to(JsonStoreFileRepository.class).asEagerSingleton();
        bind(ProviderRepository.class).to(SingletonListProviderRepository.class).asEagerSingleton();
    }
}
