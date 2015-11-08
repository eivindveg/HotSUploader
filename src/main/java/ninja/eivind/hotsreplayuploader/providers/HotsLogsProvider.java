package ninja.eivind.hotsreplayuploader.providers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.mpq.models.MpqException;
import ninja.eivind.stormparser.StormParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HotsLogsProvider extends Provider {

    private static final Logger LOG = LoggerFactory.getLogger(HotsLogsProvider.class);
    private static final String ACCESS_KEY_ID = "AKIAIESBHEUH4KAAG4UA";
    private static final String SECRET_ACCESS_KEY = "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";
    private static final String CLIENT_ID = "HotSLogsUploaderFX";
    public static final String BASE_URL = "https://www.hotslogs.com/UploadFile?Source=" + CLIENT_ID;
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

        File file = replayFile.getFile();
        try {
            boolean fileAlreadyUploaded = parseAndCheckStatus(file);
            if(fileAlreadyUploaded) {
                LOG.info("File already uploaded. No need to upload.");
                return Status.UPLOADED;
            } else {
                LOG.info("New replay. Uploading to HotSLogs.com.");
            }
        } catch (IOException e) {
            LOG.warn("Could not check status for replay: " + file, e);
            return Status.EXCEPTION;
        } catch (MpqException e) {
            LOG.error("Could not parse replay. Skipping directly to upload.", e);
        }

        String fileName = UUID.randomUUID() + ".StormReplay";
        LOG.info("Assigning remote file name " + fileName + " to " + replayFile);
        String uri = BASE_URL + "&FileName=" + fileName;

        return uploadFileToHotSLogs(file, fileName, uri);

    }

    private Status uploadFileToHotSLogs(File file, String fileName, String uri) {
        try {
            s3Client.putObject("heroesreplays", fileName, file);
            LOG.info("File " + fileName + "uploaded to remote storage.");
            String result = getHttpClient().simpleRequest(uri).toLowerCase();
            switch (result) {
                case "duplicate":
                case "success":
                case "computerplayerfound":
                case "trymemode":
                    LOG.info("File registered with HotSLogs.com");
                    return Status.UPLOADED;
                case "prealphawipe":
                    LOG.warn("File not supported by HotSLogs.com");
                    return Status.UNSUPPORTED_GAME_MODE;
                case "maintenance":
                    LOG.error("HotSLogs.com is currently undergoing maintenance.");
                    maintenance = System.currentTimeMillis();
                    return Status.NEW;
                default:
                    LOG.error("Could not upload file. Unknown status \"" + result + "\" received.");
                    return Status.EXCEPTION;
            }

        } catch (Exception e) {
            LOG.error("Could not upload file.", e);
            return Status.EXCEPTION;
        }
    }

    private boolean parseAndCheckStatus(File file) throws IOException {
        final StormParser stormParser = new StormParser(file);
        final String matchId = stormParser.getMatchId();
        LOG.info("Calculated matchId to be" + matchId);
        String uri = BASE_URL + "&ReplayHash=" + matchId;
        String result = getHttpClient().simpleRequest(uri).toLowerCase();
        return result.equals("duplicate");
    }
}
