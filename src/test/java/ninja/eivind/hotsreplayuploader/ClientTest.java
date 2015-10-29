package ninja.eivind.hotsreplayuploader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientTest {

    @Test
    public void testClientIsMainClass() throws Exception {
        Document parse = Jsoup.parse(new File("pom.xml"), "UTF-8");
        String className = parse.select("project > properties > mainClass").text();

        System.out.println("Loading class " + className);
        Class<?> mainClass = Class.forName(className);

        Method main = mainClass.getDeclaredMethod("main", String[].class);
        int modifiers = main.getModifiers();

        Class<?> returnType = main.getReturnType();

        assertEquals("Client is mainClass", Client.class, mainClass);
        assertEquals("Main method returns void", returnType, Void.class);
        assertTrue("Main method is static", Modifier.isStatic(modifiers));
        assertTrue("Main method is public", Modifier.isPublic(modifiers));
    }
}
