package nfc.share.nfcshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;

import nfc.share.nfcshare.service.EmulationService;
import nfc.share.nfcshare.service.MqttService;
import nfc.share.nfcshare.service.NfcService;

public class MainActivity extends AppCompatActivity {
    private TextView log, server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Utils.mainActivity = this;
        log = findViewById(R.id.log);
        server = findViewById(R.id.ip);
        SwitchCompat aSwitch = findViewById(R.id.switchSender);
        Button connectServer = findViewById(R.id.join);
        appendLog("Current mode is " + (Utils.isServer ? "Server" : "Client"));
        Utils.isServer = false;
        Utils.clientId = "Client";
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Utils.isServer = isChecked;
            Utils.clientId = (Utils.isServer ? "Server" : "Client");
            appendLog("Current mode is " + Utils.clientId);
        });
        connectServer.setOnClickListener(l -> {
            if (server.getText().toString().isEmpty()) {
                Toast.makeText(this, "Error: please enter server address", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                if (Utils.mqttService == null) {
                    Utils.mqttService = new MqttService(this);
                }
                Utils.mqttService.connect(server.getText().toString());
            } catch (MqttException e) {
                return;
            }
            if (Utils.isServer) {
                Intent service = new Intent(this, EmulationService.class);
                startService(service);
            } else {
                Utils.nfcService = new NfcService(this);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void appendLog(String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeString = sdf.format(new Date(System.currentTimeMillis()));
        runOnUiThread(() -> log.append(timeString + ":" + msg + "\n"));
    }
}