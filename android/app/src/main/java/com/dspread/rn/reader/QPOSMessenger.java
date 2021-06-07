package com.dspread.rn.reader;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dspread.rn.reader.utils.DUKPK2009_CBC;
import com.dspread.rn.reader.utils.QPOSUtil;
import com.dspread.rn.reader.utils.TRACE;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import Decoder.BASE64Encoder;

import static com.dspread.xpos.QPOSService.DoTradeResult;
import static com.dspread.xpos.QPOSService.Error;
import static com.dspread.xpos.QPOSService.TransactionResult;
import static com.dspread.xpos.QPOSService.UpdateInformationResult;

/**
 *
 */
class QPOSMessenger extends CQPOSService {
    private static final String TAG = "QPOSMessenger";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private final Context context;
    private final QPOSService pos;
    private final OnQPOSMessageListener listener;

    QPOSMessenger(@NonNull Context context,
                  @NonNull QPOSService pos,
                  @NonNull OnQPOSMessageListener listener) {
        this.context = context;
        this.pos = pos;
        this.listener = listener;
    }

    public interface OnQPOSMessageListener {
        void onError(@NonNull Error errorState);

        void onDevicePermissionRequest(@NonNull UsbManager manager, @NonNull UsbDevice usbDevice);

        void onUpdateResult(@NonNull UpdateInformationResult result);
        void onUpdateFinished(boolean result);
        void onUpdateCanceled();

        void onDeviceFound(@NonNull BluetoothDevice device);
        void onDeviceScanFinished();

//        void onBluetoothBonded();
//        void onBluetoothBondTimeout();
//        void onBluetoothBondFailed();

        void onQposConnected();
        void onQposDisconnected();
        void onNoQposDetected();

        void onAmountRequest();
        /**
         * Wait for user to insert/swipe/tap card
         */
        void onWaitingForCard();
        void onRequestSelectEmvApp(@NonNull List<String> appList);
        void onRequestSetPin();
        void onRequestFinalConfirm();
        void onRequestOnlineProcess(Hashtable<String, String> decodeData);
        void onRequestDisplay(QPOSService.Display displayMsg);
        void onRequestServerConnected();

        void onTransactionResult(@NonNull TransactionResult transactionResult);
        void onMaxTradeNum(int tradeNum);
        void onTradeResult(@NonNull DoTradeResult result, Hashtable<String, String> decodeData);
    }

    @Override
    public void onRequestWaitingUser() {
        Log.d(TAG, "onRequestWaitingUser()");
        listener.onWaitingForCard();
    }

