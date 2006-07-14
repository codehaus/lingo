package org.logicblaze.lingo.example;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * @author Sanjiv Jivan
 * @since Jul 13, 2006
 */
public class ExampleCompositeHeaderMarshaller extends ExampleAcegiTest {

    public void testLocalePropagation() {
        ExampleService service = (ExampleService) getBean("client");

        Locale testLocale = new Locale("zh", "CN");
        LocaleContextHolder.setLocale(testLocale);
        Locale returnedLocale = service.whereAmI();
        assertEquals("Incorrect locale returned", testLocale, returnedLocale);

        testLocale = new Locale("it", "");
        LocaleContextHolder.setLocale(testLocale);
        returnedLocale = service.whereAmI();
        assertEquals("Incorrect locale returned", testLocale, returnedLocale);

    }

    protected String getApplicationContextXml() {
        return "org/logicblaze/lingo/example/spring-with-headermarshallers.xml";
    }
}
