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

import java.io.*;
import java.nio.charset.Charset;

/**
 * Helper class for reading and writing to files.
 */
public class FileUtils {

    /**
     * Writes a {@link String} to a specified {@link File} with UTF-8 encoding.
     * @param file where to write the content
     * @param content non-null {@link String}
     * @throws IOException
     */
    public static void writeStringToFile(final File file, final String content) throws IOException {
        if (content == null) {
            throw new UnsupportedOperationException("Cannot write null to file. Consider deleting it instead");
        }
        try (OutputStream fos = openOutputStream(file)) {
            fos.write(content.getBytes(Charset.forName("UTF-8")));
        }
    }

    public static FileOutputStream openOutputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    /**
     * Reads a {@link String} from a specified {@link File} with UTF-8 encoding.
     * @param file where to read the content from
     * @return the file's content
     * @throws IOException
     */
    public static String readFileToString(final File file) throws IOException {
        try (FileInputStream inputStream = openInputStream(file)) {
            return IOUtils.readInputStream(inputStream);
        }
    }
}
