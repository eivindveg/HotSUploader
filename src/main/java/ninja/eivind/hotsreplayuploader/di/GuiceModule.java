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
import ninja.eivind.hotsreplayuploader.repositories.FileRepository;
import ninja.eivind.hotsreplayuploader.repositories.JsonStoreFileRepository;
import ninja.eivind.hotsreplayuploader.repositories.ProviderRepository;
import ninja.eivind.hotsreplayuploader.repositories.SingletonListProviderRepository;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiceModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(GuiceModule.class);

    @Override
    protected void configure() {
        LOG.info("Instantiating IoC Container");
        bind(PlatformService.class).toProvider(new PlatformServiceProvider()).asEagerSingleton();
        bind(FileRepository.class).to(JsonStoreFileRepository.class).asEagerSingleton();
        bind(ProviderRepository.class).to(SingletonListProviderRepository.class).asEagerSingleton();
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).asEagerSingleton();
        LOG.info("IoC Container instantiated");
    }
}
