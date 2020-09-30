package com.pnet;

import com.pnet.abstractions.Chat;
import com.pnet.abstractions.Message;
import com.pnet.abstractions.User;
import com.pnet.telega.*;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.*;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;


/**TODO: onMessage в конструтор, вызывать processUpdates() из main
 *
 */
public class Telega {
    private static final int SYNC_TIMEOUT_MS = 1000;
    public OnMessageHandler onMessage;
    private static TelegaClient client;
    private static boolean haveAuthorization;
    private TdApi.AuthorizationState authorizationState = null;
    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization = authorizationLock.newCondition();

    private static final ConcurrentMap<Integer, CachedUser> users = new ConcurrentHashMap<Integer, CachedUser>();
    private static final ConcurrentMap<Integer, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Integer, TdApi.BasicGroup>();
    private static final ConcurrentMap<Integer, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Integer, TdApi.Supergroup>();
    private static final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;

    private static final ConcurrentMap<Integer, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Integer, TdApi.UserFullInfo>();
    private static final ConcurrentMap<Integer, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Integer, TdApi.BasicGroupFullInfo>();
    private static final ConcurrentMap<Integer, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Integer, TdApi.SupergroupFullInfo>();


    private volatile String currentPrompt = null;
    private volatile boolean quiting = false;
    private static final String newLine = System.getProperty("line.separator");
    private final int WAIT_FOR_UPDATE_INTERVAL_MS = 1000;

    public void init() throws CantLoadLibrary {

        // Initialize TDLight native libraries
        Init.start();

        // Set TDLib log level
        Log.setVerbosityLevel(1);

        // Uncomment this line to print TDLib logs to a file
        // Log.setFilePath("logs" + File.separatorChar + "tdlib.log");

        client = TelegaClient.getClient(client, object -> Telega.this.processResponse(object));

        // Now you can use the client

        while (!haveAuthorization) {
            client.processUpdates(1000);
        }

    }

    private void createPrivateChat(int userId) throws TdApiException {
        client.send(new TdApi.CreatePrivateChat(userId, true));
        while(!chats.containsKey(new Long(userId))) {
            client.processUpdates(WAIT_FOR_UPDATE_INTERVAL_MS);
        }
    }


    int userIdByPhone(String phone) throws TdApiException {

        final int[] result = new int[1];
        TdApi.Contact[] contacts = new TdApi.Contact[]{
                new TdApi.Contact(phone, "", "", null, 0)
        };
            client.send(new TdApi.ImportContacts(contacts), new ReceiveHandler() {
                @Override
                public boolean onResult(TdApi.Object object) {
                    switch (object.getConstructor()) {
                        case TdApi.ImportedContacts.CONSTRUCTOR:
                            TdApi.ImportedContacts contacts1 = (TdApi.ImportedContacts) object;
                            if (1 != contacts1.userIds.length)
                                throw new NoSuchElementException(phone);
                            result[0] = contacts1.userIds[0];
                            return true;
                    }
                    return false;
                }
            });
            return result[0];

        /*
        TdApi.ChatListMain chatListMain = new TdApi.ChatListMain();
        client.send(new TdApi.GetChats(chatListMain, 0, 0, Integer.MAX_VALUE));
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */
    }

    private int importContactByUserId(int userId) throws TdApiException {

        final int[] result = new int[1];
        TdApi.Contact[] contacts = new TdApi.Contact[]{
                new TdApi.Contact(null, "", "", null, userId)
        };
        client.send(new TdApi.ImportContacts(contacts), new ReceiveHandler() {
            @Override
            public boolean onResult(TdApi.Object object) {
                switch (object.getConstructor()) {
                    case TdApi.ImportedContacts.CONSTRUCTOR:
                        TdApi.ImportedContacts contacts1 = (TdApi.ImportedContacts) object;
                        if (1 != contacts1.userIds.length)
                            throw new NoSuchElementException(new Integer(userId).toString());
                        result[0] = contacts1.userIds[0];
                        return true;
                }
                return false;
            }
        });
        return result[0];

        /*
        TdApi.ChatListMain chatListMain = new TdApi.ChatListMain();
        client.send(new TdApi.GetChats(chatListMain, 0, 0, Integer.MAX_VALUE));
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */
    }

