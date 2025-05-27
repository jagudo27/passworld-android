package mobile.passworld.utils;

import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger para Android que usa java.util.logging.Logger pero redirige a Logcat.
 */
public class LogUtils {

    public static final Logger LOGGER = Logger.getLogger("passworldLogger");

    static {
        // Eliminar handlers por defecto (si hay)
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        // Añadir nuestro handler que redirige a android.util.Log
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(new AndroidLogHandler());
    }

    private static class AndroidLogHandler extends Handler {

        private static final String TAG = "Passworld";

        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;

            String message = getFormatter() == null ? record.getMessage() : getFormatter().format(record);
            int priority = mapLevelToPriority(record.getLevel());

            Log.println(priority, TAG, message);
        }

        @Override
        public void flush() {
            // No buffering, nada que hacer
        }

        @Override
        public void close() throws SecurityException {
            // No recursos que liberar
        }

        private int mapLevelToPriority(Level level) {
            int value = level.intValue();
            if (value >= Level.SEVERE.intValue()) {
                return Log.ERROR;
            } else if (value >= Level.WARNING.intValue()) {
                return Log.WARN;
            } else if (value >= Level.INFO.intValue()) {
                return Log.INFO;
            } else if (value >= Level.FINE.intValue()) {
                return Log.DEBUG;
            } else {
                return Log.VERBOSE;
            }
        }
    }
}
