package com.goodtrendltd.HolySongs.chat;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public interface Chat {
    void sendMessage(ChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release() throws XMPPException;
}
