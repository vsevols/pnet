package Exceptions;

import ua.Storm.Debug;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StormThrowable extends Exception{
    private Object throwContext;

    public StormThrowable(Throwable cause) {
        super(cause);
    }

    public StormThrowable(String message) {
        super(message);
    }

    public StormThrowable(Throwable cause, Object throwContext) {
        super(cause);
        this.throwContext=throwContext;
    }

    /**
     * Оборачивает checked-исключения в StormException
     * http://www.quizful.net/post/java-exceptions
     * @param callback
     * @param <T>
     * @return
     * @throws StormException
     */
    public static <T> T WrapExceptions(WrapExceptionCallback<T> callback) throws StormException {
        try {
            return callback.func();
        }catch (RuntimeException e) {
            throw e;
        }catch (Exception e) {
            throw new StormException(e);
        } //Остальные (Throwable) пропускаются без обработки
    }

    public static void WrapExceptions(WrapExceptionVoidCallback callback) throws StormException{
        ua.Storm.Exceptions.StormThrowable.WrapExceptions(()->{callback.func();return null;});
    }

    public static void UnsupportedOperation(Object throwContext) {
        if(Debug.throwOnUnsupportedOperation)
            throw new UnsupportedOperationException(new ua.Storm.Exceptions.StormThrowable(null, throwContext));

        new ua.Storm.Exceptions.StormThrowable(
                new UnsupportedOperationException(new ua.Storm.Exceptions.StormThrowable(null, throwContext))
        ).logWarn("");
    }

    public static void setLogLevel(Level level) {
        Logger log = Logger.getGlobal();
        log.setLevel(level);
        for(Handler h : log.getParent().getHandlers()){
            if(h instanceof ConsoleHandler) {
                h.setLevel(level);
            }
        }
        log.finest("testFinest");
        log.severe("testSevere");
    }

    private static String formatMsg(Object caughtContext) {
        String result="";
        if(null!=caughtContext)
        result = "Context: " + caughtContext.toString();

        return result;
    }

    private static <T> boolean causeExists(Throwable throwable, Class<T> causeClass)
    {
        return null != throwable && (throwable.getClass().isInstance(causeClass) || ua.Storm.Exceptions.StormThrowable.causeExists(throwable.getCause(), causeClass));
    }

    public void logDebug(Object caughtContext) {
        log(Level.FINE, formatMsg(caughtContext));
    }

    public void logInfo(Object caughtContext) {
        log(Level.INFO, formatMsg(caughtContext));
    }

    public void logError(Object caughtContext) {
        log(Level.SEVERE, formatMsg(caughtContext));
    }

    public void logWarn(Object caughtContext) {
        log(Level.WARNING, formatMsg(caughtContext));
    }

    /**
     * Пишет в лог
     * @return  Всегда возвращает false
     */
    public boolean log() {
        return log(Level.WARNING);
    }

    /**
     * Пишет в лог
     * @return  Всегда возвращает false
     * @param level
     */
    public boolean log(Level level) {
        return log(level, "");
    }

    /**
     * Пишет в лог
     * @return  Всегда возвращает false
     * @param level
     * @param msg
     */
    public boolean log(Level level, String msg)
    {
        return log(level, msg, this);
    }

    public static boolean log(Level level, String msg, Throwable e) {
        //TODO: ?Заюзать SLF4J
        Logger.getGlobal().log(level, msg, e);
        return false;
    }

    public static boolean logFinest(String msg, Object context) {
        return log(Level.FINEST, formatMsg(context, msg), null);
    }

    private static String formatMsg(Object context, String msg) {
        return msg+"\n"+formatMsg(context);
    }

    public <T> boolean causeExists(Class<T> causeClass)
    {
        return ua.Storm.Exceptions.StormThrowable.causeExists(this, causeClass);
    }

    public static interface WrapExceptionCallback<T>
    {
        public T func() throws Exception;
    }

    public static interface WrapExceptionVoidCallback
    {
        public void func() throws Exception;
    }
}
