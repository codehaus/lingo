package org.logicblaze.lingo;

/**
 * Client context holder. Calling {@link #getUserName()} on the server wll return the invoking clients
 * username if provided or null if not provided.
 *
 * @author Sanjiv Jivan
 * @since 1.5
 */
public class ClientContextHolder {

    private static InheritableThreadLocal contextHolder = new InheritableThreadLocal();

    /**
     * Associates a new ClientContext with the current thread of execution.
     */
    public static void setContext(ClientContext context) {
        contextHolder.set(context);
    }

    /**
     * Obtains the <code>ClientContext</code> associated with the current thread of execution.
     *
     * @return the current ClientContext
     */
    public static ClientContext getContext() {
        if (contextHolder.get() == null) {
            contextHolder.set(new ClientContextImpl());
        }
        return (ClientContext) contextHolder.get();
    }

    public static String getUserName() {
        return ((ClientContext) contextHolder.get()).getUserName();
    }
}
