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

package ninja.eivind.hotsreplayuploader.services.platform;

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformServiceProvider implements Provider<PlatformService> {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformServiceProvider.class);
    private static final String OS_NAME = System.getProperty("os.name");

    @Override
    public PlatformService get() {
        LOG.info("Constructing PlatformService for " + OS_NAME);
        if (OS_NAME.contains("Windows")) {
            return new WindowsService();
        } else if (OS_NAME.contains("Mac")) {
            return new OSXService();
        } else if (OS_NAME.contains("Linux")) {
            return new LinuxService();
        } else {
            throw new PlatformNotSupportedException("Operating system not supported");
        }
    }
}
