// Copyright 2015 Eivind Vegsundvåg
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

package rules;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

/**
 * A JUnit {@link Rule} for running tests on the JavaFX thread and performing
 * JavaFX initialisation.  To include in your test case, add the following code:
 * <pre>
 * {@literal @}Rule
 * public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
 * </pre>
 *
 */
public class JavaFXThreadingRule implements TestRule {

    private static final Logger logger = LoggerFactory.getLogger(JavaFXThreadingRule.class);
    /**
     * Flag for setting up the JavaFX, we only need to do this once for all tests.
     */
    private static boolean jfxIsSetup;

    public JavaFXThreadingRule() {
        System.setProperty("java.awt.headless", "false");
    }

    @Override
    public Statement apply(Statement statement, Description description) {

        return new OnJFXThreadStatement(statement);
    }

    private static class OnJFXThreadStatement extends Statement {

        private final Statement statement;

        public OnJFXThreadStatement(Statement aStatement) {
            statement = aStatement;
        }

        @Override
        public void evaluate() throws Throwable {

            if (!jfxIsSetup) {
                setupJavaFX();

                jfxIsSetup = true;
            }

            statement.evaluate();
        }

        protected void setupJavaFX() throws InterruptedException {

            long timeMillis = System.currentTimeMillis();

            final CountDownLatch latch = new CountDownLatch(1);

            logger.info("Attempting to initialize JavaFX");

            SwingUtilities.invokeLater(() -> {
                // initializes JavaFX environment
                logger.info("Initializing JavaFX...");
                new JFXPanel();
                logger.info("JavaFX initialized.");

                latch.countDown();
            });

            latch.await();
            System.out.println("JavaFX was initialized in " + ((double) (System.currentTimeMillis() - timeMillis) / 1000.0) + "s");
        }

    }
}