    private boolean processResponse(TdApi.Object object) throws TdApiException {
        switch (object.getConstructor()){
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                try {
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                break;
            case TdApi.User.CONSTRUCTOR:
                TdApi.User user = (TdApi.User)object;
                cacheUser(CachedUser.fromUser(user));
                break;
            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                cacheUser(CachedUser.fromUser(updateUser.user));
                break;
            case TdApi.UpdateUserStatus.CONSTRUCTOR:  {
                TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                user = users.get(updateUserStatus.userId);
                if(null==user){
                    cacheUser(new CachedUser(
                            updateUserStatus.userId, LocalDateTime.MIN));
                }
                synchronized (user) {
                    user.status = updateUserStatus.status;
                }
                break;
            }
            case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                break;
            case TdApi.UpdateSupergroup.CONSTRUCTOR:
                TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                break;
            case TdApi.UpdateSecretChat.CONSTRUCTOR:
                TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                break;
            case TdApi.Chat.CONSTRUCTOR:{
                TdApi.Chat chat = (TdApi.Chat) object;
                chats.put(chat.id, chat);
                break;
            }
            case TdApi.UpdateNewChat.CONSTRUCTOR: {
                TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                WrappedChat chat = new WrappedChat(updateNewChat.chat);
                synchronized (chat) {
                    chats.put(chat.id, chat);

                    long order = chat.order;
                    chat.order = 0;
                    setChatOrder(chat, order);
                }
                break;
            }
            case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.title = updateChat.title;
                }
                break;
            }
            case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.photo = updateChat.photo;
                }
                break;
            }/*
            case TdApi.UpdateChatChatList.CONSTRUCTOR: {
                TdApi.UpdateChatChatList updateChat = (TdApi.UpdateChatChatList) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (mainChatList) { // to not change Chat.chatList while mainChatList is locked
                    synchronized (chat) {
                        assert chat.order == 0; // guaranteed by TDLib
                        chat.chatList = updateChat.chatList;
                    }
                }
                break;
            }*/
            case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.lastMessage = updateChat.lastMessage;
                    //setChatOrder(chat, updateChat.order);
                    setChatOrder(new WrappedChat(chat), 0);
                }
                break;
            }/*
            case TdApi.UpdateChatOrder.CONSTRUCTOR: {
                TdApi.UpdateChatOrder updateChat = (TdApi.UpdateChatOrder) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    setChatOrder(chat, updateChat.order);
                }
                break;
            }
            case TdApi.UpdateChatIsPinned.CONSTRUCTOR: {
                TdApi.UpdateChatIsPinned updateChat = (TdApi.UpdateChatIsPinned) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.isPinned = updateChat.isPinned;
                    setChatOrder(chat, updateChat.order);
                }
                break;
            }
            */
            case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                if(null==chat)
                    break;
                synchronized (chat) {
                    chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                    chat.unreadCount = updateChat.unreadCount;
                }
                break;
            }
            case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                if(null==chat)break;

                synchronized (chat) {
                    chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                }
                break;
            }
            case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.unreadMentionCount = updateChat.unreadMentionCount;
                }
                break;
            }
            case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.unreadMentionCount = updateChat.unreadMentionCount;
                }
                break;
            }
            case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                }
                break;
            }
            case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.draftMessage = updateChat.draftMessage;
                    //setChatOrder(chat, updateChat.order);
                    setChatOrder(new WrappedChat(chat), 0);
                }
                break;
            }
            case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                TdApi.Chat chat = chats.get(update.chatId);
                if(null==chat)
                    return false;
                synchronized (chat) {
                    chat.notificationSettings = update.notificationSettings;
                }
                break;
            }
            case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                TdApi.Chat chat = chats.get(update.chatId);
                synchronized (chat) {
                    chat.defaultDisableNotification = update.defaultDisableNotification;
                }
                break;
            }
            case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                TdApi.Chat chat = chats.get(update.chatId);
                synchronized (chat) {
                    chat.isMarkedAsUnread = update.isMarkedAsUnread;
                }
                break;
            }
            /*
            case TdApi.UpdateChatIsSponsored.CONSTRUCTOR: {
                TdApi.UpdateChatIsSponsored updateChat = (TdApi.UpdateChatIsSponsored) object;
                TdApi.Chat chat = chats.get(updateChat.chatId);
                synchronized (chat) {
                    chat.isSponsored = updateChat.isSponsored;
                    setChatOrder(chat, updateChat.order);
                }
                break;
            }

             */

            case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                break;
            case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                break;
            case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                break;
            case TdApi.Chats.CONSTRUCTOR:
                long[] chatIds = ((TdApi.Chats) object).chatIds;
                print(object.toString());
                break;
            case TdApi.Message.CONSTRUCTOR:
                onMessage.onMessage(new MessageImpl((TdApi.Message) object));
                break;
            case TdApi.UpdateNewMessage.CONSTRUCTOR:
                onMessage.onMessage(new MessageImpl(((TdApi.UpdateNewMessage)object).message));
                break;
                /*
            case TdApi.Error.CONSTRUCTOR:
                print("TdApi error: "+((TdApi.Error)object).message);
                break;*/
            default:
                new ErrorProcess(object);
                print("unhandled response: "+object.toString());
                return false;
        }
        return true;
    }

    private void cacheUser(CachedUser user) {
        //if(users.containsKey(user.id))
          //  users.remove(user.id);
        users.put(user.id, user);
    }

    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) throws TimeoutException, TdApiException {
        if (authorizationState != null) {
            this.authorizationState = authorizationState;
        }
        switch (authorizationState.getConstructor()){
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:{
                //client.send(new TdApi.AddProxy(
                //        "iplc-hk1.crhnode.top", 540, true,
                //        new TdApi.ProxyTypeMtproto("ddf5c322081360e73b40db06ea6539f7f2")));
                //client.send(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test"));


                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = "tdlib";
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.apiId = 94575;
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Desktop";
                parameters.systemVersion = "Unknown";
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;
                client.send(new TdApi.SetTdlibParameters(parameters));
                break;
            }
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                String phoneNumber = promptString("Please enter phone number: ");
                client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                String code = promptString("Please enter authentication code: ");
                client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                String firstName = promptString("Please enter your first name: ");
                String lastName = promptString("Please enter your last name: ");
                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                String password = promptString("Please enter password: ");
                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                print("Closed");
                if (!quiting) {
                    client = TelegaClient.getClient(client, object -> Telega.this.processResponse(object)); // recreate client after previous has closed
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + authorizationState);
        }

    }

    private String promptString(String prompt) {
        System.out.println(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPrompt = null;
        return str;
    }

    private void print(String str) {
        if (currentPrompt != null) {
            System.out.println("");
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }

    private void internalSendMessage(long chatId, String message) {
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});

        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        client.send(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content));
    }

    public void sendMessage(String phone, String message) throws TdApiException {
        int id = userIdByPhone(phone);
        sendMessage(id, message);
    }

    public void sendMessage(int userId, String message) throws TdApiException {
        createPrivateChat(userId);
        internalSendMessage(userId, message);
    }

    public boolean process(int milliSeconds) {
        return client.processUpdates(milliSeconds);
    }

    public LocalDateTime getUserLastSeen(int id, String superGroupName, int cacheExpiredMins) throws Exception {
        CachedUser user = discoverUser(id, superGroupName, cacheExpiredMins);
        if (null==user)
            return LocalDateTime.MIN;

        return user.getLastSeen();
    }

    private CachedUser discoverUser(int id, String superGroupName, int cacheExpiredMins) throws Exception {
        if(null==users.get(id)){
            if(!"".equals(superGroupName))
                getSupergroupMembers(superGroupName);
            else {
                try {
                    getUser(id);
                } catch (TdApiException e) {
                    throw new Exception(e);
                }
            }
        }else if(users.get(id).isExpired(cacheExpiredMins)){
            users.remove(users.get(id).id);
            try {
                getUser(id);
            } catch (TdApiException e) {
                throw new Exception(e);
            }
            return discoverUser(id, superGroupName);
        }


        while((null==users.get(id))&&process(SYNC_TIMEOUT_MS));
        return users.get(id);
    }

    private CachedUser discoverUser(int id, String superGroupName) throws Exception {
        return discoverUser(id, superGroupName, Integer.MAX_VALUE);
    }

    private TdApi.User getUser(int id) throws TdApiException {
        TdApi.User user=users.get(new Integer(id));
        if (null!=user)
            return user;
        user = client.syncRequest(new TdApi.GetUser(id), new TdApi.User());
        cacheUser(CachedUser.fromUser(user));
        return user;
    }

    public long searchPublicChat(String name){
        final long[] id = new long[1];
        try {
            client.send(new TdApi.SearchPublicChat(name), new ReceiveHandler() {
                @Override
                public boolean onResult(TdApi.Object object) throws TdApiException {
                    switch (object.getConstructor()){
                        case TdApi.Chat.CONSTRUCTOR:
                            TdApi.Chat chat=(TdApi.Chat)object;
                            if(TdApi.ChatTypeSupergroup.CONSTRUCTOR==chat.type.getConstructor()){
                                id[0]=((TdApi.ChatTypeSupergroup)chat.type).supergroupId;
                                return true;
                            }
                            id[0] =chat.id;
                            return true;
                    }
                    new ErrorProcess(object);
                    return false;
                }
            });
        } catch (TdApiException e) {
            e.printStackTrace();
        }
        return id[0];
    }

    public List<Integer> getSupergroupMembers(String name) throws Exception {
        int id = Math.toIntExact(searchPublicChat(name));
        ArrayList<Integer> result = new ArrayList<>();
        final int LIMIT=200;
        for (int offset = 0; true; offset+=LIMIT) {
            List<Integer> result2= null;
            try {
                result2 = getSupergroupMembers(id, offset, LIMIT);
            } catch (TdApiException e) {
                throw new Exception(e);
            }
            result.addAll(result2);
            if(result2.size()<LIMIT)
                return result;
        }
    }

    private List<Integer> getSupergroupMembers(int id, int offset, int limit) throws TdApiException {
        ArrayList<Integer> result = new ArrayList<>();
        client.send(new TdApi.GetSupergroupMembers(id, null, offset, limit),
                new TypedResultHandler<TdApi.ChatMembers>(new TdApi.ChatMembers()) {
                    @Override
                    public void onTypedResult(TdApi.ChatMembers object) {
                        for (TdApi.ChatMember member : object.members) {
                            result.add(member.userId);
                        }
                    }
                }
        );
        return result;
    }

    public List<Message> getChatHistory(int userId, long fromMessageId, int offset, int limit, boolean userJustQueriedSleep) {
        TdApi.Messages result;
        try {
            createPrivateChat(userId);

            //После получения TdApi.User:
            //При задержке <= 200 следующий вызов возвращает пустой массив
            //При задержке 400 иногда тоже
            if(userJustQueriedSleep) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }

            result = client.syncRequest(
                    new TdApi.GetChatHistory(userId, fromMessageId, offset, limit, true), new TdApi.Messages());
        } catch (TdApiException e) {
            e.printStackTrace();
            return new ArrayList<Message>();
        }

        return new ArrayList<Message>(){{
            for (int i = 0; i < result.messages.length; i++) {
                add(new MessageImpl(result.messages[i]));
            }
        }};
    }

    public Chat tryChatByUserId(int id) {
        if(chats.containsKey(id))
            return new ChatImpl(chats.get(id));
        return null;
    }

    public int getMe() throws Exception {
        final int[] id = new int[1];
        try {
            client.send(new TdApi.GetMe(), new ReceiveHandler() {
                @Override
                public boolean onResult(TdApi.Object object) throws TdApiException {
                    switch (object.getConstructor()){
                        case TdApi.UpdateUser.CONSTRUCTOR:{
                            id[0] =((TdApi.UpdateUser)object).user.id;
                            return true;
                        }
                        case TdApi.User.CONSTRUCTOR:{
                            id[0] =((TdApi.User)object).id;
                            return true;
                        }
                    }
                    return false;
                }
            });
        } catch (TdApiException e) {
            throw new Exception(e);
        }
        return id[0];
    }

    public void setUserLastSeen(int id, LocalDateTime lastSeenNotBefore) {
        CachedUser cachedUser = users.get(id);
        if(null==cachedUser){
            cacheUser(new CachedUser(id, lastSeenNotBefore));
        }else {
            cacheUser(CachedUser.fromUser(users.remove(id), lastSeenNotBefore));
        }
    }

    public boolean isUserRegularNotScam(int id, String superGroupName) throws Exception {
        TdApi.User user = discoverUser(id, superGroupName);
        return (user.type.getConstructor()==TdApi.UserTypeRegular.CONSTRUCTOR)
                &&!user.isScam;
    }

    public User getUserInterface(int id, String superGroupName) throws Exception {
        TdApi.User user = discoverUser(id, superGroupName);
        return new UserImpl(user);
    }

    private class AuthorizationRequestHandler implements ReceiveHandler {
        @Override
        public boolean onResult(TdApi.Object object) {
            return false;
        }
    }

    private static class OrderedChat implements Comparable<OrderedChat> {
        final long order;
        final long chatId;

        OrderedChat(long order, long chatId) {
            this.order = order;
            this.chatId = chatId;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.order != o.order) {
                return o.order < this.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.order == o.order && this.chatId == o.chatId;
        }
    }

    private static void setChatOrder(WrappedChat chat, long order) {
        synchronized (mainChatList) {
            synchronized (chat) {
                /*
                if (chat.chatList == null || chat.chatList.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                    return;
                }
                 */

                if (chat.order != 0) {
                    boolean isRemoved = mainChatList.remove(new OrderedChat(chat.order, chat.id));
                    assert isRemoved;
                }

                chat.order = order;

                if (chat.order != 0) {
                    boolean isAdded = mainChatList.add(new OrderedChat(chat.order, chat.id));
                    assert isAdded;
                }
            }
        }

    }
}
