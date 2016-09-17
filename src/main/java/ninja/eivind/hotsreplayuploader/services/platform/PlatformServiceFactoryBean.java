// Copyright 2016 Eivind Vegsundvåg
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

package ninja.eivind.hotsreplayuploader.services.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class PlatformServiceFactoryBean implements FactoryBean<PlatformService> {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformServiceFactoryBean.class);
    private static final String OS_NAME = System.getProperty("os.name");

    @Override
    public PlatformService getObject() throws Exception {
        LOG.info("Constructing PlatformService for " + OS_NAME);
        if (OS_NAME.contains("Windows")) {
            return new WindowsService();
        } else if (OS_NAME.toLowerCase().contains("mac")) {
            return new OSXService();
        } else if (OS_NAME.contains("Linux")) {
            return new LinuxService();
        } else {
            throw new PlatformNotSupportedException("Operating system not supported");
        }
    }

    @Override
    public Class<?> getObjectType() {
        return PlatformService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
