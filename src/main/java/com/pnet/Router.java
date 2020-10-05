package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.abstractions.User;
import com.pnet.routing.MessageImpl;
import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class Router {

    private int getMaxReproduceCount(){
      return Math.round(3/config.incomingMessages.size());
    };
    private static final int MAX_MY_MONOLOG_MESSAGES = 5;
    private static final int MAX_MESSAGES_ARCHIVE_SIZE = 500;
    public static final int USER_CACHE_EXPIRED_MINUTES = 10;
    public static final int MAX_MINUTES_RECENT_SEEN = USER_CACHE_EXPIRED_MINUTES*2;
    private Telega telega;
    private Config config;
    private PublicationService publication;
    private VictimService victimService;

    public void Init() throws Exception {
        load();
        telega = new Telega();
        telega.init();
        telega.onMessage = msg -> messageRegister(msg);
        victimService=new VictimService(telega);
        publication=new PublicationService(telega, victimService,
                Debug.debug.isTesting?
                        telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK):
                        telega.checkChatInviteLink(Config.OBSERVERS_CHAT_INVITELINK));
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
        while(config.incomingMessages.size()>0){ //TODO: проверка isStopped()
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

        //Не удаляем сообщение из бэкап-очереди, т.к. тесты не добавляют его в основной конфиг
        return !Debug.debug.isTesting;
    }

    private void logInfo(String message){
        Logger.getGlobal().info(message);
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
            boolean userRegularNotScam = false;
            try {
                userRegularNotScam = isUserRegularNotScam(victim);
                victim.isRegularNotScam=userRegularNotScam;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(userRegularNotScam) {
                config.victims.moveToFirst(victim.id);
                config.lastIncomingMessageMoment = LocalDateTime.now();
            }else{
                logInfo(String.format("Deleting victim:\n%s\nthat said:\n%s",
                        victimPrintInfo(victim), msg));
                config.victims.remove(victim);
                config.incomingMessages.remove(msg);
            }
            save();

            if(userRegularNotScam) {
                try {
                    publication.publish(msg, config.victims);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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

    private String victimPrintInfo(Victim victim) {
        User user=null;
        try {
            user=telega.tryObtainUser(victim.id, victim.groupName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("label: %s\nvictim:\n%s\nuser:\n%s", victimService.getLabel(victim), victim, user);
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

        if(0 == getMaxReproduceCount()){
            publication.publishReproduced(msg, config.victims, config.incomingMessages.size());
            return true;
        }

        victimSetUserIdByPhone(victim);

        try {
            if(!isVictimSuitable(victim, msg))
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {//TODO: print label: senderUserId
            logInfo(String.format("Reproducing message:\n%s\nto:\n%s",
                    msg, victimPrintInfo(victim)));
            if(!Debug.debug.dontReallyReproduceMessages)
                telega.sendMessage(victim.id, msg.getText());
            msg.setReproducedCount(msg.getReproducedCount()+1);
            msg.reproducedTo.add(victim.id);
            save();
        } catch (TdApiException e) {
            e.printStackTrace();
        }

        if(msg.getReproducedCount() >= getMaxReproduceCount()){
            publication.publishReproduced(msg, config.victims, config.incomingMessages.size());
            return true;
        }
        return false;
    }

    private void victimSetUserIdByPhone(Victim victim) {
        if(victim.phone.equals(""))
            return;

        try {
            Victim victim1 = new Victim(telega.userIdByPhone(victim.phone), victim.groupName);
            config.victims.remove(victim);
            config.victims.add(victim1);
        } catch (TdApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isVictimSuitable(Victim victim, RoutingMessage msg) throws Exception {
        if(isMe(victim))
            return false;
        if(!isUserRegularNotScam(victim))
            return false;
        if(!isRecentLastSeen(victim))
            return false;
        if(!noResponseTimeout(victim, msg))
            return false;

        return true;
    }

    private boolean isUserRegularNotScam(Victim victim) throws Exception {
        if(victim.isRegularNotScam)
            return true;

        return telega.isUserRegularNotScam(victim.getId(), victim.getGroupName());
    }

    private boolean isMe(Victim victim) throws Exception {
        return telega.getMe()==victim.getId();
    }

    private boolean noResponseTimeout(Victim victim, RoutingMessage msg) {
        List<Message> chatHistory = telega.getChatHistory(victim.id, 0, 0, MAX_MY_MONOLOG_MESSAGES);

        if(msg.isGreeting()&&chatHistory.size()>0)
            return false;

        int outgoingCount=0;
        LocalDateTime lastOutgoingMoment=LocalDateTime.MIN;
        for (Message message:
             chatHistory) {
            if(!message.isOutgoing())
                break;

            outgoingCount++;
            if(lastOutgoingMoment.isBefore(message.getLocalDateTime()))
                    lastOutgoingMoment=message.getLocalDateTime();
        }

        if(0==outgoingCount) {
            return true;
        }

        //1->9
        //2->90
        //3->900
        // ...
        int timeoutMinutes=(int) Math.pow(10, outgoingCount-1)*9;

        return LocalDateTime.now().minusMinutes(timeoutMinutes).isAfter(lastOutgoingMoment);
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
