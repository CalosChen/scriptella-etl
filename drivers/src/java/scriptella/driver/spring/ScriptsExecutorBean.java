/*
 * Copyright 2006 The Scriptella Project Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scriptella.driver.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.Resource;
import scriptella.configuration.ConfigurationFactory;
import scriptella.execution.ExecutionStatistics;
import scriptella.execution.ScriptsExecutor;
import scriptella.execution.ScriptsExecutorException;
import scriptella.interactive.ProgressIndicator;

import java.io.IOException;
import java.net.URL;

/**
 * Implementation of {@link ScriptsExecutor} for Spring IoC container.
 *
 * @author Fyodor Kupolov
 * @version 1.0
 */
public class ScriptsExecutorBean extends ScriptsExecutor implements BeanFactoryAware {

    private static final ThreadLocal<BeanFactory> LOCAL_BEAN_FACTORY = new ThreadLocal<BeanFactory>();
    private BeanFactory beanFactory;
    private ProgressIndicator progressIndicator;

    /**
     * Creates scripts executor.
     */
    public ScriptsExecutorBean() {
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Sets progress indicator to use.
     * <p>By default no progress shown.
     * @param progressIndicator progress indicator to use.
     */
    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    /**
     * Sets configuration location and loads it.
     * @param resource configuration resource.
     * @see #setConfigLocation(java.net.URL)
     */
    public void setConfigLocation(Resource resource) throws IOException {
        setConfigLocation(resource.getURL());
    }

    /**
     * Loads the configuration from the specified URL.
     * @param url URL to load configuration from.
     */
    private void setConfigLocation(URL url) {
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setResourceURL(url);
        setConfiguration(cf.createConfiguration());
    }

    @Override
    public ExecutionStatistics execute() throws ScriptsExecutorException {
        if (progressIndicator!=null) {
            return execute(progressIndicator);
        } else {
            return super.execute();
        }
    }

    @Override
    public ExecutionStatistics execute(final ProgressIndicator indicator) throws ScriptsExecutorException {
        LOCAL_BEAN_FACTORY.set(beanFactory);
        try {
            return super.execute(indicator);
        } finally {
            LOCAL_BEAN_FACTORY.remove();
        }
    }


    /**
     * @return bean factory associated with the current thread.
     */
    static BeanFactory getContextBeanFactory() {
        BeanFactory f = LOCAL_BEAN_FACTORY.get();
        if (f == null) {
            throw new IllegalStateException("No beanfactory associated with the current thread");
        }
        return f;
    }
}
