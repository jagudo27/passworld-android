package mobile.passworld.utils;

import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeSyncManager {

    private static Duration timeOffset = Duration.ZERO;
    private static final String NTP_SERVER = "time.google.com";

    /**
     * Sincroniza el reloj del sistema con un servidor NTP. Calcula el offset.
     * Llama a esto en un hilo secundario (Thread o Executor).
     */
    public static void syncTimeWithUtcServer() {
        try {
            Instant serverUtcTime = getNtpUtcTime(NTP_SERVER);
            if (serverUtcTime != null) {
                Instant systemUtcTime = Instant.now();
                timeOffset = Duration.between(systemUtcTime, serverUtcTime);

                Log.i("TimeSync", "Hora sistema UTC:  " + systemUtcTime);
                Log.i("TimeSync", "Hora servidor UTC: " + serverUtcTime);
                Log.i("TimeSync", "Offset calculado:  " + timeOffset.getSeconds() + " segundos");
            }
        } catch (Exception e) {
            Log.w("TimeSync", "Error sincronizando NTP: " + e.getMessage());
        }
    }

    private static Instant getNtpUtcTime(String server) throws Exception {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(3000);
        try {
            InetAddress address = InetAddress.getByName(server);
            TimeInfo timeInfo = client.getTime(address);
            timeInfo.computeDetails();

            long serverTimeMillis = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            return Instant.ofEpochMilli(serverTimeMillis);
        } finally {
            client.close();
        }
    }

    /**
     * Devuelve la hora UTC corregida con offset calculado.
     */
    public static Instant nowUtcCorrected() {
        return Instant.now().plus(timeOffset);
    }

    public static String nowUtcCorrectedString() {
        Instant correctedInstant = nowUtcCorrected();
        return formatInstantUtcWithZ(correctedInstant);
    }

    public static String formatInstantUtcWithZ(Instant instant) {
        OffsetDateTime odt = instant.atOffset(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        return odt.format(formatter);
    }

    /**
     * Convierte una hora local a UTC corregido.
     */

    public static LocalDateTime parseUtcStringToLocalDateTime(String utcString) {
        // Si el string termina con Z, usamos el método antiguo
        if (utcString != null && utcString.endsWith("Z")) {
            return OffsetDateTime.parse(utcString).toLocalDateTime();
        } else {
            // Para el nuevo formato sin Z, interpretamos como LocalDateTime directamente
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            return LocalDateTime.parse(utcString, formatter);
        }
    }

    /**
     * Devuelve el offset en segundos entre el sistema y el servidor NTP.
     */
    public static Duration getOffset() {
        return timeOffset;
    }
}
