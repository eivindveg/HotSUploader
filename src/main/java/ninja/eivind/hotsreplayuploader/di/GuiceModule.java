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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.repositories.*;
import ninja.eivind.hotsreplayuploader.services.UploaderService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * {@link com.gluonhq.ignite.DIContext} implementation that wires into Ignite-Guice to service the application's
 * CDI needs.
 */
public class GuiceModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(GuiceModule.class);

    @Override
    protected void configure() {
        LOG.info("Instantiating IoC Container");
        bind(PlatformService.class).toProvider(new PlatformServiceProvider()).asEagerSingleton();
        bind(UploaderService.class).asEagerSingleton();
        bind(FileRepository.class).to(OrmLiteFileRepository.class).asEagerSingleton();
        bind(DataSource.class).toProvider(DataSourceProvider.class).asEagerSingleton();
        bind(ConnectionSource.class).toProvider(ConnectionSourceProvider.class).asEagerSingleton();
        bind(ProviderRepository.class).to(SingletonListProviderRepository.class).asEagerSingleton();
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).asEagerSingleton();
        LOG.info("IoC Container instantiated");

        bindListener(new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(final TypeLiteral<?> typeLiteral) {
                final Class<?> rawType = typeLiteral.getRawType();
                final boolean assignableFrom = Initializable.class.isAssignableFrom(rawType);
                LOG.info("Type " + rawType + " is " + (assignableFrom ? "" : "not ") + "Initializable.");
                return assignableFrom;
            }
        }, new TypeListener() {
            @Override
            public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
                LOG.info("Binding " + type.getType().getTypeName() + " to post inject.");
                encounter.register((InjectionListener<I>) injectee -> {
                    if (injectee instanceof Initializable) {
                        ((Initializable) injectee).initialize();
                    }
                });
            }
        });
    }
}
