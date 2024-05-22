package nfc.share.nfcshare.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NfcInfo {
    private String cardBytes;
    private MqttChannel channel;
    private String sender;
}
