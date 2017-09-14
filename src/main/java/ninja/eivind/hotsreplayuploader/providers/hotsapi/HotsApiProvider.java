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

package ninja.eivind.hotsreplayuploader.providers.hotsapi;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.providers.Provider;
import ninja.eivind.stormparser.models.Replay;
import ninja.eivind.hotsreplayuploader.utils.ReplayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * JSON response from hotsapi.net call to check if a replay exists
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class DuplicateResponse {
    @JsonProperty()
    private boolean exists;

    public boolean isDuplicate() {
        return exists;
    }
}

/**
 * JSON response from hotsapi.net upload call
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class UploadResponse {
    @JsonProperty()
    private boolean success;

    @JsonProperty()
    private String status;

    public boolean isSuccess() {
        return success;
    }

    /**
     * Upload result (Success, Duplicate, AiDetected, CustomGame, PtrRegion, TooOld, Incomplete)
     */
    public String getStatus() {
        return status;
    }
}


/**
 * Implements a {@link Provider} to upload replays to hotsapi.net.
 */
@Component
public class HotsApiProvider extends Provider {

    private static final Logger LOG = LoggerFactory.getLogger(HotsApiProvider.class);

    private static final String UPLOAD_URL = "http://hotsapi.net/api/v1/replays/";
    private static final String DUPLICATE_URL = "http://hotsapi.net/api/v1/replays/fingerprints/v3/";

    public HotsApiProvider() {
        super("HotsApi");
    }

    @Override
    public Status upload(final ReplayFile replayFile) {
        final File file = replayFile.getFile();
        if (!(file.exists() && file.canRead())) {
            return Status.EXCEPTION;
        }

        final String fileName = UUID.randomUUID() + ".StormReplay";
        LOG.info("Assigning remote file name " + fileName + " to " + replayFile);

        return uploadFile(file, fileName);
    }

    @Override
    public Status getPreStatus(final Replay replay) {

        // Temporary fix for computer players found until the parser supports this
        if (ReplayUtils.replayHasComputerPlayers(replay)) {
            LOG.info("Computer players found for replay, tagging as uploaded.");
            return Status.UNSUPPORTED_GAME_MODE;
        }
        try {
            final String matchId = ReplayUtils.getMatchId(replay);
            LOG.info("Calculated matchId to be" + matchId);
            final String uri = DUPLICATE_URL + matchId;
            final String response = Request.Get(uri)
                .execute()
                .returnContent()
                .asString();
            final DuplicateResponse result = new ObjectMapper().readValue(response, DuplicateResponse.class);
            if (result.isDuplicate()) {
                return Status.UPLOADED;
            }
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("Platform does not support MD5; cannot proceed with parsing", e);
        } catch (IOException e) {
            return Status.EXCEPTION;
        }
        return Status.NEW;
    }

    private Status uploadFile(File file, String fileName) {
        try {
            final HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, fileName)
                .build();
            final String response = Request.Post(UPLOAD_URL)
                .body(entity)
                .execute()
                .returnContent()
                .asString();
            final UploadResponse result = new ObjectMapper().readValue(response, UploadResponse.class);
            LOG.info("File " + fileName + " uploaded to remote storage.");
            switch (result.getStatus()) {
                case "Duplicate":
                case "Success":
                    LOG.info("File registered with hotsapi.net");
                    return Status.UPLOADED;
                case "AiDetected":
                case "CustomGame":
                case "PtrRegion":
                case "TooOld":
                case "Incomplete":
                    LOG.warn("File not supported by hotsapi.net");
                    return Status.UNSUPPORTED_GAME_MODE;
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
