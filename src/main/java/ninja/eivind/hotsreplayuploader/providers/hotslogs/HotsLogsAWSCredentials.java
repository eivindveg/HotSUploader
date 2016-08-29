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

import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.stereotype.Component;

@Component("hotslogs-s3credentials")
public class HotsLogsAWSCredentials extends BasicAWSCredentials {

    private static final String ACCESS_KEY = "AKIAIESBHEUH4KAAG4UA";
    private static final String SECRET_KEY = "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";

    public HotsLogsAWSCredentials() {
        super(ACCESS_KEY, SECRET_KEY);
    }
}
