package org.logicblaze.lingo;

import java.io.Serializable;

/**
 * Client context interface for getting the user name from the invocation client.
 * The client context is stored in a {@link ClientContextHolder}.
 *
 * @author Sanjiv Jivan
 * @since 1.5
 */
public interface ClientContext extends Serializable {

    String getUserName();

}
