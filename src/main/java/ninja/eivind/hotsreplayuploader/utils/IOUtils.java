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

package ninja.eivind.hotsreplayuploader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Helper class for input / output directives, e.g. stream handling.
 */
public class IOUtils {
    private IOUtils() {
    }

    /**
     * Convenience method for readings an {@link InputStream}, that contains UTF-8 encoded text.
     * @param inputStream with UTF-8 encoded content
     * @return the stream's content
     * @throws IOException
     */
    public static String readInputStream(final InputStream inputStream) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        try (InputStreamReader in = new InputStreamReader(inputStream, Charset.forName("UTF-8"))) {
            final char[] buffer = new char[4096];
            int n;
            while (-1 != (n = in.read(buffer))) {
                stringBuilder.append(buffer, 0, n);
            }
        } finally {
            inputStream.close();
        }
        return stringBuilder.toString();
    }
}
