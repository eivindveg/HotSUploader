package ninja.eivind.hotsreplayuploader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class ClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientTest.class);
    private static Document parse;

    @BeforeClass
    public static void setUpClass() throws IOException {
        parse = Jsoup.parse(new File("pom.xml"), "UTF-8");
    }

    @Test
    public void testClientIsMainClass() throws Exception {
        String className = parse.select("project > properties > mainClass").text();

        LOG.info("Loading class " + className);
        Class<?> mainClass = Class.forName(className);

        Method main = mainClass.getDeclaredMethod("main", String[].class);
        int modifiers = main.getModifiers();

        Class<?> returnType = main.getReturnType();

        assertEquals("Client is mainClass", Client.class, mainClass);
        assertSame("Main method returns void", returnType, Void.TYPE);
        assertTrue("Main method is static", Modifier.isStatic(modifiers));
        assertTrue("Main method is public", Modifier.isPublic(modifiers));
    }

    @Test
    public void testApplicationHasWindowsIcon() throws Exception {
        String appName = parse.select("project > name").text();
        File icon = new File("src/main/deploy/package/windows/" + appName + ".ico");

        assertTrue("Windows icon exists", icon.exists());
    }

    @Test
    public void testApplicationHasOSXIcon() throws Exception {
        String appName = parse.select("project > name").text();
        File icon = new File("src/main/deploy/package/macosx/" + appName + ".icns");

        assertTrue("OSX icon exists", icon.exists());
    }
}
