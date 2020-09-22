package com.pnet;

import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.*;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

    private static final int MAX_VICTIM_ID = 1266000000;
    private static final int MAX_COPIES = 3;
    private Telega telega;
    private ConcurrentHashMap<Integer, Victim> victims = new ConcurrentHashMap<>();
    private int copiesSent;
    private LocalDateTime lastMessageMoment = LocalDateTime.now().minusDays(1);

    public void Init() throws CantLoadLibrary, IOException {
        load();
        telega = new Telega();
        telega.init();
        telega.onMessage = this::onMessage;
    }


    public void run() {
        while(true){
            telega.process(200000);
            checkProcessStartingMessage();
        }

    }

    private void checkProcessStartingMessage() {
        if(LocalDateTime.now().minusMinutes(1).isBefore(lastMessageMoment))
            return;
        processMessage(new Message(new PartyId(""), "Здрасьте", 0));
    }

    private void onMessage(Message msg) {
        processMessage(msg);
    }

    private void processMessage(Message msg) {
        //TODO: Добавлять новые входящие контакты !кроме контакта "Telegram"
        for (Victim victim :
                victims.values()) {
            if(victimProcess(victim, msg))
                return;
        }
        Victim victim;
        do{
            victim=newVictim();
            victims.put(victim.id, victim);
            save();
        }while(!victimProcess(victim, msg));
    }

    private void load() throws IOException {
        if(!new File(getVictimsFilePath()).isFile()) {
            if (!"initConfig".equals(promptString(
                    String.format("%s not exists. Type 'initConfig' to create new", getVictimsFilePath()))))
                throw new FileNotFoundException(getVictimsFilePath());
            else return;
        }
        Config config=new ConfigService().ReadJsonFile(Config.class, getVictimsFilePath());
        victims=config.victims;
    }

    private String promptString(String prompt) {
        System.out.print(prompt+System.lineSeparator());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    private void save() {
        Config config=new Config();
        config.victims=victims;
        try {
            new ConfigService().WriteJsonFile(getVictimsFilePath(), config);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private String getVictimsFilePath() {
        return Config.toDataPath("victims.json");
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

        try {
            telega.sendMessage(victim.id, msg.text);
        } catch (TdApiException e) {
            e.printStackTrace();
        }

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
        LocalDateTime lastSeen=telega.getUserLastSeen(victim.id);
        return lastSeen.isAfter(LocalDateTime.now().minusMinutes(10));
    }

    private boolean checkArchivate(Victim victim) {
        return false;
    }
}
