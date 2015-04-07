package com.metacodestudio.hotsuploader.files;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

public class ReplayUtils {
    private ReplayUtils() {

    }

    public static List<UploadStatus> fromJson(List<LinkedTreeMap<String, String>> json) {
        List<UploadStatus> list = new ArrayList<>();
        json.forEach(st -> list.add(new UploadStatus(String.valueOf(st.get("host")), Status.valueOf(st.get("status")))));
        return list;
    }
}
