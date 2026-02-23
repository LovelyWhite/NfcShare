# NfcShare

NfcShare is a small demo project that shows how to tunnel NFC smart‑card
communication over the network. It consists of:

- An Android client that can act either as an NFC card reader or as a
  host‑card emulation (HCE) device
- A lightweight Node.js MQTT broker that exposes a WebSocket endpoint
  used by the Android client

The goal is to let two Android devices exchange APDU commands and
responses in real time through an MQTT message broker.

## Project Structure

- `NfcShareClient/` – Android application (Kotlin/Java, NFC + HCE)
  - Uses `NfcAdapter` / `IsoDep` to read NFC cards
  - Uses `HostApduService` to emulate a card and forward APDUs
  - Communicates with the server over MQTT via WebSocket
- `NfcShareServer/` – Node.js MQTT broker
  - Based on `aedes` and `websocket-stream`
  - Listens on port `8888` and upgrades WebSocket connections

## How It Works

- The Android app connects to the MQTT broker over WebSocket
  (default URL: `ws://suxitech.cn:8888` shown in the UI).
- One device can act as the **reader**:
  - It uses `NfcAdapter` in reader mode and `IsoDep` to talk to a card.
  - APDU requests and responses are sent as hex strings through MQTT.
- Another device can act as the **emulated card**:
  - It uses `HostApduService` to receive APDU commands.
  - APDU commands are forwarded to the MQTT broker and routed to the reader.
- All messages are wrapped as JSON payloads and published on a single
  MQTT topic so both sides can stay synchronized.

## Requirements

- Android device with NFC support
- Android Studio (to build and run `NfcShareClient`)
- Node.js (to run `NfcShareServer`)

## Running the Server

1. Open a terminal and go to the server folder:

   ```bash
   cd NfcShareServer
   ```

2. Install dependencies (you can use `yarn` or `npm`):

   ```bash
   yarn
   # or
   npm install
   ```

3. Start the MQTT WebSocket broker:

   ```bash
   yarn start
   # or
   npm start
   ```

   By default the server listens on port `8888`.

## Running the Android App

1. Open `NfcShareClient` in Android Studio.
2. Build and install the app on at least one NFC‑capable Android device.
3. Make sure the device can reach the MQTT server:
   - For a local server, replace the default URL in the input field
     with something like `ws://<your-ip>:8888`.
4. In the app:
   - Use the switch to choose whether the device acts as **Server**
     (emulated card) or **Client** (reader).
   - Enter the WebSocket URL of your server.
   - Tap the “Join” button to connect.

## License

This project is licensed under the Apache License, Version 2.0. See the
[`LICENSE`](LICENSE) file for details.
