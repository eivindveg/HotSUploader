package com.metacodestudio.hotsuploader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * NOTICE: MANY METHODS HERE ARE DIRECTLY REVERSED ENGINEERED FROM APACHE SOFTWARE FOUNDATION'S COMMONS-IO LIBRARY
 * https://github.com/apache/commons-io/blob/trunk/src/main/java/org/apache/commons/io/IOUtils.java
 * <p/>
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class IOUtils {
    private IOUtils() {
    }

    public static String readInputStream(final InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
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
