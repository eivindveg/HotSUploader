package ninja.eivind.hotsreplayuploader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StormHandler {

    private static final String APPLICATION_DIRECTORY_NAME = "HotSLogs UploaderFX";
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String SEPARATOR = System.getProperty("file.separator");
    private final String ACCOUNT_FOLDER_FILTER = "(\\d+[^A-Za-z,.\\-()\\s])";
    private final String hotsAccountFilter = "(\\d-Hero-\\d-\\d{1,20})";
    private final String USER_HOME = System.getProperty("user.home");
    private final String OSX_LIBRARY = "/Library/Application Support/";

    private File applicationHome;
    private File hotsHome;

    public StormHandler() {
        System.out.println("Detected Heroes of the Storm profile: " + getHotSHome());
        System.out.println("Using Uploader directory: " + getApplicationHome());
    }

    public static boolean isLinux() {
        return OS_NAME.contains("Linux");
    }

    public static boolean isMacintosh() {
        return OS_NAME.contains("Mac");
    }

    public static boolean isWindows() {
        return OS_NAME.contains("Windows");
    }

    public File getApplicationHome() {
        if (applicationHome == null) {
            applicationHome = buildApplicationHome();
        }
        return applicationHome;
    }

    private File buildApplicationHome() {
        StringBuilder builder = new StringBuilder(USER_HOME).append(SEPARATOR);
        if (isWindows()) {
            builder.append("\\Documents\\" + APPLICATION_DIRECTORY_NAME);
        } else if (isMacintosh()) {
            builder.append(OSX_LIBRARY + "MetaCode Studio/").append(APPLICATION_DIRECTORY_NAME);
        } else if (isLinux()) {
            builder.append(APPLICATION_DIRECTORY_NAME);
        }
        return new File(builder.append(SEPARATOR).toString());
    }

    public File getPropertiesFile(final File replayFile) {
        final String propertiesFileName = replayFile.toString().replaceAll(".StormReplay", "") + ".json";
        final String replace = propertiesFileName.replace(getHotSHome().toString(), getApplicationHome() + SEPARATOR + "Accounts");
        return new File(replace);
    }

    public File getReplayFile(final File propertiesFile) {
        final String replayFileName = propertiesFile.toString().replaceAll(".json", "") + ".StormReplay";
        final String replace = replayFileName.replace(getApplicationHome() + SEPARATOR + "Accounts", getHotSHome().toString());
        return new File(replace);
    }

    public File getHotSHome() {
        if (hotsHome == null) {
            hotsHome = buildHotSHome();
        }
        return hotsHome;
    }

    private File buildHotSHome() {
        StringBuilder builder = new StringBuilder(USER_HOME);
        if (isWindows()) {
            builder.append("\\Documents\\Heroes of the Storm\\Accounts\\");
        } else if (isMacintosh()) {
            builder.append(OSX_LIBRARY + "Blizzard/Heroes of the Storm/Accounts/");
        } else if (isLinux()) {
            System.out.println("Attention! Linux is experimental and not official supported!");
            builder.append("/Heroes of the Storm/Accounts/");
        } else {
            throw new UnsupportedOperationException("This application requires Windows, OSX or GNU/Linux to run");
        }
        return new File(builder.toString());
    }

    public List<File> getAccountDirectories(final File root) {
        List<File> hotsAccounts = new ArrayList<>();
        File[] files = root.listFiles((dir, name) -> name.matches(ACCOUNT_FOLDER_FILTER));
        if (files == null) {
            files = new File[0];
        }
        for (final File file : files) {
            File[] hotsFolders = file.listFiles((dir, name) -> name.matches(hotsAccountFilter));
            Arrays.stream(hotsFolders)
                    .map(folder -> new File(folder, "Replays"))
                    .map(folder -> new File(folder, "Multiplayer"))
                    .forEach(hotsAccounts::add);
        }
        return hotsAccounts;
    }

    public List<String> getAccountStringUris() {
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
                    return "https://www.hotslogs.com/API/Players" + accountNameBuilder.toString();
                }).collect(Collectors.toList());
    }

    public String getHotSAccountFilter() {
        return hotsAccountFilter;
    }

    public static String getApplicationName() {
        return APPLICATION_DIRECTORY_NAME;
    }
}
