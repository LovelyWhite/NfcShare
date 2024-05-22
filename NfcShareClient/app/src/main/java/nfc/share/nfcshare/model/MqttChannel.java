package nfc.share.nfcshare.model;

public enum MqttChannel {
    // used to fetch card response
    FETCH_CHANNEL,
    // used to send msg to pos
    SEND_CHANNEL,
    // used to send msg
    LOG_CHANNEL
}
