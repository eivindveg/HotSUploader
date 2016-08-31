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

package ninja.eivind.hotsreplayuploader;

import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.TestEnvironmentPlatformService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rules.JavaFXThreadingRule;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@HotsReplayUploaderTest
public class ClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientTest.class);
    private static Document parse;

    @Rule
    public JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    @Autowired
    private DataSource dataSource;
    @Autowired
    private PlatformService platformService;

    @BeforeClass
    public static void setUpClass() throws IOException {
        parse = Jsoup.parse(new File("pom.xml"), "UTF-8");
    }

    @Test
    public void testDataSourceIsEmbedded() {
        assertTrue("DataSource is an instance of EmbeddedDatabase", dataSource instanceof EmbeddedDatabase);
    }

    @Test
    public void testPlatformServiceIsTestEnvironment() {
        assertTrue("PlatformService is an instance of TestEnvironmentPlatformService",
                platformService instanceof TestEnvironmentPlatformService);
    }

    @Test
    public void testJavaFXIsAvailable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Task<Void> javaFxTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                latch.countDown();
                return null;
            }
        };
        javaFxTask.setOnSucceeded((result) -> latch.countDown());
        new Thread(javaFxTask).run();

        if(!latch.await(1, TimeUnit.SECONDS)) {
            fail("JavaFX is not available.");
        }
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
