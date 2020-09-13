import Exceptions.StormException;
import org.telegram.api.TLConfig;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcCallback;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.requests.TLRequestHelpGetConfig;

public class Telega {
    void Initialize() throws java.io.IOException {
        TelegramApi api = new TelegramApi(
                new ApiState(),
                new AppInfo(0,"","","",""),
                new ApiCallback() {

        @Override
        public void onAuthCancelled(TelegramApi api) {
            //TODO: implement
            StormException.UnsupportedOperation(this);

        }

        @Override
        public void onUpdatesInvalidated(TelegramApi api) {
            // When api engine expects that update sequence might be broken
        }

        @Override
        public void onUpdate(org.telegram.api.TLAbsUpdates updates) {
            //TODO: implement
            StormException.UnsupportedOperation(this);

        }
    });

// Syncronized call
// All request objects are in org.telegram.api.requests package
        TLConfig config = api.doRpcCall(new TLRequestHelpGetConfig());

// Standart async call
        api.doRpcCall(new TLRequestHelpGetConfig(), new RpcCallback<TLConfig>()
        {
            public void onResult(TLConfig result)
            {

            }

            public void onError(int errorCode, String message)
            {
                // errorCode == 0 if request timeouted
            }
        });
    }
}
