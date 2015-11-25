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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Provider} for Jackson's {@link ObjectMapper}. Required to provide an ObjectMapper capable of deserializing
 * JSR310 objects through {@link JavaTimeModule}.
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
