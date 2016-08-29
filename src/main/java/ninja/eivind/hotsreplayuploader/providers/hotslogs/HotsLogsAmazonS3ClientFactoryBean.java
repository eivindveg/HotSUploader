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

package ninja.eivind.hotsreplayuploader.providers.hotslogs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("hotslogs-s3client")
@Profile("!test")
public class HotsLogsAmazonS3ClientFactoryBean implements FactoryBean<AmazonS3> {

    private final AWSCredentials credentials;

    @Autowired
    public HotsLogsAmazonS3ClientFactoryBean(@Qualifier("hotslogs-s3credentials") AWSCredentials credentials) {
        this.credentials = credentials;
    }


    @Override
    public AmazonS3 getObject() throws Exception {
        return new AmazonS3Client(credentials);
    }

    @Override
    public Class<?> getObjectType() {
        return AmazonS3.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
