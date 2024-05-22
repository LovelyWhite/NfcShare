package nfc.share.nfcshare.service;

import static nfc.share.nfcshare.model.MqttChannel.FETCH_CHANNEL;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.SystemClock;

import cn.hutool.core.util.HexUtil;
import nfc.share.nfcshare.Utils;

public class EmulationService extends HostApduService {

    long currentThreadTimeMillis;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String strApdu = HexUtil.encodeHexStr(commandApdu);
        Utils.addLogs("Sending command to server: " + strApdu);
        Utils.mqttService.pushMessageToMqtt(FETCH_CHANNEL, strApdu);
        currentThreadTimeMillis = SystemClock.currentThreadTimeMillis();
        while (SystemClock.currentThreadTimeMillis() - currentThreadTimeMillis <= 10000) {
            if (Utils.mqttService == null) {
                continue;
            }
            String result = Utils.blockingQueue.poll();
            if (result == null) {
                continue;
            }
            return HexUtil.decodeHex(result);
        }
        return HexUtil.decodeHex("FFFF");
    }

    @Override
    public void onDeactivated(int reason) {
        Utils.addLogs("Get server response failed " + reason);
    }
}
