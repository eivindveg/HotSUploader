package ninja.eivind.hotsreplayuploader.providers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HotsLogsProvider extends Provider {

    private static final Logger LOG = LoggerFactory.getLogger(HotsLogsProvider.class);
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
        LOG.info("Assigning remote file name " + fileName + " to " + replayFile);
        String uri = "https://www.hotslogs.com/UploadFile.aspx?Source=" + CLIENT_ID + "&FileName=" + fileName;

        try {
            s3Client.putObject("heroesreplays", fileName, replayFile.getFile());
            LOG.info("File " + fileName + "uploaded to remote storage.");
            String result = getHttpClient().simpleRequest(uri);
            switch (result) {
                case "Duplicate":
                case "Success":
                case "ComputerPlayerFound":
                case "TryMeMode":
                    LOG.info("File registered with HotSLogs.com");
                    return Status.UPLOADED;
                case "PreAlphaWipe":
                    LOG.warn("File not supported by HotSLogs.com");
                    return Status.UNSUPPORTED_GAME_MODE;
                case "Maintenance":
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
}
