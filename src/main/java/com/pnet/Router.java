package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.abstractions.User;
import com.pnet.routing.MessageImpl;
import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import it.tdlight.tdlib.TdApi;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class Router {


    private boolean isStopped=false;

    private int getMaxReproduceCount(boolean isGreeting){
        final int REPRODUCE_FACTOR = 3;
        if(isGreeting)
            return REPRODUCE_FACTOR;
        return config.incomingMessages.size()>0?
                Math.round(REPRODUCE_FACTOR /config.incomingMessages.size()):REPRODUCE_FACTOR;
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
        telega.onMessage = new OnMessageHandler() {
            @Override
            public boolean onMessage(Message msg) {
                return messageRegister(msg);
            }

            @Override
            public void onMessageSendingFailedFlood(long chatId) {
                MessageSendingFailedFlood(chatId);
            }
        };
        victimService=new VictimService(telega);
        publication=new PublicationService(telega, victimService,
                Debug.debug.isTesting?
                        telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK).chatId:
                        telega.checkChatInviteLink(Config.OBSERVERS_CHAT_INVITELINK).chatId);
    }

    private void MessageSendingFailedFlood(long chatId) {
        Victim victim = config.victims.getByKey(Math.toIntExact(chatId));
        victim.isSendingFailedFlood=true;
        if(victim.tailOutgoingCount>0)
            victim.tailOutgoingCount--;

        save();
        logInfo("MessageSendingFailedFlood:"+victimPrintInfo(victim));
    }


    public void run() {
        LocalDateTime startMoment = LocalDateTime.now();
        boolean isLaunched=false;

        while(!isStopped()){
            telega.process(20000);
            if(!isLaunched) {
                processLaunched();
                isLaunched=true;
                logInfo("Routing launched");
            }
            processIncomingMessages();
            if(startMoment.plusSeconds(Debug.debug.noGreetingMessageTimeout?0:20).isBefore(LocalDateTime.now())
                    &&!Debug.debug.dontGenerateStartingMessages)
                checkGenerateStartNewDialogMessage();
        }
        telega.close();
    }

    private void processLaunched() {
        addMoreVictimsByJoinViaSupergroupLink(Config.OBSERVERS_CHAT_INVITELINK, true);
        victimAddByContactInChatHistoryLink(
                Config.TEST_OUTBOUND_USER_ID_FROM_CHATMSG_CONTACT, Config.TEST_OUTBOUND_CHAT_INVITELINK, true);
        save();
        try {
            telega.getContacts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void victimAddByContactInChatHistoryLink(int userId, String link, boolean toBeginning) {
        try {
            telega.joinChatByInviteLink(link);
        } catch (Exception e) {
        }

        TdApi.ChatInviteLinkInfo chatInviteLinkInfo=null;
        try {
            chatInviteLinkInfo = telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TdApi.Messages chatHistory=null;
        try {
            chatHistory = telega.getChatHistory(chatInviteLinkInfo.chatId, 0, 0, Integer.MAX_VALUE);
        } catch (TdApiException e) {
            e.printStackTrace();
        }

        //TODO: Брать userId из истории
        victimAddifNew(userId,"", toBeginning, true);
    }

    private boolean isStopped() {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        try {
            if(reader.ready()&&reader.readLine().equals("stop"))
                isStopped=true;
            File stopFile = new File(Config.toDataPath("stop"));
            if(stopFile.isFile()) {
                isStopped = true;
                stopFile.delete();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        if(isStopped)
            logInfo("terminating...");

        return isStopped;
    }

    private void processIncomingMessages() {
        while(config.incomingMessages.size()>0&&!isStopped()){ //TODO: проверка isStopped()
            processMessage(config.incomingMessages.get(0));
        }
    }

    private void checkGenerateStartNewDialogMessage() {
        if(!canStartNewDialogTimeoutReached())
            return;
        try {
            processMessage(new RoutingMessage(
                    new MessageImpl("Хеллов"), true));
        }finally {
            config.lastGreetingMessageMoment=LocalDateTime.now();
            save();
        }
    }

    private boolean messageRegister(Message msg) {
        //Отфильтруем конференции
        if(msg.getSenderUserId()!=msg.getChatId())
            return true;
        if(msg.isOutgoing())
            return true;
        if(processAdminMessage(msg))
            return true;

        try {
            config.incomingMessages.add(RoutingMessage.fromMessage(msg));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Victim victim = config.victims.getOrDefault(msg.getSenderUserId(), null);
        if(null!=victim)
            victim.tailOutgoingCount=0;

        save();

        //Не удаляем сообщение из бэкап-очереди, т.к. тесты не добавляют его в основной конфиг
        return !Debug.debug.isTesting;
    }

    private boolean processAdminMessage(Message msg) {
        //TODO: msg.attachedContact
        return false;
    }

    private void logInfo(String message){
        getLogger().info(message);
    }

    private Logger getLogger() {
        return Logger.getGlobal();
    }

    private void processMessage(RoutingMessage msg) {
        //?Не имеет смысла, т.к. на поступившее сообщение отвечаем в любом случае
        //UPD: не в любом, -по стандартному алгоритму, чтобы не тратить живые сообщения
        //TODO: инициализировать дату кеш-элемента датой сообщения, протестировать
        //setUserLastSeen(msg.getSenderUserId(), msg.getLocalDateTime());

        try{
            logInfo(String.format("msgId %d processing started", msg.getId()));

            Victim victim = config.victims.getOrDefault(msg.getSenderUserId(), null);
            //TODO: (?) Добавлять новые входящие контакты !кроме контакта "Telegram"
            //UPD: Возможен спам. Лучше складывать в отдельную коллекцию для ручного аппрува

            //TODO: (?) Переместить в com.pnet.Router.incomingMessage
            if (null != victim) {
                boolean userRegularNotScam = false;
                try {
                    userRegularNotScam = isUserRegularNotScam(victim);
                    victim.isRegularNotScam = userRegularNotScam;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (userRegularNotScam) {
                    config.victims.moveToFirst(victim.id);
                    config.lastIncomingMessageMoment = LocalDateTime.now();
                } else {
                    logInfo(String.format("Deleting victim:\n%s\nthat said:\n%s",
                            victimPrintInfo(victim), msg));
                    config.victims.remove(victim);
                    config.incomingMessages.remove(msg);
                }
                save();

                if (userRegularNotScam) {
                    try {
                        publication.publish(msg, config.victims);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else if (!msg.isGreeting()) {
                incomingMessageArchivate(msg);
                return;
            }

            for (int i = 0; i < config.victims.size(); i++) {
                victim = config.victims.get(i);
                if (victimProcess(victim, msg)) {
                    incomingMessageArchivate(msg);
                    return;
                }

                if (isStopped())
                    return;
            }
        }finally{
            logInfo(String.format("msgId %d processing finished", msg.getId()));
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
            return;
        config.incomingMessagesArchive.add(msg);
        config.incomingMessages.remove(msg);

        while(config.incomingMessagesArchive.size()>MAX_MESSAGES_ARCHIVE_SIZE)
            config.incomingMessagesArchive.remove(0);

        save();
    }

    private boolean addMoreVictims(){
        boolean wasAdded = false;

        //wasAdded = addMoreVictimsByJoinViaSupergroupLinks(true)||wasAdded;

        for (String superGroupName : Config.superGroupNames) {
            List<Integer> members;
            try {
                members = telega.getSupergroupMembers(superGroupName);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            for (Integer member : members) {
                wasAdded = victimAddifNew(member, superGroupName, false, false)
                        ||wasAdded;
            }
        }
        save();
        return wasAdded;
    }

    private boolean addMoreVictimsByJoinViaSupergroupLinks(boolean toBeginning) {
        boolean wasAdded = false;
        for (String link :
                Config.superGroupLinks) {
            wasAdded = addMoreVictimsByJoinViaSupergroupLink(link, toBeginning)||wasAdded;
        }

        return wasAdded;
    }

    private boolean addMoreVictimsByJoinViaSupergroupLink(String link, boolean toBeginning) {
        boolean wasAdded = false;
        TdApi.Chat chat=null;

        try {
            chat=telega.joinChatByInviteLink(link);
        } catch (Exception e) {
            e.printStackTrace();
            return wasAdded;
        }

        int supergroupId;
        String chatTitle;

        if(null==chat){
            try {
                TdApi.ChatInviteLinkInfo chatInviteLinkInfo = telega.checkChatInviteLink(link);
                TdApi.ChatTypeSupergroup chatTypeSupergroup = (TdApi.ChatTypeSupergroup) chatInviteLinkInfo.type;
                supergroupId= chatTypeSupergroup.supergroupId;
                chatTitle=chatInviteLinkInfo.title;
            } catch (Exception e) {
                e.printStackTrace();
                return wasAdded;
            }
        }else{
            supergroupId=((TdApi.ChatTypeSupergroup)chat.type).supergroupId;
            chatTitle=chat.title;
        }

        //supergroupId=chat.supergroupId;
        //List<Integer> supergroupMembers = telega.getSupergroupMembers(chatInviteLinkInfo.title);
        List<Integer> supergroupMembers = null;
        try {
            supergroupMembers = telega.getSupergroupMembers(supergroupId, 0, 200);
        } catch (TdApiException e) {
            e.printStackTrace();
            return wasAdded;
        }

        for (int member :
                supergroupMembers) {
            wasAdded = victimAddifNew(member, chatTitle, toBeginning, false)||wasAdded;
        }
        return wasAdded;
    }

    private boolean victimAddifNew(int userId, String s, boolean toBeginning, boolean forceStartNewDialog) {
        boolean wasAdded = false;
        Victim victim;
        if (!config.victims.containsKey(userId)&&!config.victimsArchive.containsKey(userId)) {
            victim = new Victim(userId, s, "");
            victim.forceStartNewDialog=forceStartNewDialog;
            config.victims.put(userId, victim, toBeginning);
            wasAdded = true;
        }

        if(forceStartNewDialog&&null!=(victim=config.victims.getOrDefault(userId, null)))
            victim.forceStartNewDialog=forceStartNewDialog;
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

        if(0 == getMaxReproduceCount(msg.isGreeting())){
            publication.publishReproduced(msg, config.victims, config.incomingMessages.size());
            return true;
        }

        victim=victimSetUserIdByPhone(victim);

        try {
            telega.obtainUser(victim.getId(), victim.groupName);
            if(!isVictimSuitable(victim, msg))
                return false;
        } catch (Exception e) {
            getLogger().finest(String.format("victim.id=%d exception: %s", victim.getId(), e.getMessage()));
            return false;
        }

        try {//TODO: print label: senderUserId
            logInfo(String.format("Reproducing message:\n%s\nto:\n%s",
                    msg, victimPrintInfo(victim)));
            if(!Debug.debug.dontReallyReproduceMessages)
                telega.sendMessage(victim.id, msg.getText());
            victim.forceStartNewDialog=false;
            victim.tailOutgoingCount++;
            msg.setReproducedCount(msg.getReproducedCount()+1);
            msg.reproducedTo.add(victim.id);
            save();
        } catch (TdApiException e) {
            e.printStackTrace();
        }

        if(msg.getReproducedCount() >= getMaxReproduceCount(msg.isGreeting())){
            publication.publishReproduced(msg, config.victims, config.incomingMessages.size());
            return true;
        }
        return false;
    }

    private Victim victimSetUserIdByPhone(Victim victim) {
        if(null==victim.phone||victim.phone.equals(""))
            return victim;

        try {
            telega.userIdByPhone(victim.phone);
        } catch (TdApiException e) {
            e.printStackTrace();
        }
        return victim;

        //Дальнейший вызов конструктора не копирует все необходимые поля
        /*

        if(victim.getId()!=0)
            return victim;

        try {
            Victim victim1 = new Victim(telega.userIdByPhone(victim.phone), victim.groupName, victim.phone);
            int i = config.victims.indexOf(victim);
            config.victims.remove(victim);
            config.victims.add(i, victim1);
            return victim1;
        } catch (TdApiException e) {
            e.printStackTrace();
            return victim;
        }

         */
    }

    private boolean isVictimSuitable(Victim victim, RoutingMessage msg) throws Exception {
        if(isMe(victim))
            return false;
        if(isMsgToVictimReproduced(msg, victim))
            return false;

        if(victim.forceStartNewDialog&&!dialogNotEmpty(victim))
            return true;

        if(!isUserRegularNotScam(victim))
            return false;
        if(!isRecentLastSeen(victim))
            return false;
        if(!msg.isGreeting()&&!dialogNotEmpty(victim))
            return false;
        if(!noResponseTimeout(victim, msg))
            return false;

        return true;
    }

    private boolean isMsgToVictimReproduced(RoutingMessage msg, Victim victim) {
        for (Integer userId : msg.reproducedTo) {
            if(userId==victim.getId())
                return true;
        }

        return false;
    }

    private boolean dialogNotEmpty(Victim victim) {
        return telega.getUserChatHistory(victim.id, 0, 0, 1).size()>0;
    }

    private boolean canStartNewDialogTimeoutReached() {
        return config.lastGreetingMessageMoment.isBefore(LocalDateTime.now().minusMinutes(180));
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
        List<Message> chatHistory = telega.getUserChatHistory(victim.id, 0, 0, MAX_MY_MONOLOG_MESSAGES);

        if(msg.isGreeting()&&(chatHistory.size()>0||victim.tailOutgoingCount>0))
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

        //Особенности реализации выдачи истории Телеги
        if(outgoingCount==chatHistory.size()){
            if(victim.tailOutgoingCount<outgoingCount)
                victim.tailOutgoingCount=outgoingCount;
            outgoingCount=victim.tailOutgoingCount;
        }else
            victim.tailOutgoingCount=outgoingCount;

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
        if(victim.isSendingFailedFlood) {
            /*
            config.victimsArchive.put(victim.id, victim);
            config.victims.remove(victim);
            save();
            return true;

             */
        }
        return false;
    }
}
