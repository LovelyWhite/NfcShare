package nfc.share.nfcshare;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;

import java.util.concurrent.ArrayBlockingQueue;

import nfc.share.nfcshare.service.EmulationService;
import nfc.share.nfcshare.service.MqttService;
import nfc.share.nfcshare.service.NfcService;

@SuppressLint("StaticFieldLeak")
public class Utils {
    public static final String QUERY_CARD = "00A404000E325041592E5359532E444446303100";
    public static Gson Gson = new Gson();
    public static MqttService mqttService;
    public static ArrayBlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(100);
    public static boolean isServer;
    public static NfcService nfcService;
    public static MainActivity mainActivity;
    public static String clientId;
    public static EmulationService emulationService;

    public static void addLogs(String... strs) {
        Log.d("TAG111", strs[0]);
        for (String str : strs) {
            mainActivity.appendLog(str);
        }
    }
}
