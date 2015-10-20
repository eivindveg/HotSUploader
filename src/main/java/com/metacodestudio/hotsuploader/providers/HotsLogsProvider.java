package com.metacodestudio.hotsuploader.providers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;

import java.util.UUID;

public class HotsLogsProvider extends Provider {

    private static final String ACCESS_KEY_ID = "AKIAIESBHEUH4KAAG4UA";
    private static final String SECRET_ACCESS_KEY = "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";
    private static final String CLIENT_ID = "HotSLogsUploaderFX";
    private static long maintenance;
    private final AmazonS3Client s3Client;

    public HotsLogsProvider() {
        super("HotSLogs.com");
        final AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY);
        s3Client = new AmazonS3Client(credentials);
    }

    public static boolean isMaintenance() {
        return maintenance + 600000L > System.currentTimeMillis();
    }

    @Override
    public Status upload(final ReplayFile replayFile) {
        if (isMaintenance()) {
            return null;
        }

        String fileName = UUID.randomUUID() + ".StormReplay";
        String uri = "https://www.hotslogs.com/UploadFile.aspx?Source=" + CLIENT_ID + "&FileName=" + fileName;

        try {
            s3Client.putObject("heroesreplays", fileName, replayFile.getFile());
            String result = getHttpClient().simpleRequest(uri);
            switch (result) {
                case "Duplicate":
                case "Success":
                case "ComputerPlayerFound":
                case "TryMeMode":
                    return Status.UPLOADED;
                case "PreAlphaWipe":
                    return Status.UNSUPPORTED_GAME_MODE;
                case "Maintenance":
                    maintenance = System.currentTimeMillis();
                    return Status.NEW;
                default:
                    return Status.EXCEPTION;
            }

        } catch (Exception e) {
            return null;
        }

    }
}
