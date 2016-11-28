// Copyright 2015-2016 Eivind Vegsundvåg
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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.providers.Provider;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.stormparser.models.Player;
import ninja.eivind.stormparser.models.PlayerType;
import ninja.eivind.stormparser.models.Replay;
import ninja.eivind.stormparser.models.replaycomponents.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements a {@link Provider} to upload replays to hotslogs.com.<br>
 * Uses the Amazon AWS S3 data store to save the file
 * and an HTTP request to hotslogs.com to submit it to the site.
 */
@Component
public class HotsLogsProvider extends Provider {

    private static final Logger LOG = LoggerFactory.getLogger(HotsLogsProvider.class);

    private static final String CLIENT_ID = "HotSLogsUploaderFX";
    private static final String BASE_URL = "https://www.hotslogs.com/UploadFile?Source=" + CLIENT_ID;
    private static long maintenance;

    private final AmazonS3 s3Client;

    private SimpleHttpClient httpClient;

    @Autowired
    public HotsLogsProvider(SimpleHttpClient httpClient, @Qualifier("hotslogs-s3client") AmazonS3 s3Client) {
        super("HotSLogs.com");
        this.httpClient = httpClient;
        this.s3Client = s3Client;
    }

    public static boolean isMaintenance() {
        return maintenance + 600000L > System.currentTimeMillis();
    }

    private static UUID getUUIDForString(String concatenatedString) throws NoSuchAlgorithmException {
        final byte[] hashed = MessageDigest.getInstance("MD5").digest(concatenatedString.getBytes());
        final byte[] reArranged = reArrangeForUUID(hashed);
        return getUUID(reArranged);
    }

    private static byte[] reArrangeForUUID(byte[] hashed) {
        return new byte[]{
                hashed[3],
                hashed[2],
                hashed[1],
                hashed[0],

                hashed[5],
                hashed[4],
                hashed[7],
                hashed[6],
                hashed[8],
                hashed[9],
                hashed[10],
                hashed[11],
                hashed[12],
                hashed[13],
                hashed[14],
                hashed[15],
        };
    }

    private static UUID getUUID(byte[] bytes) {
        long msb = 0;
        long lsb = 0;
        assert bytes.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }

    @Override
    public Status upload(final ReplayFile replayFile) {
        if (isMaintenance()) {
            return null;
        }

        final File file = replayFile.getFile();
        if (!(file.exists() && file.canRead())) {
            return Status.EXCEPTION;
        }

        final String fileName = UUID.randomUUID() + ".StormReplay";
        LOG.info("Assigning remote file name " + fileName + " to " + replayFile);
        final String uri = BASE_URL + "&FileName=" + fileName;

        return uploadFileToHotSLogs(file, fileName, uri);

    }

    @Override
    public Status getPreStatus(final Replay replay) {

        // Temporary fix for computer players found until the parser supports this
        if (replayHasComputerPlayers(replay)) {
            LOG.info("Computer players found for replay, tagging as uploaded.");
            return Status.UNSUPPORTED_GAME_MODE;
        }
        try {
            final String matchId = getMatchId(replay);
            LOG.info("Calculated matchId to be" + matchId);
            final String uri = BASE_URL + "&ReplayHash=" + matchId;
            final String result = getHttpClient().simpleRequest(uri).toLowerCase();
            if (result.equals("duplicate")) {
                return Status.UPLOADED;
            }
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("Platform does not support MD5; cannot proceed with parsing", e);
        } catch (IOException e) {
            return Status.EXCEPTION;
        }
        return Status.NEW;
    }

    private Status uploadFileToHotSLogs(File file, String fileName, String uri) {
        try {
            s3Client.putObject("heroesreplays", fileName, file);
            LOG.info("File " + fileName + " uploaded to remote storage.");
            final String result = getHttpClient().simpleRequest(uri).toLowerCase();
            switch (result) {
                case "duplicate":
                case "success":
                    LOG.info("File registered with HotSLogs.com");
                    return Status.UPLOADED;
                case "prealphawipe":
                case "computerplayerfound":
                case "trymemode":
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

    private boolean replayHasComputerPlayers(Replay replay) {
        return replay.getReplayDetails()
                .getPlayers()
                .stream()
                .map(Player::getPlayerType)
                .anyMatch(playerType -> playerType == PlayerType.COMPUTER);
    }

    protected String getMatchId(Replay replay) throws NoSuchAlgorithmException {
        final String concatenatedString = getConcatenatedString(replay);

        return getUUIDForString(concatenatedString).toString();

    }

    private String getConcatenatedString(Replay replay) {
        final String randomValue = String.valueOf(replay.getInitData().getRandomValue());
        final List<String> battleNetIdsSorted = replay.getReplayDetails()
                .getPlayers()
                .stream()
                .map(Player::getBNetId)
                .map(Long::parseLong)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.toList());
        final StringBuilder builder = new StringBuilder();
        battleNetIdsSorted.forEach(builder::append);
        builder.append(randomValue);
        return builder.toString();
    }

    public SimpleHttpClient getHttpClient() {
        return httpClient;
    }

    protected void setHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
