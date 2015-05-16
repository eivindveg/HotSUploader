package com.metacodestudio.hotsuploader.providers;

import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.UUID;

public class HeroGGProvider extends Provider {

    private static final String ACCESS_KEY_ID = "beta:anQA9aBp";
    private static final String ENCODING = "UTF-8";

    public HeroGGProvider() {
        super("Hero.GG");
    }

    @Override
    public Status upload(final ReplayFile replayFile) {
        String uri = "http://upload.hero.gg/ajax/upload-replay";

        String boundary = String.format("----------%s", UUID.randomUUID().toString().replaceAll("-", ""));
        String contentType = "multipart/form-data; boundary=" + boundary;
        byte[] fileData = getFileData(replayFile, boundary);

        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return Status.EXCEPTION;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("User-Agent", "HeroGG");
            connection.setFixedLengthStreamingMode((long) fileData.length);
            connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(ACCESS_KEY_ID.getBytes(ENCODING))));

            OutputStream requestStream = connection.getOutputStream();
            requestStream.write(fileData, 0, fileData.length);
            requestStream.close();

            byte[] b;
            try (InputStream responseStream = connection.getInputStream()) {
                b = new byte[responseStream.available()];
                responseStream.read(b);
            }

            String result = new String(b, Charset.defaultCharset());
            JSONObject resultObj = new JSONObject(result);
            String status = resultObj.getString("success");

            if ("true".equals(status)) {
                return Status.UPLOADED;
            } else {
                return Status.EXCEPTION;
            }

        } catch (UnsupportedEncodingException | ProtocolException | MalformedURLException | JSONException e) {
            return Status.EXCEPTION;
        } catch (IOException e) {
            return Status.NEW;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private byte[] getFileData(final ReplayFile replayFile, String boundary) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        String key = getContentString(boundary, "key", "Nothing goes here at the moment");
        String name = getContentString(boundary, "name", replayFile.getFile().getName());
        String file = getContentString(boundary, "file", replayFile.getFile());
        String closing = "\r\n--" + boundary + "--\r\n";
        String newLine = "\r\n";

        try {
            byte[] fileContents = Files.readAllBytes(replayFile.getFile().toPath());

            stream.write(key.getBytes(ENCODING));
            stream.write(newLine.getBytes(ENCODING));
            stream.write(name.getBytes(ENCODING));
            stream.write(newLine.getBytes(ENCODING));
            stream.write(file.getBytes(ENCODING));
            stream.write(fileContents);
            stream.write(closing.getBytes(ENCODING));
            return stream.toByteArray();

        } catch (IOException e) {
            return null;
        }

    }

    private String getContentString(String boundary, String key, String value) {
        Object[] params = new Object[]{boundary, key, value};
        String s = String.format("--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"\r\n\r\n%3$s", params);

        return s;
    }

    private String getContentString(String boundary, String key, File value) {
        Object[] params = new Object[]{boundary, key, value.getName(), "application/x-www-form-urlencoded"};
        String s = String.format("--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"; filename=\"%3$s\"\r\nContent-Type: %4$s\r\n\r\n", params);

        return s;
    }
}
