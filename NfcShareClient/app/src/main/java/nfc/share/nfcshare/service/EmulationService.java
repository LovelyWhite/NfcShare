package nfc.share.nfcshare.service;

import static nfc.share.nfcshare.model.MqttChannel.FETCH_CHANNEL;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import cn.hutool.core.util.HexUtil;
import nfc.share.nfcshare.Utils;

public class EmulationService extends HostApduService {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.emulationService = this;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String strApdu = HexUtil.encodeHexStr(commandApdu);
        String str = "FFFF";
        if (Utils.mqttService == null) {
            return HexUtil.decodeHex(str);
        }
        Utils.mqttService.pushMessageToMqtt(FETCH_CHANNEL, strApdu);
        return null;
    }


    @Override
    public void onDeactivated(int reason) {
    }
}
