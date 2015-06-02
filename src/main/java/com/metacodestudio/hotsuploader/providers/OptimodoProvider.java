package com.metacodestudio.hotsuploader.providers;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.sun.jndi.toolkit.url.Uri;

import java.io.FileInputStream;
import java.net.URI;
import java.util.UUID;

public class OptimodoProvider extends Provider {

    public OptimodoProvider() {
        super("Optimodo.com");
    }

    @Override
    public Status upload(final ReplayFile replayFile) {
        String fileName = UUID.randomUUID() + ".StormReplay";
        String uri = "https://optimodo.azurewebsites.net/api/upload/token";

        try {
            String accessToken = getHttpClient().simpleRequest(uri);
            accessToken = accessToken.substring(1, accessToken.length() - 1);
            //Return a reference to the container using the SAS URI.
            CloudBlobContainer container = new CloudBlobContainer(new URI(accessToken));
            CloudBlockBlob blob = container.getBlockBlobReference(fileName);
            blob.uploadFromFile(replayFile.getFile().getAbsolutePath());
            return Status.UPLOADED;
        } catch (Exception e) {
            return null;
        }
    }
}
