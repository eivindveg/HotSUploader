package ninja.eivind.hotsreplayuploader.services.platform;

import javafx.application.Platform;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsService implements PlatformService {

    private Desktop desktop;

    private Pattern pathPattern = Pattern.compile("[A-Z]:(\\\\|\\w+| )+");
    private File documentsHome;

    public WindowsService() {
        desktop = Desktop.getDesktop();
    }

    @Override
    public File getApplicationHome() {
        if (documentsHome == null) {
            try {
                documentsHome = findMyDocuments();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(documentsHome, APPLICATION_DIRECTORY_NAME);
    }

    @Override
    public File getHotSHome() {
        if (documentsHome == null) {
            try {
                documentsHome = findMyDocuments();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(documentsHome, "Heroes of the Storm\\Accounts");
    }

    @Override
    public TrayIcon getTrayIcon(URL imageURL, Stage primaryStage) {
        final java.awt.Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
        final PopupMenu popup = new PopupMenu();
        final MenuItem showItem = new MenuItem("Show");
        final MenuItem exitItem = new MenuItem("Exit");

        // Deal with window events
        Platform.setImplicitExit(false);
        primaryStage.setOnHiding(value -> {
            primaryStage.hide();
            value.consume();
        });

        // Declare shared action for showItem and trayicon click
        Runnable openAction = () -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        });
        popup.add(showItem);
        popup.add(exitItem);

        final TrayIcon trayIcon = new TrayIcon(image, StormHandler.getApplicationName(), popup);
        trayIcon.setImageAutoSize(true);

        // Add listeners
        trayIcon.addMouseListener(mouseListener(openAction));
        showItem.addActionListener(e -> openAction.run());
        exitItem.addActionListener(event -> {
            Platform.exit();
            System.exit(0);
        });
        return trayIcon;
    }

    private MouseListener mouseListener(final Runnable result) {
        return new TrayMouseListenerBase() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    result.run();
                }
            }
        };
    }

    @Override
    public void browse(final URI uri) throws IOException {
        desktop.browse(uri);
    }

    private File findMyDocuments() throws FileNotFoundException {
        Process p = null;
        String myDocuments = null;
        try {
            p = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            p.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                StringBuilder builder = new StringBuilder();
                reader.lines().forEach(builder::append);
                final String[] values = builder.toString().trim().split("\\s\\s+");
                for (final String value : values) {
                    Matcher matcher = pathPattern.matcher(value);
                    if (matcher.matches()) {
                        myDocuments = matcher.group();
                        break;
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }

        if (myDocuments == null) {
            throw new FileNotFoundException("Could not locate Documents folder");
        }
        return new File(myDocuments);
    }
}
