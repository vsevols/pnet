package com.pnet;

public class AbstractPNetTest {
    void setUp(){
        Debug.debug=new Debug();
        Debug.debug.dontInjectBackupedMessages=true;
    }
}