    @Override
    public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
        Log.d(TAG, "(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
        listener.onTradeResult(result, decodeData);

        String cardNo = "";
        if (result == DoTradeResult.NONE) {
        } else if (result == DoTradeResult.ICC) {
            Log.d(TAG, "EMV ICC Start");
            pos.doEmvApp(QPOSService.EmvOption.START);
        } else if (result == DoTradeResult.NOT_ICC) {
        } else if (result == DoTradeResult.BAD_SWIPE) {
        } else if (result == DoTradeResult.CARD_NOT_SUPPORT) {
        } else if (result == DoTradeResult.PLS_SEE_PHONE) {
        } else if (result == DoTradeResult.MCR) {//Magnetic card
            String formatID = decodeData.get("formatID");
            if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                String serviceCode = decodeData.get("serviceCode");
                String trackblock = decodeData.get("trackblock");
                String psamId = decodeData.get("psamId");
                String posId = decodeData.get("posId");
                String pinblock = decodeData.get("pinblock");
                String macblock = decodeData.get("macblock");
                String activateCode = decodeData.get("activateCode");
                String trackRandomNumber = decodeData.get("trackRandomNumber");
                cardNo = maskedPAN;
            } else if (formatID.equals("FF")) {
                String type = decodeData.get("type");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
            } else {
                String orderID = decodeData.get("orderId");
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                // String ksn = decodeData.get("ksn");
                String serviceCode = decodeData.get("serviceCode");
                String track1Length = decodeData.get("track1Length");
                String track2Length = decodeData.get("track2Length");
                String track3Length = decodeData.get("track3Length");
                String encTracks = decodeData.get("encTracks");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
                String partialTrack = decodeData.get("partialTrack");
                String pinKsn = decodeData.get("pinKsn");
                String trackksn = decodeData.get("trackksn");
                String pinBlock = decodeData.get("pinBlock");
                String encPAN = decodeData.get("encPAN");
                String trackRandomNumber = decodeData.get("trackRandomNumber");
                String pinRandomNumber = decodeData.get("pinRandomNumber");
                cardNo = maskedPAN;
                String realPan = null;
                if (!TextUtils.isEmpty(trackksn) && !TextUtils.isEmpty(encTrack2)) {
                    String clearPan = DUKPK2009_CBC.getDate(trackksn, encTrack2, DUKPK2009_CBC.Enum_key.DATA, DUKPK2009_CBC.Enum_mode.CBC);
                    realPan = clearPan.substring(0, maskedPAN.length());
                }
                if (!TextUtils.isEmpty(pinKsn) && !TextUtils.isEmpty(pinBlock) && !TextUtils.isEmpty(realPan)) {
                    String date = DUKPK2009_CBC.getDate(pinKsn, pinBlock, DUKPK2009_CBC.Enum_key.PIN, DUKPK2009_CBC.Enum_mode.CBC);
                    String parsCarN = "0000" + realPan.substring(realPan.length() - 13, realPan.length() - 1);
                    String oin = DUKPK2009_CBC.xor(parsCarN, date);
                }
            }
        } else if ((result == DoTradeResult.NFC_ONLINE) || (result == DoTradeResult.NFC_OFFLINE)) {
            String nfcLog = decodeData.get("nfcLog");
            String formatID = decodeData.get("formatID");
            if (formatID.equals("31") || formatID.equals("40")
                    || formatID.equals("37") || formatID.equals("17")
                    || formatID.equals("11") || formatID.equals("10")) {
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                String serviceCode = decodeData.get("serviceCode");
                String trackblock = decodeData.get("trackblock");
                String psamId = decodeData.get("psamId");
                String posId = decodeData.get("posId");
                String pinblock = decodeData.get("pinblock");
                String macblock = decodeData.get("macblock");
                String activateCode = decodeData.get("activateCode");
                String trackRandomNumber = decodeData
                        .get("trackRandomNumber");
                cardNo = maskedPAN;
            } else {
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                String serviceCode = decodeData.get("serviceCode");
                String track1Length = decodeData.get("track1Length");
                String track2Length = decodeData.get("track2Length");
                String track3Length = decodeData.get("track3Length");
                String encTracks = decodeData.get("encTracks");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
                String partialTrack = decodeData.get("partialTrack");
                String pinKsn = decodeData.get("pinKsn");
                String trackksn = decodeData.get("trackksn");
                String pinBlock = decodeData.get("pinBlock");
                String encPAN = decodeData.get("encPAN");
                String trackRandomNumber = decodeData
                        .get("trackRandomNumber");
                String pinRandomNumber = decodeData.get("pinRandomNumber");
                cardNo = maskedPAN;
            }
            // pos.getICCTag(QPOSService.EncryptType.PLAINTEXT,1,1,"5F20") // get plaintext or ciphertext 5F20 tag
            // pos.getICCTag(QPOSService.EncryptType.PLAINTEXT,1,2,"5F205F24") // get plaintext or ciphertext 5F20 and 5F24 tag

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String nfcContent = "";
            if (nfcLog == null) {
                Hashtable<String, String> h = pos.getNFCBatchData();
                String tlv = h.get("tlv");
                Log.d(TAG, "nfc batchdata1: " + tlv);
            }
        } else if ((result == DoTradeResult.NFC_DECLINED)) {
        } else if (result == DoTradeResult.NO_RESPONSE) {
        }
    }

    @Override
    public void onQposInfoResult(Hashtable<String, String> posInfoData) {
        Log.d(TAG, "onQposInfoResult" + posInfoData.toString());
        String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
        String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
        String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
        String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
        String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
        String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
        String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
        String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
        String batteryPercentage = posInfoData.get("batteryPercentage") == null ? ""
                : posInfoData.get("batteryPercentage");
        String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
        String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
        String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? ""
                : posInfoData.get("PCI_firmwareVersion");
        String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? ""
                : posInfoData.get("PCI_hardwareVersion");
    }

    /**
     * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
     */
    @Override
    public void onRequestTransactionResult(TransactionResult transactionResult) {
        Log.d(TAG, "onRequestTransactionResult()" + transactionResult.toString());
        listener.onTransactionResult(transactionResult);
    }

    @Override
    public void onRequestBatchData(String tlv) {
        Log.d(TAG, "ICC trade finished");
        Log.d(TAG, "onRequestBatchData(String tlv):" + tlv);
    }

    @Override
    public void onRequestTransactionLog(String tlv) {
        Log.d(TAG, "onRequestTransactionLog(String tlv):" + tlv);
    }

    @Override
    public void onQposIdResult(Hashtable<String, String> posIdTable) {
        TRACE.w("onQposIdResult():" + posIdTable.toString());
        String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
        String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
        String psamId = posIdTable.get("psamId") == null ? "" : posIdTable
                .get("psamId");
        String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable
                .get("nfcID");
    }

    @Override
    public void onRequestSelectEmvApp(ArrayList<String> appList) {
        Log.d(TAG, "onRequestSelectEmvApp():" + appList.toString());
        Log.d(TAG, "Please select App -- S，emv card config");
        listener.onRequestSelectEmvApp(appList);
        //pos.selectEmvApp(position);
    }

    /**
     * pos.setAmount(amount, cashbackAmount, "156", transactionType);
     *         pos.cancelSetAmount();
     */
    @Override
    public void onRequestSetAmount() {
        Log.d(TAG, "input amount -- S");
        Log.d(TAG, "onRequestSetAmount()");
        listener.onAmountRequest();
    }

    /**
     * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestIsServerConnected()
     */
    @Override
    public void onRequestIsServerConnected() {
        Log.d(TAG, "onRequestIsServerConnected()");
//        pos.isServerConnected(true);
        listener.onRequestServerConnected();
    }

    @Override
    public void onRequestOnlineProcess(final String tlv) {
        Log.d(TAG, "onRequestOnlineProcess " + tlv);
        Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
        Log.d(TAG, "anlysEmvIccData(tlv):" + decodeData.toString());
        listener.onRequestOnlineProcess(decodeData);
    }

    @Override
    public void onRequestTime() {
        Log.d(TAG, "onRequestTime");
        String terminalTime = DATE_FORMAT.format(Calendar.getInstance().getTime());
        pos.sendTime(terminalTime);
    }

    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        Log.d(TAG, "onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        listener.onRequestDisplay(displayMsg);
    }

    /**
     * if (!isPinCanceled) {
     *             pos.finalConfirm(true);
     *             pos.finalConfirm(false);
     *         } else {
     *             pos.finalConfirm(false);
     *         }
     */
    @Override
    public void onRequestFinalConfirm() {
        Log.d(TAG, "onRequestFinalConfirm() ");
        listener.onRequestFinalConfirm();
    }

    @Override
    public void onRequestNoQposDetected() {
        Log.d(TAG, "onRequestNoQposDetected()");
        listener.onNoQposDetected();
    }

    @Override
    public void onRequestQposConnected() {
        Log.d(TAG, "onRequestQposConnected()");
        listener.onQposConnected();
    }

    @Override
    public void onRequestQposDisconnected() {
        Log.d(TAG, "onRequestQposDisconnected()");
        listener.onQposDisconnected();
    }

    @Override
    public void onError(QPOSService.Error errorState) {
        Log.d(TAG, "onError" + errorState.toString());
        listener.onUpdateCanceled();
        listener.onError(errorState);
    }

    @Override
    public void onReturnReversalData(String tlv) {
        Log.d(TAG, "onReturnReversalData(): " + tlv);
    }

    @Override
    public void onReturnGetPinResult(Hashtable<String, String> result) {
        Log.d(TAG, "onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
        String pinBlock = result.get("pinBlock");
        String pinKsn = result.get("pinKsn");
    }

    @Override
    public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
        Log.d(TAG, "onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
    }

    @Override
    public void onReturnPowerOffIccResult(boolean arg0) {
        Log.d(TAG, "onReturnPowerOffIccResult(boolean arg0):" + arg0);
    }

    @Override
    public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
        Log.d(TAG, "onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
        if (arg0) {
            pos.sendApdu("123456");
        }
    }

    @Override
    public void onReturnSetSleepTimeResult(boolean isSuccess) {
        Log.d(TAG, "onReturnSetSleepTimeResult(boolean isSuccess):" + isSuccess);
        String content = "";
        if (isSuccess) {
            content = "set the sleep time success.";
        } else {
            content = "set the sleep time failed.";
        }
    }

    @Override
    public void onGetCardNoResult(String cardNo) {
        Log.d(TAG, "onGetCardNoResult(String cardNo):" + cardNo);
    }

    @Override
    public void onRequestCalculateMac(String calMac) {
        Log.d(TAG, "onRequestCalculateMac(String calMac):" + calMac);
        if (calMac != null && !"".equals(calMac)) {
            calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
        }
        Log.d(TAG, "calMac_result: calMac=> e: " + calMac);
    }

    @Override
    public void onRequestSignatureResult(byte[] data) {
        Log.d(TAG, "onRequestSignatureResult(byte[] data):" + Arrays.toString(data));
    }

    @Override
    public void onRequestUpdateWorkKeyResult(UpdateInformationResult result) {
        Log.d(TAG, "onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
        listener.onUpdateResult(result);
    }

    @Override
    public void onReturnCustomConfigResult(boolean isSuccess, String result) {
        Log.d(TAG, "onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + TRACE.NEW_LINE + result);
    }

    /**
     *         if (pin.length() >= 4 && pin.length() <= 12) {
     *             pos.sendPin(pin);
     *         }
     *         pos.sendPin("");
     *         pos.cancelPin();
     */
    @Override
    public void onRequestSetPin() {
        Log.d(TAG, "onRequestSetPin()");
        listener.onRequestSetPin();
    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess) {
        Log.d(TAG, "onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
    }

    @Override
    public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
        Log.d(TAG, "onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult);
    }

    @Override
    public void onBluetoothBondFailed() {
        Log.d(TAG, "onBluetoothBondFailed()");
//        listener.onBluetoothBondFailed();
    }

    @Override
    public void onBluetoothBondTimeout() {
        Log.d(TAG, "onBluetoothBondTimeout()");
//        listener.onBluetoothBondTimeout();
    }

    @Override
    public void onBluetoothBonded() {
        Log.d(TAG, "onBluetoothBonded()");
//        listener.onBluetoothBonded();
    }

    @Override
    public void onBluetoothBonding() {
        Log.d(TAG, "onBluetoothBonding()");
    }

    @Override
    public void onReturniccCashBack(Hashtable<String, String> result) {
        Log.d(TAG, "onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
        String serviceCode = result.get("serviceCode");
        String trackblock = result.get("trackblock");
    }

    @Override
    public void onLcdShowCustomDisplay(boolean arg0) {
        Log.d(TAG, "onLcdShowCustomDisplay(boolean arg0):" + arg0);
    }

    @Override
    public void onUpdatePosFirmwareResult(UpdateInformationResult result) {
        Log.d(TAG, "onUpdatePosFirmwareResult(UpdateInformationResult result):" + result.toString());
        listener.onUpdateFinished(result != UpdateInformationResult.UPDATE_SUCCESS);
    }

    @Override
    public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
        Log.d(TAG, "onReturnDownloadRsaPublicKey(HashMap<String, String> map):" + map);
        if (map == null) {
            return;
        }

        String randomKeyLen = map.get("randomKeyLen");
        String randomKey = map.get("randomKey");
        String randomKeyCheckValueLen = map.get("randomKeyCheckValueLen");
        String randomKeyCheckValue = map.get("randomKeyCheckValue");
        Log.d(TAG, "randomKey" + randomKey + "    \n    randomKeyCheckValue" + randomKeyCheckValue);
    }

    @Override
    public void onGetPosComm(int mod, String amount, String posid) {
        Log.d(TAG, "onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
    }

    @Override
    public void onPinKey_TDES_Result(String arg0) {
        Log.d(TAG, "onPinKey_TDES_Result(String arg0):" + arg0);
    }

    @Override
    public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
        Log.d(TAG, "onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
    }

    @Override
    public void onEmvICCExceptionData(String arg0) {
        Log.d(TAG, "onEmvICCExceptionData(String arg0):" + arg0);
    }

    @Override
    public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
        Log.d(TAG, "onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
    }

    @Override
    public void onGetInputAmountResult(boolean arg0, String arg1) {
        Log.d(TAG, "onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
    }

    @Override
    public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
        Log.d(TAG, "onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
    }

    @Override
    public void onReturnPowerOffNFCResult(boolean arg0) {
        Log.d(TAG, " onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
    }

    @Override
    public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
        Log.d(TAG, "onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
    }

    @Override
    public void onCbcMacResult(String cbcMac) {
        Log.d(TAG, "onCbcMacResult(String cbcMac):" + cbcMac);
    }

    @Override
    public void onReadBusinessCardResult(boolean arg0, String arg1) {
        Log.d(TAG, " onReadBusinessCardResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
    }

    @Override
    public void onWriteBusinessCardResult(boolean arg0) {
        Log.d(TAG, " onWriteBusinessCardResult(boolean arg0):" + arg0);
    }

    @Override
    public void onConfirmAmountResult(boolean arg0) {
        Log.d(TAG, "onConfirmAmountResult(boolean arg0):" + arg0);
    }

    @Override
    public void onQposIsCardExist(boolean cardIsExist) {
        Log.d(TAG, "onQposIsCardExist(boolean cardIsExist):" + cardIsExist);
    }

    @Override
    public void onSearchMifareCardResult(Hashtable<String, String> card) {
        Log.d(TAG, "onSearchMifareCardResult(Hashtable<String, String> card):" + card);
        if (card != null) {
            String statuString = card.get("status");
            String cardTypeString = card.get("cardType");
            String cardUidLen = card.get("cardUidLen");
            String cardUid = card.get("cardUid");
            String cardAtsLen = card.get("cardAtsLen");
            String cardAts = card.get("cardAts");
            String ATQA = card.get("ATQA");
            String SAK = card.get("SAK");
        }
    }

    @Override
    public void onBatchReadMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
        Log.d(TAG, "onBatchReadMifareCardResult(boolean arg0):" + msg + cardData);
    }

    @Override
    public void onBatchWriteMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
        Log.d(TAG, "onBatchWriteMifareCardResult(boolean arg0):" + msg + cardData);
    }

    @Override
    public void onSetBuzzerResult(boolean success) {
        Log.d(TAG, "onSetBuzzerResult(boolean success):" + success);
    }

    @Override
    public void onSetBuzzerTimeResult(boolean b) {
        Log.d(TAG, "onSetBuzzerTimeResult(boolean b):" + b);
    }

    @Override
    public void onSetBuzzerStatusResult(boolean b) {
        Log.d(TAG, "onSetBuzzerStatusResult(boolean b):" + b);
    }

    @Override
    public void onGetBuzzerStatusResult(String s) {
        Log.d(TAG, "onGetBuzzerStatusResult(String s):" + s);
    }

    @Override
    public void onSetManagementKey(boolean success) {
        Log.d(TAG, "onSetManagementKey(boolean success):" + success);
    }

    @Override
    public void onReturnUpdateIPEKResult(boolean success) {
        Log.d(TAG, "onReturnUpdateIPEKResult(boolean success):" + success);
    }

    @Override
    public void onReturnUpdateEMVRIDResult(boolean arg0) {
        Log.d(TAG, "onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);
    }

    @Override
    public void onReturnUpdateEMVResult(boolean arg0) {
        Log.d(TAG, "onReturnUpdateEMVResult(boolean arg0):" + arg0);
    }

    @Override
    public void onBluetoothBoardStateResult(boolean arg0) {
        Log.d(TAG, "onBluetoothBoardStateResult(boolean arg0):" + arg0);
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (device == null || device.getName() == null) {
            // ignore
            return;
        }

        Log.d(TAG, "onDeviceFound(BluetoothDevice device):" + device.getName() + ":" + device.toString());
        listener.onDeviceFound(device);
    }

    @Override
    public void onSetSleepModeTime(boolean success) {
        Log.d(TAG, "onSetSleepModeTime(boolean success):" + success);
    }

    @Override
    public void onReturnGetEMVListResult(String result) {
        Log.d(TAG, "onReturnGetEMVListResult(String result):" + result);
    }

    @Override
    public void onWaitingforData(String data) {
        Log.d(TAG, "onWaitingforData(String data):" + data);
    }

    @Override
    public void onRequestDeviceScanFinished() {
        Log.d(TAG, "onRequestDeviceScanFinished()");
        listener.onDeviceScanFinished();
    }

    @Override
    public void onRequestUpdateKey(String arg0) {
        Log.d(TAG, "onRequestUpdateKey(String arg0):" + arg0);
    }

    @Override
    public void onReturnGetQuickEmvResult(boolean configed) {
        Log.d(TAG, "onReturnGetQuickEmvResult(boolean configed):" + configed);
        if (configed) {
            pos.setQuickEmv(true);
        }
    }

    @Override
    public void onQposDoGetTradeLogNum(String tradeNumber) {
        Log.d(TAG, "onQposDoGetTradeLogNum(String tradeNumber):" + tradeNumber);
        int a = Integer.parseInt(tradeNumber, 16);
        if (a >= 188) {
            listener.onMaxTradeNum(a);
        }
    }

    @Override
    public void onQposDoTradeLog(boolean success) {
        Log.d(TAG, "onQposDoTradeLog(boolean success) :" + success);
    }

    @Override
    public void onAddKey(boolean success) {
        Log.d(TAG, "onAddKey(boolean success) :" + success);
    }

    @Override
    public void onEncryptData(Hashtable<String, String> resultTable) {
        if (resultTable != null) {
            Log.d(TAG, "onEncryptData(String arg0) :" + resultTable);
        }
    }

    @Override
    public void onQposKsnResult(Hashtable<String, String> arg0) {
        Log.d(TAG, "onQposKsnResult(Hashtable<String, String> arg0):" + arg0.toString());
        String pinKsn = arg0.get("pinKsn");
        String trackKsn = arg0.get("trackKsn");
        String emvKsn = arg0.get("emvKsn");
        Log.d(TAG, "get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);
    }

    @Override
    public void onQposDoGetTradeLog(String orderId, String log) {
        Log.d(TAG, "onQposDoGetTradeLog(String orderId, String log):" + orderId + TRACE.NEW_LINE + log);
        log = QPOSUtil.convertHexToString(log);
    }

    @Override
    public void onRequestDevice() {
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            return;
        }
        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if (usbDevice.getVendorId() == 2965 || usbDevice.getVendorId() == 0x03EB) {
                if (usbManager.hasPermission(usbDevice)) {
                    pos.setPermissionDevice(usbDevice);
                } else {
                    listener.onDevicePermissionRequest(usbManager, usbDevice);
                }
            }
        }
    }

    @Override
    public void onGetKeyCheckValue(List<String> checkValue) {
        Log.d(TAG, "onGetKeyCheckValue(List<String> checkValue)" + checkValue);
    }

    @Override
    public void onGetDevicePubKey(String clearKeys) {
        Log.d(TAG, "onGetDevicePubKey(clearKeys):" + clearKeys);
        String lenStr = clearKeys.substring(0, 4);
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            int bit = Integer.parseInt(lenStr.substring(i, i + 1));
            sum += bit * Math.pow(16, (3 - i));
        }
        final String pubModel = clearKeys.substring(4, 4 + sum * 2);
//        listener.onDevicePubKey(pubModel);
    }

    @Override
    public void onTradeCancelled() {
        Log.d(TAG, "onTradeCancelled");
    }

    @Override
    public void onReturnSignature(boolean b, String signaturedData) {
        Log.d(TAG, "onReturnSignature(String result):" + " b=" + b + "  " + signaturedData);
        if (b) {
            final BASE64Encoder base64Encoder = new BASE64Encoder();
            String encode = base64Encoder.encode(signaturedData.getBytes());
//            listener.onSignatureReturned(encode);
        }
    }

    @Override
    public void onReturnConverEncryptedBlockFormat(String result) {
        Log.d(TAG, "onReturnConverEncryptedBlockFormat(String result):" + result);
    }

    @Override
    public void onFinishMifareCardResult(boolean success) {
        Log.d(TAG, "onFinishMifareCardResult(boolean success):" + success);
    }

    @Override
    public void onVerifyMifareCardResult(boolean success) {
        Log.d(TAG, "onVerifyMifareCardResult(boolean success):" + success);
    }

    @Override
    public void onReadMifareCardResult(Hashtable<String, String> data) {
        Log.d(TAG, "onReadMifareCardResult(Hashtable<String, String> data):" + data);
        if (data != null) {
            String addr = data.get("addr");
            String cardDataLen = data.get("cardDataLen");
            String cardData = data.get("cardData");
        } else {
            //"onReadWriteMifareCardResult fail"
        }
    }

    @Override
    public void onWriteMifareCardResult(boolean arg0) {
        Log.d(TAG, "onWriteMifareCardResult(boolean arg0):" + arg0);
    }

    @Override
    public void onOperateMifareCardResult(Hashtable<String, String> data) {
        Log.d(TAG, "onOperateMifareCardResult(Hashtable<String, String> data):" + data);
        if (data != null) {
            String cmd = data.get("Cmd");
            String blockAddr = data.get("blockAddr");
        } else {
            //"operate failed"
        }
    }

    @Override
    public void getMifareCardVersion(Hashtable<String, String> data) {
        Log.d(TAG, "getMifareCardVersion(Hashtable<String, String> data):" + data);
        if (data != null) {
            String verLen = data.get("versionLen");
            String ver = data.get("cardVersion");
        } else {
            //  "get mafire UL version failed"
        }
    }

    @Override
    public void getMifareFastReadData(Hashtable<String, String> data) {
        Log.d(TAG, "getMifareFastReadData(Hashtable<String, String> data):" + data);
        if (data != null) {
            String startAddr = data.get("startAddr");
            String endAddr = data.get("endAddr");
            String dataLen = data.get("dataLen");
            String cardData = data.get("cardData");
        } else {
            // "read fast UL failed"
        }
    }

    @Override
    public void getMifareReadData(Hashtable<String, String> data) {
        Log.d(TAG, "getMifareReadData(Hashtable<String, String> data):" + data);
        if (data != null) {
            String blockAddr = data.get("blockAddr");
            String dataLen = data.get("dataLen");
            String cardData = data.get("cardData");
        } else {
            // "read mafire UL failed"
        }
    }

    @Override
    public void writeMifareULData(String data) {
        Log.d(TAG, "writeMifareULData(String data):" + data);
    }

    @Override
    public void verifyMifareULData(Hashtable<String, String> data) {
        Log.d(TAG, "verifyMifareULData(Hashtable<String, String> data):" + data);
        if (data != null) {
            String dataLen = data.get("dataLen");
            String pack = data.get("pack");
        } else {
            // "verify UL failed"
        }
    }

    @Override
    public void onGetSleepModeTime(String time) {
        Log.d(TAG, "onGetSleepModeTime(String time):" + time);
    }

    @Override
    public void onGetShutDownTime(String time) {
        Log.d(TAG, "onGetShutDownTime(String time):" + time);
    }

    @Override
    public void onQposDoSetRsaPublicKey(boolean successed) {
        Log.d(TAG, "onQposDoSetRsaPublicKey(boolean successed):" + successed);
    }

    @Override
    public void onQposGenerateSessionKeysResult(Hashtable<String, String> data) {
        Log.d(TAG, "onQposGenerateSessionKeysResult(Hashtable<String, String> data):" + data);
        if (data != null) {
            String rsaFileName = data.get("rsaReginString");
            String enPinKeyData = data.get("enPinKey");
            String enKcvPinKeyData = data.get("enPinKcvKey");
            String enCardKeyData = data.get("enDataCardKey");
            String enKcvCardKeyData = data.get("enKcvDataCardKey");
        } else {
            // "get key failed,pls try again!"
        }
    }

    @Override
    public void transferMifareData(String data) {
        Log.d(TAG, "transferMifareData(String data):" + data);
    }

    @Override
    public void onReturnRSAResult(String data) {
        Log.d(TAG, "onReturnRSAResult(String data):" + data);
    }

    @Override
    public void onRequestNoQposDetectedUnbond() {
        Log.d(TAG, "onRequestNoQposDetectedUnbond()");
    }

}
