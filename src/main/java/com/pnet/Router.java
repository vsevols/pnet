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
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class Router {

    private static final int MAX_COPIES = 3;
    private static final int MAX_MY_MONOLOG_MESSAGES = 5;
    private static final int MAX_MESSAGES_ARCHIVE_SIZE = 500;
    public static final int USER_CACHE_EXPIRED_MINUTES = 10;
    public static final int MAX_MINUTES_RECENT_SEEN = USER_CACHE_EXPIRED_MINUTES*2;
    private Telega telega;
    private LocalDateTime lastMessageMoment = LocalDateTime.now().minusDays(1);
    private Config config;

    public void Init() throws CantLoadLibrary, IOException {
        load();
        telega = new Telega();
        telega.init();
        telega.onMessage = msg -> incomingMessage(msg);
    }


    public void run() {
        while(true){
            telega.process(20000);
            processIncomingMessages();
            //checkProcessStartingMessage();
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
        processMessage(new RoutingMessage(
                new MessageImpl(0, true, 0, 0,  "Здрасьте"), true));
    }

    private void incomingMessage(Message msg) {
        //Отфильтруем конференции
        if(msg.getSenderUserId()!=msg.getChatId())
            return;

        try {
            config.incomingMessages.add(RoutingMessage.fromMessage(msg));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        save();
    }

    private void processMessage(RoutingMessage msg) {
        Victim victim = config.victims.getOrDefault(msg.getSenderUserId(), null);
        //TODO: (?) Добавлять новые входящие контакты !кроме контакта "Telegram"
        //UPD: Возможен спам. Лучше складывать в отдельную коллекцию для ручного аппрува

        if (null!=victim){
            config.victims.moveToFirst(victim.id);
        }

        for (int i = 0; i < config.victims.size(); i++) {
            victim=config.victims.get(i);
            if(victimProcess(victim, msg)){
                incomingMessageArchivate(msg);
                return;
        }

        }
        if(!Debug.debug.dontAddVictims&&addMoreVictims())
            processMessage(msg);
    }

    private void incomingMessageArchivate(RoutingMessage msg) {
        int i = config.incomingMessages.indexOf(msg);
        if(i<0)
            throw new NoSuchElementException();
        config.incomingMessagesArchive.add(msg);
        config.incomingMessages.remove(msg);

        while(config.incomingMessagesArchive.size()>MAX_MESSAGES_ARCHIVE_SIZE)
            config.incomingMessagesArchive.remove(0);

        save();
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
        config= ConfigService.loadConfig();
    }

    private void save() {
        ConfigService.saveConfig(config);
    }

    private boolean victimProcess(Victim victim, RoutingMessage msg) {
        if(victim.id==msg.getSenderUserId())
            return false;
        if(checkArchivate(victim))
            return false;
        if(!isVictimSuitable(victim))
            return false;

        try {
            msg.setReproducedCount(msg.getReproducedCount()+1);
            Logger.getGlobal().severe(msg.toString());
            telega.sendMessage(victim.id, msg.getText());
        } catch (TdApiException e) {
            e.printStackTrace();
        }

        if(msg.getReproducedCount()>=MAX_COPIES){
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
            lastSeen = telega.getUserLastSeen(victim.id, victim.groupName, USER_CACHE_EXPIRED_MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return lastSeen.isAfter(LocalDateTime.now().minusMinutes(MAX_MINUTES_RECENT_SEEN));
    }

    private boolean checkArchivate(Victim victim) {
        //TODO: здесь, возможно прийдётся подменять victims, или, лучше: изменить цикл на i-deletedOffset
        return false;
    }
}
