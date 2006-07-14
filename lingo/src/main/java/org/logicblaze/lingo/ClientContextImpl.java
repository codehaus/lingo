package org.logicblaze.lingo;

/**
 * Default implmentation of {@link ClientContext}. Used by {@link ClientContextHolder}.
 *
 * @author Sanjiv Jivan
 * @since 1.5
 */
public class ClientContextImpl implements ClientContext {

    private String userName;

    public ClientContextImpl() {
    }

    public ClientContextImpl(String userName) {
        this.userName = userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}