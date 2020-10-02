package com.pnet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Debug {
    public final boolean isTesting;
    public static Debug debug=new Debug(false);
    public boolean dontAddVictims;
    public boolean dontGenerateStartingMessages;
    public boolean dontReallyReproduceMessages;
    public boolean noGreetingMessageTimeout;
    public boolean dontInjectBackupedMessages;
}
