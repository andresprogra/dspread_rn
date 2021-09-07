package com.dspread.rn;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dspread.rn.utils.ReactUtils;
import com.dspread.xpos.QPOSService;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.dspread.xpos.QPOSService.CommunicationMode;
import static com.dspread.xpos.QPOSService.DoTradeResult;
import static com.dspread.xpos.QPOSService.Error;
import static com.dspread.xpos.QPOSService.TransactionResult;
import static com.dspread.xpos.QPOSService.UpdateInformationResult;

/**
 *
 */
public class QPOSModule extends ReactContextBaseJavaModule implements QPOSMessenger.OnQPOSMessageListener {
    private static final int SCAN_BLUETOOTH_TIMEOUT = 20;
    private static final int SCAN_BLE_TIMEOUT = 6;

    private static final int QPOS_INFO_TIMEOUT = 10;

    private static final String EVENT_CONNECTION = "connection";
    private static final String EVENT_PARAM_CONNECTED = "connected";

    private static final String EVENT_DEVICES = "bluetooth_devices";
    private static final String EVENT_PARAM_DEVICES = "devices";
    private static final String EVENT_PARAM_SCANNING = "scanning";
    private static final String EVENT_PARAM_DEVICE_NAME = "name";
    private static final String EVENT_PARAM_DEVICE_ADDRESS = "address";
    private static final String EVENT_PARAM_DEVICE_BONDED = "bonded";

    private static final String EVENT_ERROR = "error";
    private static final String EVENT_PARAM_STATE = "state";

    private static final String EVENT_TRADE = "trade";
    private static final String EVENT_PARAM_TRADE_REQUEST = "request";
    private static final String TRADE_REQUEST_CARD = "card";
    private static final String TRADE_REQUEST_PIN = "pin";
    private static final String TRADE_REQUEST_CONFIRM = "confirm";
    private static final String TRADE_REQUEST_ONLINE = "online";
    private static final String TRADE_REQUEST_DISPLAY = "display";
    private static final String TRADE_REQUEST_APP = "app";
    private static final String TRADE_REQUEST_SERVER_CONNECTED = "server";
    private static final String EVENT_PARAM_TRADE_DATA = "data";
    private static final String EVENT_PARAM_TRADE_MESSAGE = "message";
    private static final String EVENT_PARAM_TRADE_APPS = "apps";
    private static final String EVENT_PARAM_TRADE_TRANSACTION = "transaction";
    private static final String EVENT_PARAM_TRADE_RESULT = "result";

    @Nullable
    private QPOSService pos;
    private QPOSService.CommunicationMode mode = QPOSService.CommunicationMode.BLUETOOTH;

    private final List<BluetoothDevice> devices = new ArrayList<>();

    QPOSModule(@NonNull ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return "QPOS";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("SCAN_BLUETOOTH_TIMEOUT", SCAN_BLUETOOTH_TIMEOUT);
        constants.put("SCAN_BLE_TIMEOUT", SCAN_BLE_TIMEOUT);
        return constants;
    }

    @ReactMethod
    public void open(@NonNull String mode) {
        if (pos != null) {
            if (mode.equals(this.mode.name())) {
                return;
            }
            close();
            pos.release();
            pos = null;
        }

        final CommunicationMode communicationMode = CommunicationMode.valueOf(mode);
        this.mode = communicationMode;
        pos = QPOSService.getInstance(communicationMode);
        pos.setConext(getReactApplicationContext().getApplicationContext());
        final Handler handler = new Handler(Looper.getMainLooper());

        final Context context = getCurrentActivity().getApplicationContext();
        final QPOSMessenger messenger = new QPOSMessenger(context, pos, this);
        pos.initListener(handler, messenger);
    }

