package ninja.eivind.hotsreplayuploader.di;

import com.google.inject.AbstractModule;
import ninja.eivind.hotsreplayuploader.repositories.FileRepository;
import ninja.eivind.hotsreplayuploader.repositories.JsonStoreFileRepository;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformServiceProvider;

import javax.inject.Singleton;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PlatformService.class).toProvider(new PlatformServiceProvider()).in(Singleton.class);
        bind(FileRepository.class).to(JsonStoreFileRepository.class).in(Singleton.class);
    }
}
