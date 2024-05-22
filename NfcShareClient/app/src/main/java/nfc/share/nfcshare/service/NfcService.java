package nfc.share.nfcshare.service;

import static nfc.share.nfcshare.model.MqttChannel.LOG_CHANNEL;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import cn.hutool.core.util.HexUtil;
import nfc.share.nfcshare.MainActivity;
import nfc.share.nfcshare.Utils;

public class NfcService implements NfcAdapter.ReaderCallback, NfcAdapter.OnTagRemovedListener {
    private IsoDep isoDep;
    NfcAdapter nfcAdapter;

    public NfcService(MainActivity context) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            Utils.addLogs("Not Support NFC");
            return;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        nfcAdapter.enableForegroundDispatch(context, pendingIntent, null, null);
        nfcAdapter.enableReaderMode(context, this, 1, null);
    }

    @Override
    public void onTagDiscovered(Tag discoveredTag) {
        if (discoveredTag != null) {
            try {
                String cardId = connectCard(discoveredTag);
                Utils.mqttService.pushMessageToMqtt(LOG_CHANNEL, "Card connected: " + cardId);
                Utils.addLogs("Card connected: " + cardId);
            } catch (IOException e) {
                Utils.addLogs("Connect card failed");
            }
            this.nfcAdapter.ignore(discoveredTag, 1000, this, new Handler(Looper.getMainLooper()));
        }
    }

    private String connectCard(Tag tag) throws IOException {
        IsoDep isoDep = IsoDep.get(tag);
        this.isoDep = isoDep;
        if (isoDep != null) {
            isoDep.connect();
        }
        String cardId = HexUtil.encodeHexStr(tag.getId());
        String result = sendData(Utils.QUERY_CARD);
        if (result.length() < 4) {
            Utils.addLogs("Card not support");
        }
        return cardId;
    }

    @Override
    public void onTagRemoved() {
        Utils.mqttService.pushMessageToMqtt(LOG_CHANNEL, "Card removed");
        Utils.addLogs("Card removed");
    }

    public String sendData(String command) throws IOException {
        byte[] decodeHex = HexUtil.decodeHex(command);
        return HexUtil.encodeHexStr(this.isoDep != null ? isoDep.transceive(decodeHex) : null);
    }
}