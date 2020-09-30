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
    private Config config;

    public void Init() throws CantLoadLibrary, IOException {
        load();
        telega = new Telega();
        telega.init();
        telega.onMessage = msg -> messageRegister(msg);
    }


    public void run() {
        LocalDateTime startMoment = LocalDateTime.now();

        while(!isStopped()){
            telega.process(20000);
            processIncomingMessages();
            if(startMoment.plusSeconds(Debug.debug.noGreetingMessageTimeout?0:20).isBefore(LocalDateTime.now())
                    &&!Debug.debug.dontGenerateStartingMessages)
                checkGenerateStartingMessage();
        }

    }

    private boolean isStopped() {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        try {
            if(reader.ready())
                return reader.readLine().equals("stop");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return false;
    }

    private void processIncomingMessages() {
        while(config.incomingMessages.size()>0){
            processMessage(config.incomingMessages.get(0));
        }
    }

    private void checkGenerateStartingMessage() {
        if(LocalDateTime.now().minusMinutes(3).isAfter(config.lastIncomingMessageMoment))
            return;
        processMessage(new RoutingMessage(
                new MessageImpl("Здрасьте"), true));
    }

    private boolean messageRegister(Message msg) {
        //Отфильтруем конференции
        if(msg.getSenderUserId()!=msg.getChatId())
            return true;
        if(msg.isOutgoing())
            return true;

        try {
            config.incomingMessages.add(RoutingMessage.fromMessage(msg));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        save();
        return true;
    }

    private void processMessage(RoutingMessage msg) {
        //?Не имеет смысла, т.к. на поступившее сообщение отвечаем в любом случае
        //UPD: не в любом, -по стандартному алгоритму, чтобы не тратить живые сообщения
        //TODO: инициализировать дату кеш-элемента датой сообщения, протестировать
        //setUserLastSeen(msg.getSenderUserId(), msg.getLocalDateTime());

        Victim victim = config.victims.getOrDefault(msg.getSenderUserId(), null);
        //TODO: (?) Добавлять новые входящие контакты !кроме контакта "Telegram"
        //UPD: Возможен спам. Лучше складывать в отдельную коллекцию для ручного аппрува

        //TODO: (?) Переместить в com.pnet.Router.incomingMessage
        if (null!=victim){
            config.victims.moveToFirst(victim.id);
            config.lastIncomingMessageMoment=LocalDateTime.now();
            save();
        }else if (!msg.isGreeting()){
            config.incomingMessages.remove(msg);
            return;
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

    private void setUserLastSeen(int id, LocalDateTime lastSeenNotBefore) {
        telega.setUserLastSeen(id, lastSeenNotBefore);
    }

    private void incomingMessageArchivate(RoutingMessage msg) {
        if(msg.isGreeting())
            return;

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
        //TODO: Для наглядности лога: fillVictimUser(victim)
        if(victim.id==msg.getSenderUserId())
            return false;
        if(checkArchivate(victim))
            return false;
        try {
            if(!isVictimSuitable(victim, msg))
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            Logger.getGlobal().info(String.format("Reproducing message:\n%s \nto:\n %s ", msg, victim));
            if(!Debug.debug.dontReallySendMessages)
                telega.sendMessage(victim.id, msg.getText());
            msg.setReproducedCount(msg.getReproducedCount()+1);
            save();
        } catch (TdApiException e) {
            e.printStackTrace();
        }

        if(msg.getReproducedCount()>=MAX_COPIES){
            return true;
        }
        return false;
    }

    private boolean isVictimSuitable(Victim victim, RoutingMessage msg) throws Exception {
        if(isMe(victim))
            return false;
        if(isUserRegularNotScam(victim))
            return false;
        if(!isRecentLastSeen(victim))
            return false;
        if(!noResponseTimeout(victim, msg))
            return false;

        return true;
    }

    private boolean isUserRegularNotScam(Victim victim) throws Exception {
        return telega.isUserRegularNotScam(victim.getId(), victim.getGroupName());
    }

    private boolean isMe(Victim victim) throws Exception {
        return telega.getMe()==victim.getId();
    }

    private boolean noResponseTimeout(Victim victim, RoutingMessage msg) {
        List<Message> chatHistory = telega.getChatHistory(victim.id, 0, 0, MAX_MY_MONOLOG_MESSAGES, false);

        if(msg.isGreeting()&&chatHistory.size()>0)
            return false;

        int outgoingCount=0;
        LocalDateTime incomingMoment=LocalDateTime.MAX;
        for (Message message:
             chatHistory) {
            if(!message.isOutgoing()) {
                incomingMoment=message.getLocalDateTime();
                break;
            }
            outgoingCount++;
        }
        int timeoutMinutes=outgoingCount*9;

        if(0==outgoingCount)
            return true;

        return incomingMoment.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
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
