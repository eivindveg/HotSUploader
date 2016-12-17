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
import javafx.scene.control.ProgressIndicator;
import ninja.eivind.hotsreplayuploader.preloader.notifications.BeanLoadedNotification;
import ninja.eivind.hotsreplayuploader.preloader.notifications.BeanLoadingNotification;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class ProgressMonitorBeanPostProcessor implements BeanPostProcessor {
    private final PreloaderHandle handle;
    private final int minBeanDefCount;
    private final ConfigurableListableBeanFactory beanFactory;
    private int loadedBeans = 0;
    private int beanDefinitionCount;

    public ProgressMonitorBeanPostProcessor(ConfigurableListableBeanFactory beanFactory, PreloaderHandle handle,
                                            int minBeanDefCount) {
        this.handle = handle;
        this.minBeanDefCount = minBeanDefCount;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final int beanDefinitionCount = beanFactory.getBeanDefinitionCount();
        if (beanDefinitionCount >= minBeanDefCount) {
            this.beanDefinitionCount = beanDefinitionCount;
            notifyContextIsLoadingBean(beanName);
        }
        return bean;
    }

    private void notifyContextIsLoadingBean(String beanName) {
        handle.notifyPreloader(new BeanLoadingNotification(beanName));
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        loadedBeans++;
        final double progress;
        if (beanDefinitionCount > 0) {
            progress = (double) loadedBeans / (double) beanDefinitionCount;
            handle.notifyPreloader(new Preloader.ProgressNotification(progress));
        } else {
            progress = ProgressIndicator.INDETERMINATE_PROGRESS;
        }
        notifyContextLoadedBean(beanName, progress);
        return bean;
    }

    private void notifyContextLoadedBean(String beanName, double progress) {
        handle.notifyPreloader(new BeanLoadedNotification(beanName, progress));
    }
}
