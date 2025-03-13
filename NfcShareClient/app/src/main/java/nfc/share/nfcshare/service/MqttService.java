package nfc.share.nfcshare.service;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

import cn.hutool.core.util.HexUtil;
import nfc.share.nfcshare.model.MqttChannel;
import nfc.share.nfcshare.model.NfcInfo;
import nfc.share.nfcshare.Utils;

public class MqttService {
    private final Context context;
    private MqttAndroidClient client;

    public MqttService(Context context) {
        this.context = context;
    }

    public void connect(String serverUrl) throws MqttException {
        if (client != null) {
            client.disconnect();
        }
        client = new MqttAndroidClient(context, serverUrl, Utils.clientId);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Utils.addLogs("Server connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String data = new String(message.getPayload());
                NfcInfo cardMessage = Utils.Gson.fromJson(data, NfcInfo.class);
                if (cardMessage.getSender().equals(Utils.clientId)) {
                    return;
                }
                Utils.addLogs("Received: " + cardMessage.getCardBytes());
                switch (cardMessage.getChannel()) {
                    case FETCH_CHANNEL:
                        String result = Utils.nfcService.sendData(cardMessage.getCardBytes());
                        pushMessageToMqtt(MqttChannel.SEND_CHANNEL, result);
                        break;
                    case SEND_CHANNEL:
                        Utils.emulationService.sendResponseApdu(HexUtil.decodeHex(cardMessage.getCardBytes()));
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        connect();
    }

    private void connect() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        client.connect(options, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Utils.addLogs("Server connect success");
                try {
                    subscribe();
                } catch (MqttException e) {
                    Utils.addLogs("Server connect failed");
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                Utils.addLogs("Server connect failed");
            }
        });
    }

    public void subscribe() throws MqttException {
        client.subscribe("message", 0);
    }

    public void pushMessageToMqtt(MqttChannel channel, String msg) {
        new Thread(() -> {
            try {
                Utils.addLogs("Sending: " + msg);
                NfcInfo nfcInfo = NfcInfo.builder().channel(channel).sender(Utils.clientId).cardBytes(msg).build();
                sendMessage(Utils.Gson.toJson(nfcInfo));
            } catch (Exception e) {
                Utils.addLogs("Push message to server failed");
            }
        }).start();
    }

    private void sendMessage(String msg) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes(StandardCharsets.UTF_8));
        client.publish("message", message);
    }
}
