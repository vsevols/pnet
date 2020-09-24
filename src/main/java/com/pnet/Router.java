package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.routing.MessageImpl;
import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Router {

    private static final int MAX_COPIES = 3;
    private static final int MAX_MY_MONOLOG_MESSAGES = 5;
    private Telega telega;
    private int copiesSent;
    private LocalDateTime lastMessageMoment = LocalDateTime.now().minusDays(1);
    private Config config;

    public void Init() throws CantLoadLibrary, IOException {
        load();
        telega = new Telega();
        telega.init();
        telega.onMessage = msg -> onMessage(msg);
    }


    public void run() {
        while(true){
            telega.process(20000);
            /*
            processIncomingMessages();
            checkProcessStartingMessage();

             */
        }

    }

    private void processIncomingMessages() {
        while(config.incomingMessages.size()>0){
            processMessage(config.incomingMessages.get(0));
        }
    }

    private void checkProcessStartingMessage() {
        if(LocalDateTime.now().minusMinutes(3).isBefore(lastMessageMoment))
            return;
        processMessage(new MessageImpl("Здрасьте"));
    }

    private void onMessage(Message msg) {
        //Отфильтруем конференции
        if(msg.getSenderUserId()!=msg.getChatId())
            return;

        config.incomingMessages.add(new RoutingMessage(msg, false));
        save();
    }

    private void processMessage(Message msg) {
        //TODO: (?) Добавлять новые входящие контакты !кроме контакта "Telegram"
        //UPD: Возможен спам. Лучше вручную

        copiesSent=0;
        for (Victim victim :
                config.victims.values()) {
            if(victimProcess(victim, msg)){
                if(msg.getId()>=config.lastProcessedMessageId) {
                    config.lastProcessedMessageId = msg.getId();
                    save();
                }
                else throw new RuntimeException(
                        String.format("msg.getId()<config.lastProcessedMessageId %d %d",
                                msg.getId(), config.lastProcessedMessageId));
                return;
            }
        }
        if(addMoreVictims())
            processMessage(msg);
    }

    private boolean addMoreVictims(){
        boolean wasAdded = false;
        for (String superGroupName : Config.superGroupNames) {
            List<Integer> members;
            try {
                members = telega.getSupergroupMembers(superGroupName);
            } catch (Exception e) {
                e.printStackTrace();
                save();
                return wasAdded;
            }
            for (Integer member : members) {
                if(!config.victims.containsKey(member)) {
                    config.victims.put(member, new Victim(member, superGroupName));
                    wasAdded = true;
                }
            }
        }
        save();
        return wasAdded;
    }

    private void load() throws IOException {
        if(!new File(getVictimsFilePath()).isFile()) {
            if (!"initConfig".equals(promptString(
                    String.format("%s not exists. Type 'initConfig' to create new", getVictimsFilePath()))))
                throw new FileNotFoundException(getVictimsFilePath());
            else return;
        }
        config=new ConfigService().ReadJsonFile(Config.class, getVictimsFilePath());
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
        try {
            new ConfigService().WriteJsonFile(getVictimsFilePath(), config);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private String getVictimsFilePath() {
        return Config.toDataPath("victims.json");
    }

    private boolean victimProcess(Victim victim, Message msg) {
        if(victim.id==msg.getSenderUserId())
            return false;
        if(checkArchivate(victim))
            return false;
        if(!isVictimSuitable(victim))
            return false;

        try {
            copiesSent++;
            Logger.getGlobal().severe(msg.toString());
            if(copiesSent>9999)throw new TdApiException(null);
            return true;//Заглушка
            //telega.sendMessage(victim.id, msg.text);
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
        if(isMyMonolog(victim))
            return false;

        return true;
    }

    private boolean isMyMonolog(Victim victim) {
        List<Message> chatHistory = telega.getChatHistory(victim.id, 0, 0, MAX_MY_MONOLOG_MESSAGES);
        int count=0;
        for (Message message:
             chatHistory) {
            if(!message.isOutgoing())
                return false;
            if(MAX_MY_MONOLOG_MESSAGES<=count)
                return true;
            count++;
        }
        return false;
    }

    private boolean isRecentLastSeen(Victim victim) {
        LocalDateTime lastSeen= null;
        try {
            lastSeen = telega.getUserLastSeen(victim.id, victim.groupName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return lastSeen.isAfter(LocalDateTime.now().minusMinutes(10));
    }

    private boolean checkArchivate(Victim victim) {
        //TODO: здесь, возможно прийдётся подменять victims
        return false;
    }
}
