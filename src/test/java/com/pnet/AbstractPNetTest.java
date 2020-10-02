package com.pnet;

public class AbstractPNetTest {
    void setUp(){
        Debug.debug=new Debug(true);
        Debug.debug.dontInjectBackupedMessages=true;
    }
}
