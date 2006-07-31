package org.logicblaze.lingo.example;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.logicblaze.lingo.SpringTestSupport;

/**
 * @author Sanjiv Jivan
 * @since Jul 13, 2006
 */
public class ExampleAcegiTest extends SpringTestSupport {

    protected void setUp() throws Exception {
        super.setUp();
        getBean("server");
    }

    public void testClientContextPropagation() throws Exception {

        SecurityContext sc = new SecurityContextImpl();
        sc.setAuthentication(new UsernamePasswordAuthenticationToken("sjivan", "foosball"));
        SecurityContextHolder.setContext(sc);

        ExampleService service = (ExampleService) getBean("client");

        String userName = service.whoAmI();
        assertEquals("Incorrect user returned", "sjivan", userName);

        sc.setAuthentication(new UsernamePasswordAuthenticationToken("lingo", "farfegnugen"));
        SecurityContextHolder.setContext(sc);

        userName = service.whoAmI();
        assertEquals("Incorrect user returned", "lingo", userName);

    }

    protected String getApplicationContextXml() {
        return "org/logicblaze/lingo/example/spring-with-acegi.xml";
    }
}
