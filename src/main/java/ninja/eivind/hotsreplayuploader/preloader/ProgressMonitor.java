// Copyright 2016 Eivind Vegsundvåg
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

package ninja.eivind.hotsreplayuploader.preloader;

import javafx.application.Preloader;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

public class ProgressMonitor implements ApplicationContextInitializer<ConfigurableApplicationContext>, ApplicationListener<ApplicationEvent> {

    /*
     * As of 2016-12-17, the bean definition count is 81. We might shrink this, though we'll probably grow it.
     * In any case, >= 75 seems to mean the context is fully mapped
     */
    private static final int MIN_BEAN_DEF_COUNT = 75;
    private PreloaderHandle handle;

    public ProgressMonitor(PreloaderHandle handle) {
        this.handle = handle;
    }


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        applicationContext.addApplicationListener(this);
        beanFactory.addBeanPostProcessor(new ProgressMonitorBeanPostProcessor(beanFactory, handle, MIN_BEAN_DEF_COUNT));
    }

    private void notifyContextIsFullyLoaded() {
        handle.notifyPreloader(new Preloader.ProgressNotification(1));
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            notifyContextIsFullyLoaded();
        }
    }
}
