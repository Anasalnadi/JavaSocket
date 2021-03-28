package com.nado.listner;

public interface UserStatusListener {
    public void online(String userOnline);
    public void offline(String userOffline);
}
