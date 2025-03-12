package nfc.share.nfcshare.service;

import static nfc.share.nfcshare.model.MqttChannel.LOG_CHANNEL;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cn.hutool.core.util.HexUtil;
import nfc.share.nfcshare.MainActivity;
import nfc.share.nfcshare.Utils;

public class NfcService implements NfcAdapter.ReaderCallback {
    private IsoDep isoDep;
    NfcAdapter nfcAdapter;
    private String currentCardId = "";
    private Timer cardCheckTimer;

    public NfcService(MainActivity context) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            Utils.addLogs("Not Support NFC");
            return;
        }
        nfcAdapter.enableReaderMode(context, this, 1, null);
    }


    @Override
    public void onTagDiscovered(Tag discoveredTag) {
        if (discoveredTag != null) {
            Utils.blockingQueue.clear();
            try {
                currentCardId = connectCard(discoveredTag);
                if (cardCheckTimer != null) {
                    cardCheckTimer.cancel();
                }
                cardCheckTimer = new Timer();
                cardCheckTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            sendData(Utils.QUERY_CARD);
                        } catch (IOException e) {
                            onTagRemoved();
                        }
                    }
                }, 3000, 3000);

                Utils.mqttService.pushMessageToMqtt(LOG_CHANNEL, "Card connected: " + currentCardId);
            } catch (IOException e) {
                Utils.addLogs("Connect card failed");
            }
        }
    }

    private String connectCard(Tag tag) throws IOException {
        IsoDep isoDep = IsoDep.get(tag);
        this.isoDep = isoDep;
        if (isoDep == null || isoDep.isConnected()) {
            Utils.addLogs("Card not support");
            throw new RuntimeException("Card not support");
        }
        isoDep.connect();
        isoDep.setTimeout(120000);
        final String cardId = HexUtil.encodeHexStr(tag.getId());
        String result = sendData(Utils.QUERY_CARD);
        if (result.length() < 4) {
            Utils.addLogs("Card not support");
            throw new RuntimeException("Card not support");
        }
        return cardId;
    }


    public String sendData(String command) throws IOException {
        byte[] decodeHex = HexUtil.decodeHex(command);
        return HexUtil.encodeHexStr(this.isoDep != null ? isoDep.transceive(decodeHex) : null);
    }

    public void onTagRemoved() {
        Utils.mqttService.pushMessageToMqtt(LOG_CHANNEL, "Card removed");
        Utils.addLogs("Card removed");
        if (isoDep != null && isoDep.isConnected()) {
            try {
                isoDep.close();
            } catch (IOException e) {
                Utils.addLogs("Error closing IsoDep connection");
            }
            isoDep = null;
        }
        cardCheckTimer.cancel();
        cardCheckTimer = null;
    }
}