package com.metacodestudio.hotsuploader.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metacodestudio.hotsuploader.models.Account;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OSUtils {

    private static final String ACCOUNT_FOLDER_FILTER = "(\\d+[^A-Za-z,.\\-()\\s])";
    private static final String HOTS_ACCOUNT_FILTER = "(\\d-Hero-\\d-\\d{1,6})";
    private static final String APPLICATION_DIRECTORY = "HotSUploader";
    private static final String APPLICATION_DIRECTORY_LOWER = "." + APPLICATION_DIRECTORY.toLowerCase();
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String SEPARATOR = System.getProperty("file.separator");

    private OSUtils() {
    }

    public static boolean isMacintosh() {
        return OS_NAME.contains("Mac");
    }

    public static boolean isWindows() {
        return OS_NAME.contains("Windows");
    }

    public static File getApplicationHome() {
        StringBuilder builder = new StringBuilder(USER_HOME).append(SEPARATOR);
        if(isWindows()) {
            builder.append(APPLICATION_DIRECTORY);
        } else if(isMacintosh()) {
            builder.append(APPLICATION_DIRECTORY_LOWER);
        }
        return new File(builder.append(SEPARATOR).toString());
    }

    public static File getPropertiesFile(final File replayFile) {
        final String propertiesFileName = replayFile.toString().replaceAll(".StormReplay", "") + ".json";
        final String replace = propertiesFileName.replace(getHotSHome().toString(), getApplicationHome() + SEPARATOR + "Accounts");
        return new File(replace);
    }

    public static File getReplayFile(final File propertiesFile) {
        final String replayFileName = propertiesFile.toString().replaceAll(".json", "") + ".StormReplay";
        final String replace = replayFileName.replace(getApplicationHome() + SEPARATOR + "Accounts", getHotSHome().toString());
        return new File(replace);
    }

    public static File getHotSHome() {
        StringBuilder builder = new StringBuilder(USER_HOME);
        if (isWindows()) {
            builder.append("\\Documents\\Heroes of the Storm\\Accounts\\");
        } else if (isMacintosh()) {
            builder.append("/Library/Application Support/Blizzard/Heroes of the Storm/Accounts/");
        } else {
            throw new UnsupportedOperationException("This application requires Windows or Macintosh OSX to run");
        }
        return new File(builder.toString());
    }

    public static List<File> getAccountDirectories(final File root) {
        List<File> hotsAccounts = new ArrayList<>();
        File[] files = root.listFiles((dir, name) -> name.matches(ACCOUNT_FOLDER_FILTER));
        System.out.println(root);
        if(files == null) {
            files = new File[0];
        }
        for (final File file : files) {
            File[] hotsFolders = file.listFiles((dir, name) -> name.matches(HOTS_ACCOUNT_FILTER));
            Arrays.stream(hotsFolders)
                    .map(folder -> new File(folder, "Replays"))
                    .map(folder -> new File(folder, "Multiplayer"))
                    .forEach(hotsAccounts::add);
        }
        return hotsAccounts;
    }

    public static List<Account> getAccounts() {
        final ObjectMapper mapper = new ObjectMapper();
        return getAccountDirectories(new File(getApplicationHome(), "Accounts")).stream()
                .map(File::getParentFile)
                .map(File::getParentFile)
                .map(folder -> {
                    String accountName = folder.getName();
                    String[] split = accountName.replace("-Hero", "").split("-");
                    StringBuilder accountNameBuilder = new StringBuilder();
                    for (final String s : split) {
                        accountNameBuilder.append("/").append(s);
                    }
                    String uri = "https://www.hotslogs.com/API/Players" + accountNameBuilder.toString();
                    System.out.println(uri);
                    try {
                        String playerInfo = NetUtils.simpleRequest(uri);
                        System.out.println(playerInfo);
                        return mapper.readValue(playerInfo, Account.class);
                    } catch (IOException e) {
                        return null;
                    }
                }).collect(Collectors.toList());
    }
}