    @ReactMethod
    public void close() {
        if (pos == null) {
            return;
        }
        switch (mode) {
            case AUDIO:
                pos.closeAudio();
                break;
            case BLUETOOTH:
                pos.disconnectBT();
                break;
            case BLUETOOTH_BLE:
                pos.disconnectBLE();
                break;
            case USB:
                pos.closeUsb();
                break;
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void getSdkVersion(Callback callback) {
        final String sdkVersion = QPOSService.getSdkVersion();
        callback.invoke(sdkVersion);
    }

    @ReactMethod
    public void startScan(int timeout) {
        if (pos == null) {
            return;
        }

        stopScan();
        devices.clear();
        pos.clearBluetoothBuffer();
        switch (mode) {
            case BLUETOOTH:
                pos.scanQPos2Mode(getCurrentActivity(), timeout);
                break;
            case BLUETOOTH_BLE:
                pos.startScanQposBLE(timeout);
                break;
        }

        final WritableMap params = new WritableNativeMap();
        params.putBoolean(EVENT_PARAM_SCANNING, true);
        sendEvent(EVENT_DEVICES, params);
    }

    @ReactMethod
    public void stopScan() {
        if (pos == null) {
            return;
        }
        switch (mode) {
            case BLUETOOTH:
                pos.stopScanQPos2Mode();
                break;
            case BLUETOOTH_BLE:
                pos.stopScanQposBLE();
                break;
        }

        final WritableMap params = new WritableNativeMap();
        params.putBoolean(EVENT_PARAM_SCANNING, false);
        sendEvent(EVENT_DEVICES, params);
    }

    @ReactMethod
    public void connect(@NonNull String address) {
        if (pos == null) {
            return;
        }
        stopScan();
        switch (mode) {
            case BLUETOOTH:
                pos.connectBT(address);
                break;
            case BLUETOOTH_BLE:
                pos.connectBLE(address);
                break;
        }
    }

    @ReactMethod
    public void disconnect() {
        if (pos == null) {
            return;
        }
        switch (mode) {
            case BLUETOOTH:
                pos.disconnectBT();
                break;
            case BLUETOOTH_BLE:
                pos.disconnectBLE();
                break;
        }
    }

    @ReactMethod
    public void getQposInfo(int timeout, Promise promise) {
        if (pos == null) {
            promise.reject(new RuntimeException("QPOS is not connected"));
            return;
        }
        try {
            final Hashtable<String, Object> qposId = pos.syncGetQposId(QPOS_INFO_TIMEOUT);
            final Hashtable<String, Object> qpos = pos.syncGetQposInfo(QPOS_INFO_TIMEOUT);
            final WritableMap device = ReactUtils.write(qpos, new WritableNativeMap());
            ReactUtils.write(qposId, device);
            promise.resolve(device);
        } catch (Throwable tr) {
            promise.reject(tr);
        }
    }

    @ReactMethod
    public void doTrade(@NonNull String amount,
                        @NonNull String cashbackAmount,
                        @NonNull String currencyCode,
                        @NonNull String type,
                        int timeout) {
        if (pos == null) {
            return;
        }

        final QPOSService.TransactionType transactionType =
                QPOSService.TransactionType.valueOf(type);
        pos.setAmount(amount, cashbackAmount, currencyCode, transactionType);
        pos.doTrade(timeout);
    }

    @ReactMethod
    public void sendOnlineProcessResult(@Nullable String tlv) {
        if (pos == null) {
            return;
        }
        /*
        if (isPinCanceled) {
            pos.sendOnlineProcessResult(null);
        } else {
            String str = "5A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F009505088004E0009A031406179C01009F02060000000000019F03060000000000009F0702AB009F080200209F0902008C9F0D05D86004A8009F0E0500109800009F0F05D86804F8009F101307010103A02000010A010000000000CE0BCE899F1A0201569F1E0838333230314943439F21031826509F2608881E2E4151E527899F2701809F3303E0F8C89F34030203009F3501229F3602008E9F37042120A7189F4104000000015A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F00";
            str = "9F26088930C9018CAEBCD69F2701809F101307010103A02802010A0100000000007EF350299F370415B4E5829F360202179505000004E0009A031504169C01009F02060000000010005F2A02015682027C009F1A0201569F03060000000000009F330360D8C89F34030203009F3501229F1E0838333230314943438408A0000003330101019F090200209F410400000001";
            String str = "8A023030";//Currently the default value,
            // should be assigned to the server to return data,
            // the data format is TLV
            pos.sendOnlineProcessResult(str);//Script notification/55domain/ICCDATA
        }
         */
        pos.sendOnlineProcessResult(tlv);
    }

    @ReactMethod
    public void selectEmvApp(int position) {
        if (pos == null) {
            return;
        }
        pos.selectEmvApp(position);
    }

    @ReactMethod
    public void setPin(@Nullable String pin) {
        if (pos == null) {
            return;
        }
        if (pin == null) {
            pos.cancelPin();
        } else if (pin.equals("")) {
            pos.bypassPin();
        } else {
            pos.sendPin(pin);
        }
    }

    @ReactMethod
    public void setServerConnected(boolean connected) {
        if (pos == null) {
            return;
        }
        pos.isServerConnected(connected);
    }

    @ReactMethod
    public void finalConfirm(boolean isConfirmed) {
        if (pos == null) {
            return;
        }
        pos.finalConfirm(isConfirmed);
    }

    @ReactMethod
    public void cancelTrade(Boolean isUserCancel) {
        if (pos == null) {
            return;
        }
        if (isUserCancel != null) {
            pos.cancelTrade(isUserCancel);
        } else {
            pos.cancelTrade();
        }
    }

  @ReactMethod(isBlockingSynchronousMethod = true)
  public void getNFCBatchData(Callback callback) {
    if (pos == null) {
      return;
    }

    final Hashtable<String, String> data = pos.getNFCBatchData();
    final WritableMap batchData = ReactUtils.write(data, new WritableNativeMap());
    callback.invoke(batchData);
  }

    @Override
    public void onError(@NonNull Error errorState) {
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_STATE, errorState.name());
        sendEvent(EVENT_ERROR, params);
    }

    @Override
    public void onDevicePermissionRequest(@NonNull UsbManager manager, @NonNull UsbDevice usbDevice) {
    }

    @Override
    public void onUpdateResult(@NonNull UpdateInformationResult result) {
    }

    @Override
    public void onUpdateFinished(boolean result) {
    }

    @Override
    public void onUpdateCanceled() {
    }

    @Override
    public void onDeviceFound(@NonNull BluetoothDevice device) {
        devices.add(device);
        sendDevices(true);
    }

    @Override
    public void onDeviceScanFinished() {
        sendDevices(false);
    }

    @Override
    public void onQposConnected() {
        final WritableMap params = new WritableNativeMap();
        params.putBoolean(EVENT_PARAM_CONNECTED, true);
        sendEvent(EVENT_CONNECTION, params);
    }

    @Override
    public void onQposDisconnected() {
        final WritableMap params = new WritableNativeMap();
        params.putBoolean(EVENT_PARAM_CONNECTED, false);
        sendEvent(EVENT_CONNECTION, params);
    }

    @Override
    public void onNoQposDetected() {
    }

    @Override
    public void onAmountRequest() {
    }

    @Override
    public void onWaitingForCard() {
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_CARD);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onRequestSelectEmvApp(@NonNull List<String> appList) {
        final WritableArray apps = new WritableNativeArray();
        for (String app : appList) {
            apps.pushString(app);
        }
        final WritableMap data = new WritableNativeMap();
        data.putArray(EVENT_PARAM_TRADE_APPS, apps);
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_APP);
        params.putMap(EVENT_PARAM_TRADE_DATA, data);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onRequestSetPin() {
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_PIN);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        final WritableMap data = new WritableNativeMap();
        data.putString(EVENT_PARAM_TRADE_MESSAGE, displayMsg.name());
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_DISPLAY);
        params.putMap(EVENT_PARAM_TRADE_DATA, data);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onRequestFinalConfirm() {
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_CONFIRM);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onRequestOnlineProcess(Hashtable<String, String> decodeData) {
        final ReadableMap data = ReactUtils.convert(decodeData);
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_ONLINE);
        params.putMap(EVENT_PARAM_TRADE_DATA, data);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onRequestServerConnected() {
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_REQUEST, TRADE_REQUEST_SERVER_CONNECTED);
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onTransactionResult(@NonNull TransactionResult transactionResult) {
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_TRANSACTION, transactionResult.name());
        sendEvent(EVENT_TRADE, params);
    }

    @Override
    public void onMaxTradeNum(int tradeNum) {
    }

    @Override
    public void onTradeResult(@NonNull DoTradeResult result, Hashtable<String, String> decodeData) {
        final ReadableMap data = ReactUtils.convert(decodeData);
        final WritableMap params = new WritableNativeMap();
        params.putString(EVENT_PARAM_TRADE_RESULT, result.name());
        params.putMap(EVENT_PARAM_TRADE_DATA, data);
        sendEvent(EVENT_TRADE, params);
    }

    private void sendDevices(boolean scanning) {
      final WritableArray result = new WritableNativeArray();
      for (BluetoothDevice device: devices) {
        final boolean bonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
        final String address = device.getAddress();
        final String name = device.getName();

        final WritableMap data = new WritableNativeMap();
        data.putString(EVENT_PARAM_DEVICE_ADDRESS, address);
        data.putString(EVENT_PARAM_DEVICE_NAME, name);
        data.putBoolean(EVENT_PARAM_DEVICE_BONDED, bonded);
        result.pushMap(data);
      }

      final WritableMap params = new WritableNativeMap();
      params.putArray(EVENT_PARAM_DEVICES, result);
      params.putBoolean(EVENT_PARAM_SCANNING, scanning);
      sendEvent(EVENT_DEVICES, params);
    }

    private void sendEvent(@NonNull String eventName,
                           @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

}


