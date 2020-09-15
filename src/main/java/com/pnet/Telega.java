package com.pnet;

import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.*;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



/**TODO: onMessage в конструтор, вызывать processUpdates() из main
 *
 */
public class Telega {
    private TelegaClient client;
    private TdApi.AuthorizationState authorizationState = null;
    private boolean haveAuthorization;
    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization = authorizationLock.newCondition();

    private volatile String currentPrompt = null;
    private volatile boolean quiting = false;
    private static final String newLine = System.getProperty("line.separator");

    public void run() throws CantLoadLibrary {

        // Initialize TDLight native libraries
        Init.start();

        // Set TDLib log level
        Log.setVerbosityLevel(1);

        // Uncomment this line to print TDLib logs to a file
        // Log.setFilePath("logs" + File.separatorChar + "tdlib.log");

        client = TelegaClient.create();

        //client.send(new TdApi.AddProxy(
        //        "iplc-hk1.crhnode.top", 540, true,
        //        new TdApi.ProxyTypeMtproto("ddf5c322081360e73b40db06ea6539f7f2")));
            //client.send(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test"));


        // Now you can use the client
        while (true) {
            processUpdates();
        }
    }

    private void processUpdates() {
        Response response =null;
        do {
            response = client.receive(0);
            if(null!=response)
                this.processResponse(response);
        }while(null!=response);
    }

    private void processResponse(Response response) {
        switch (Math.toIntExact(response.getObject().getConstructor())){
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) response.getObject()).authorizationState);
                break;
        }
    }

    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState){
        if (authorizationState != null) {
            this.authorizationState = authorizationState;
        }
        switch (authorizationState.getConstructor()){
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:{
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
                    client = client.create(); // recreate client after previous has closed
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + authorizationState);
        }

    }

    private String promptString(String prompt) {
        System.out.print(prompt);
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

    private void sendMessage(long chatId, String message) {
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});

        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        client.send(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content));
    }

    private class AuthorizationRequestHandler {
    }
}
