package ninja.eivind.hotsreplayuploader.di;

import com.google.inject.AbstractModule;
import ninja.eivind.hotsreplayuploader.services.PlatformService;
import ninja.eivind.hotsreplayuploader.services.PlatformServiceProvider;

import javax.inject.Singleton;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PlatformService.class).toProvider(new PlatformServiceProvider()).in(Singleton.class);
    }
}
