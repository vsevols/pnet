package com.pnet;

import com.pnet.secure.Config;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

    private static final int MAX_VICTIM_ID = 1266000000;
    private static final int MAX_COPIES = 3;
    private Telega telega;
    private ConcurrentHashMap<Integer, Victim> victims = new ConcurrentHashMap<>();
    private int copiesSent;

    public void Init() throws CantLoadLibrary, IOException {
        load();
        telega = new Telega();
        telega.init();
        telega.onMessage = this::onMessage;
    }


    public void run() {
        while(true)
            telega.process(Long.MAX_VALUE);
    }

    private void onMessage(Message msg) {
        for (Victim victim :
                victims.values()) {
            if(victimProcess(victim, msg))
                return;
        }
        Victim victim;
        do{
            victim=newVictim();
            victims.put(victim.id, victim);
        }while(!victimProcess(victim, msg));
        save();
    }

    private void load() throws IOException {
        victims=new ConfigService().ReadJsonFile(victims.getClass(), getVictimsFilePath());
    }

    private void save() {
        try {
            new ConfigService().WriteJsonFile(getVictimsFilePath(), victims);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private String getVictimsFilePath() {
        return Config.toAbsolutePath("victims.json");
    }

    private Victim newVictim() {
        int id;
        do {
            id = (int) (Math.random() * MAX_VICTIM_ID);
        }while(victims.containsKey(id));
        return new Victim(id);
    }

    private boolean victimProcess(Victim victim, Message msg) {
        if(victim.id==msg.senderUserId)
            return false;
        if(checkArchivate(victim))
            return false;
        if(!isVictimSuitable(victim))
            return false;

        telega.sendMessage(victim.id, msg.text);
            
        if(copiesSent>=MAX_COPIES){
            copiesSent=0;
            return true;
        }
        return false;
    }

    private boolean isVictimSuitable(Victim victim) {
        if(!isRecentLastSeen(victim))
            return false;
        if(!isChatTailFloodedByMe(victim))
            return false;

        return true;
    }

    private boolean isChatTailFloodedByMe(Victim victim) {
        return true;
    }

    private boolean isRecentLastSeen(Victim victim) {
        return false;
    }

    private boolean checkArchivate(Victim victim) {
        return false;
    }
}
