package org.logicblaze.lingo;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public abstract class SpringTestSupport extends TestCase {
    protected ConfigurableApplicationContext applicationContext;

    protected void setUp() throws Exception {
        applicationContext = createApplicationContext();
        assertNotNull("Should have an ApplicationContext", applicationContext);
    }


    protected void tearDown() throws Exception {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    protected ConfigurableApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(getApplicationContextXml());
    }

    protected abstract String getApplicationContextXml();

    /**
     * Finds the mandatory bean in the application context failing if its not there
     */
    protected Object getBean(String name) {
        Object answer = applicationContext.getBean(name);
        assertNotNull("Could not find bean in ApplicationContext called: " + name, answer);
        return answer;
    }
}
