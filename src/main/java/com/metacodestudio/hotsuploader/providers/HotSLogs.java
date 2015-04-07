package com.metacodestudio.hotsuploader.providers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.metacodestudio.hotsuploader.files.ReplayFile;
import com.metacodestudio.hotsuploader.files.Status;

import java.util.UUID;

public class HotSLogs extends Provider {

    public static final String ACCESS_KEY_ID = "AKIAIESBHEUH4KAAG4UA";
    public static final String SECRET_ACCESS_KEY = "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";
    private final AmazonS3Client s3Client;
    private final HttpRequestFactory requestFactory;
    private static long maintenance;

    public HotSLogs() {
        super("HotSLogs.com");
        final NetHttpTransport client = new NetHttpTransport();
        requestFactory = client.createRequestFactory();
        final AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY);
        s3Client = new AmazonS3Client(credentials);
    }

    @Override
    public Status upload(final ReplayFile replayFile) {
        if (isMaintenance()) {
            return null;
        }

        String fileName = UUID.randomUUID() + ".StormReplay";
        String uri = "https://www.hotslogs.com/UploadFile.aspx?FileName=" + fileName;

        try {
            s3Client.putObject("heroesreplays", fileName, replayFile.getFile());
            HttpResponse get = requestFactory.buildGetRequest(new GenericUrl(uri)).execute();
            String result = get.parseAsString();
            switch (result) {
                case "Duplicate":
                case "Success":
                    return Status.UPLOADED;
                case "ComputerPlayerFound":
                case "PreAlphaWipe":
                case "TryMeMode":
                    return Status.UNSUPPORTED_GAME_MODE;
                case "Maintenance":
                    maintenance = System.currentTimeMillis();
                    return Status.NEW;
                default:
                    return Status.EXCEPTION;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Status.NEW;
        }

    }

    public static boolean isMaintenance() {
        return maintenance + 600000L > System.currentTimeMillis();
    }
}
