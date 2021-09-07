#import "QPOS.h"

#import <Foundation/Foundation.h>

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import "pos-ios-sdk-lib/BTDeviceFinder.h"
#import "pos-ios-sdk-lib/QPOSService.h"

@implementation QPOS {
  BTDeviceFinder *bt;
  NSMutableArray *devices;

  QPOSService *pos;
  PosType     mode;

  __block NSDictionary *posId;
  __block NSDictionary *posInfo;
  dispatch_semaphore_t posInfoSemaphore;
}

RCT_EXPORT_MODULE(QPOS);

- (NSArray<NSString *> *)supportedEvents {
    return @[@"connection", @"bluetooth_devices", @"error", @"trade"];
}

RCT_EXPORT_METHOD(open:(nonnull NSString *)mode) {
  if (nil == pos) {
      pos = [QPOSService sharedInstance];
  }

  //  self.mode = PostType(rawValue: mode);
  
  [pos setDelegate:self];
  [pos setQueue:nil];
//  [pos setPosType:mode];
  [pos setPosType:PosType_BLUETOOTH_2mode];
}

RCT_EXPORT_METHOD(close) {
  [self disconnect];
}

RCT_EXPORT_METHOD(getSdkVersion:(RCTResponseSenderBlock)callback) {
  const NSString* sdkVersion = [pos getSdkVersion];
  callback(@[sdkVersion]);
}

RCT_EXPORT_METHOD(startScan:(nonnull NSNumber *)timeout) {
  if (bt == nil) {
    bt = [BTDeviceFinder new];
  }
  NSLog(@"============= startScan %d", timeout.intValue);
  [bt stopQPos2Mode];
  devices = nil;
  [bt setBluetoothDelegate2Mode:self];
  [bt scanQPos2Mode:timeout.intValue];
}

RCT_EXPORT_METHOD(stopScan) {
  [bt stopQPos2Mode];
}

RCT_EXPORT_METHOD(connect:(nonnull NSString *)address) {
  [bt stopQPos2Mode];

  [pos connectBT:address];
  [pos setBTAutoDetecting:true];
}

RCT_EXPORT_METHOD(disconnect) {
  [pos disconnectBT];
}

RCT_EXPORT_METHOD(getQposInfo:(nonnull NSNumber *)timeout
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  if (nil == pos) {
    reject(@"QPOS", @"QPOS is not connected", nil);
    return;
  }

  posInfoSemaphore = dispatch_semaphore_create(0);
  [pos getQPosId];
  dispatch_semaphore_wait(posInfoSemaphore, DISPATCH_TIME_FOREVER);

  posInfoSemaphore = dispatch_semaphore_create(0);
  [pos getQPosInfo];
  dispatch_semaphore_wait(posInfoSemaphore, DISPATCH_TIME_FOREVER);

  if (nil == posInfo || nil == posId) {
      reject(@"QPOS", @"QPOS pos info request failed", nil);
  }
    
  const NSMutableDictionary * body = [posId mutableCopy];
  [body addEntriesFromDictionary:posInfo];
  resolve(body);
}

RCT_EXPORT_METHOD(doTrade:
                  (nonnull NSString *)amount
                  cashbackAmount:(nonnull NSString *)cashbackAmount
                  currencyCode:(nonnull NSString *)currencyCode
                  type:(nonnull NSString *)type
                  timeout:(nonnull NSNumber *)timeout) {
  //TODO: add transaction type
  [pos setAmount:amount aAmountDescribe:cashbackAmount currency:currencyCode transactionType:TransactionType_GOODS];
  [pos doTrade:timeout.intValue];
}
                
RCT_EXPORT_METHOD(sendOnlineProcessResult:(nonnull NSString *)tlv) {
  [pos sendOnlineProcessResult:tlv];
}

RCT_EXPORT_METHOD(selectEmvApp:(nonnull NSNumber *)position) {
  [pos selectEmvApp:position.intValue];
}

RCT_EXPORT_METHOD(setPin:(nullable NSString *)pin) {
  if (pin == nil) {
    [pos cancelPinEntry];
  } else if ([pin isEqualToString:@""]) {
    [pos bypassPinEntry];
  } else {
    [pos sendPinEntryResult:pin];
  }
}

RCT_EXPORT_METHOD(setServerConnected:(BOOL)connected) {
  [pos isServerConnected:connected];
}

RCT_EXPORT_METHOD(finalConfirm:(BOOL)isConfirmed) {
  [pos finalConfirm:isConfirmed];
}

RCT_EXPORT_METHOD(cancelTrade:(BOOL)isUserCancel) {
  [pos cancelTrade:isUserCancel];
}

RCT_EXPORT_METHOD(getNFCBatchData:(RCTResponseSenderBlock)callback) {
  const NSDictionary* data = [pos getNFCBatchData];
  callback(@[data]);
}

-(void)onBluetoothName2Mode:(NSString *)bluetoothName {
//  NSLog(@"=========== onBluetoothName2Mode %@", bluetoothName);
  if ([bluetoothName hasPrefix:@"MPOS"] || [bluetoothName hasPrefix:@"QPOS"]) {
    if (nil == devices) {
      devices = [[NSMutableArray alloc] init];
    }
    
    const NSMutableDictionary* deviceMap = [[NSMutableDictionary alloc] init];
    deviceMap[@"name"] = bluetoothName;
    deviceMap[@"address"] = bluetoothName;
    deviceMap[@"bonded"] = @(FALSE);
    [devices addObject:deviceMap];
    
    const NSMutableDictionary* data = [[NSMutableDictionary alloc] init];
    data[@"devices"] = devices;
    data[@"scanning"] = @(TRUE);
    [self sendEventWithName:@"bluetooth_devices" body:data];
  }
}

-(void) onRequestTime{
    NSString *formatStringForHours = [NSDateFormatter dateFormatFromTemplate:@"j" options:0 locale:[NSLocale currentLocale]];
    NSRange containA = [formatStringForHours rangeOfString:@"a"];
    BOOL hasAMPM = containA.location != NSNotFound;
    //when phone time is 12h format, need add this judgement.
    if (hasAMPM) {
      NSDateFormatter *dateFormatter = [NSDateFormatter new];
      [dateFormatter setDateFormat:@"yyyyMMddhhmmss"];
      [pos sendTime:[dateFormatter stringFromDate:[NSDate date]]];
    } else {
      NSDateFormatter *dateFormatter = [NSDateFormatter new];
      [dateFormatter setDateFormat:@"yyyyMMddHHmmss"];
      [pos sendTime:[dateFormatter stringFromDate:[NSDate date]]];
    }
}

-(void)onRequestQposConnected {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
//TODO:  data[@"device"] = device;
  body[@"connected"] = @(TRUE);
  [self sendEventWithName:@"connection" body:body];
}
-(void)onRequestQposDisconnected {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"connected"] = @(FALSE);
  [self sendEventWithName:@"connection" body:body];
}

-(void)onRequestWaitingUser {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"card";
  [self sendEventWithName:@"trade" body:body];
}
-(void)onRequestSelectEmvApp: (NSArray*)appList {
  const NSMutableDictionary* data = [[NSMutableDictionary alloc] init];
  data[@"apps"] = appList;

  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"app";
  body[@"data"] = data;
  [self sendEventWithName:@"trade" body:body];
}
-(void)onRequestPinEntry {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"pin";
  [self sendEventWithName:@"trade" body:body];
}
-(void)onRequestFinalConfirm {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"confirm";
  [self sendEventWithName:@"trade" body:body];
}
-(void)onRequestOnlineProcess: (NSString*) tlv {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"online";
  body[@"data"] = [pos anlysEmvIccData:tlv];
  [self sendEventWithName:@"trade" body:body];
}
-(void)onRequestDisplay: (Display)displayMsg {
  const NSMutableDictionary* data = [[NSMutableDictionary alloc] init];
  switch (displayMsg) {
    case Display_TRY_ANOTHER_INTERFACE:
      data[@"message"] = @"TRY_ANOTHER_INTERFACE";
      break;
    case Display_PLEASE_WAIT:
      data[@"message"] = @"PLEASE_WAIT";
      break;
    case Display_REMOVE_CARD:
      data[@"message"] = @"REMOVE_CARD";
      break;
    case Display_CLEAR_DISPLAY_MSG:
      data[@"message"] = @"CLEAR_DISPLAY_MSG";
      break;
    case Display_PROCESSING:
      data[@"message"] = @"PROCESSING";
      break;
    case Display_TRANSACTION_TERMINATED:
      data[@"message"] = @"TRANSACTION_TERMINATED";
      break;
    case Display_PIN_OK:
      data[@"message"] = @"PIN_OK";
      break;
    case Display_INPUT_PIN_ING:
      data[@"message"] = @"INPUT_PIN_ING";
      break;
    case Display_MAG_TO_ICC_TRADE:
      data[@"message"] = @"MAG_TO_ICC_TRADE";
      break;
    case Display_INPUT_OFFLINE_PIN_ONLY:
      data[@"message"] = @"INPUT_OFFLINE_PIN_ONLY";
      break;
    case Display_INPUT_LAST_OFFLINE_PIN:
      data[@"message"] = @"INPUT_LAST_OFFLINE_PIN";
      break;
    case Display_CARD_REMOVED:
      data[@"message"] = @"CARD_REMOVED";
      break;
    case Display_MSR_DATA_READY:
      data[@"message"] = @"MSR_DATA_READY";
      break;
    default:
      break;
  }

  data[@"code"] = @(displayMsg);

  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"display";
  body[@"data"] = data;
  [self sendEventWithName:@"trade" body:body];
}
-(void)onRequestIsServerConnected {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  body[@"request"] = @"server";
  [self sendEventWithName:@"trade" body:body];
}

-(void)onDoTradeResult: (DoTradeResult)result DecodeData:(NSDictionary*)decodeData {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  
  switch (result) {
    case DoTradeResult_NONE:
      body[@"result"] = @"NONE";
      break;
    case DoTradeResult_MCR:
      body[@"result"] = @"MCR";
      break;
    case DoTradeResult_ICC:
      body[@"result"] = @"ICC";
      break;
    case DoTradeResult_BAD_SWIPE:
      body[@"result"] = @"BAD_SWIPE";
      break;
    case DoTradeResult_NO_RESPONSE:
      body[@"result"] = @"NO_RESPONSE";
      break;
    case DoTradeResult_NOT_ICC:
      body[@"result"] = @"NOT_ICC";
      break;
    case DoTradeResult_NO_UPDATE_WORK_KEY:
      body[@"result"] = @"NO_UPDATE_WORK_KEY";
      break;
    case DoTradeResult_NFC_ONLINE: // add 20150715
      body[@"result"] = @"NFC_ONLINE";
      break;
    case DoTradeResult_NFC_OFFLINE:
      body[@"result"] = @"NFC_OFFLINE";
      break;
    case DoTradeResult_NFC_DECLINED:
      body[@"result"] = @"NFC_DECLINED";
      break;
    case DoTradeResult_TRY_ANOTHER_INTERFACE:
      body[@"result"] = @"TRY_ANOTHER_INTERFACE";
      break;
    case DoTradeResult_CARD_NOT_SUPPORT:
      body[@"result"] = @"CARD_NOT_SUPPORT";
      break;
    case DoTradeResult_PLS_SEE_PHONE:
      body[@"result"] = @"PLS_SEE_PHONE";
      break;
    default:
      break;
  }
  
  body[@"code"] = @(result);
  body[@"data"] = decodeData;
  [self sendEventWithName:@"trade" body:body];
  
  if (result == DoTradeResult_ICC) {
    [pos doEmvApp:EmvOption_START];
  }
}

-(void)onRequestTransactionResult: (TransactionResult)transactionResult {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  switch (transactionResult) {
    case TransactionResult_APPROVED:
      body[@"transaction"] = @"APPROVED";
      break;
    case TransactionResult_TERMINATED:
      body[@"transaction"] = @"TERMINATED";
      break;
    case TransactionResult_DECLINED:
      body[@"transaction"] = @"DECLINED";
      break;
    case TransactionResult_CANCEL:
      body[@"transaction"] = @"CANCEL";
      break;
    case TransactionResult_CAPK_FAIL:
      body[@"transaction"] = @"CAPK_FAIL";
      break;
    case TransactionResult_NOT_ICC:
      body[@"transaction"] = @"NOT_ICC";
      break;
    case TransactionResult_SELECT_APP_FAIL:
      body[@"transaction"] = @"SELECT_APP_FAIL";
      break;
    case TransactionResult_DEVICE_ERROR:
      body[@"transaction"] = @"DEVICE_ERROR";
      break;
    case TransactionResult_CARD_NOT_SUPPORTED:
      body[@"transaction"] = @"CARD_NOT_SUPPORTED";
      break;
    case TransactionResult_MISSING_MANDATORY_DATA:
      body[@"transaction"] = @"MISSING_MANDATORY_DATA";
      break;
    case TransactionResult_CARD_BLOCKED_OR_NO_EMV_APPS:
      body[@"transaction"] = @"CARD_BLOCKED_OR_NO_EMV_APPS";
      break;
    case TransactionResult_INVALID_ICC_DATA:
      body[@"transaction"] = @"INVALID_ICC_DATA";
      break;
    case TransactionResult_FALLBACK:
      body[@"transaction"] = @"FALLBACK";
      break;
    case TransactionResult_NFC_TERMINATED:
      body[@"transaction"] = @"NFC_TERMINATED";
      break;
    case TransactionResult_TRADE_LOG_FULL:
      body[@"transaction"] = @"TRADE_LOG_FULL";
      break;
    case TransactionResult_CONTACTLESS_TRANSACTION_NOT_ALLOW:
      body[@"transaction"] = @"CONTACTLESS_TRANSACTION_NOT_ALLOW";
      break;
    case TransactionResult_CARD_BLOCKED:
      body[@"transaction"] = @"CARD_BLOCKED";
      break;
    case TransactionResult_TOKEN_INVALID:
      body[@"transaction"] = @"TOKEN_INVALID";
      break;
    case TransactionResult_APP_BLOCKED:
      body[@"transaction"] = @"APPROVED";
      break;
    default:
      break;
  }
  body[@"code"] = @(transactionResult);
  [self sendEventWithName:@"trade" body:body];
}

-(void)onQposIdResult: (NSDictionary*)posIdData {
  posId = posIdData;
  dispatch_semaphore_signal(posInfoSemaphore);
}

-(void)onQposInfoResult: (NSDictionary*)posInfoData {
  posInfo = posInfoData;
  dispatch_semaphore_signal(posInfoSemaphore);
}

-(void)onDHError: (DHError)errorState {
  const NSMutableDictionary* body = [[NSMutableDictionary alloc] init];
  switch (errorState) {
    case DHError_TIMEOUT:
      body[@"state"] = @"TIMEOUT";
      break;
    case DHError_MAC_ERROR:
      body[@"state"] = @"MAC_ERROR";
      break;
    case DHError_CMD_NOT_AVAILABLE:
      body[@"state"] = @"CMD_NOT_AVAILABLE";
      break;
    case DHError_DEVICE_RESET:
      body[@"state"] = @"DEVICE_RESET";
      break;
    case DHError_DEVICE_BUSY:
      body[@"state"] = @"DEVICE_BUSY";
      break;
    case DHError_INPUT_OUT_OF_RANGE:
      body[@"state"] = @"INPUT_OUT_OF_RANGE";
      break;
    case DHError_INPUT_INVALID_FORMAT:
      body[@"state"] = @"INPUT_INVALID_FORMAT";
      break;
    case DHError_INPUT_ZERO_VALUES:
      body[@"state"] = @"INPUT_ZERO_VALUES";
      break;
    case DHError_INPUT_INVALID:
      body[@"state"] = @"INPUT_INVALID";
      break;
    case DHError_CASHBACK_NOT_SUPPORTED:
      body[@"state"] = @"CASHBACK_NOT_SUPPORTED";
      break;
    case DHError_CRC_ERROR:
      body[@"state"] = @"CRC_ERROR";
      break;
    case DHError_COMM_ERROR:
      body[@"state"] = @"COMM_ERROR";
      break;
    case DHError_CMD_TIMEOUT:
      body[@"state"] = @"CMD_TIMEOUT";
      break;
    case DHError_WR_DATA_ERROR:
      body[@"state"] = @"WR_DATA_ERROR";
      break;
    case DHError_EMV_APP_CFG_ERROR:
      body[@"state"] = @"EMV_APP_CFG_ERROR";
      break;
    case DHError_EMV_CAPK_CFG_ERROR:
      body[@"state"] = @"EMV_CAPK_CFG_ERROR";
      break;
    case DHError_APDU_ERROR:
      body[@"state"] = @"APDU_ERROR";
      break;
    case DHError_ICC_ONLINE_TIMEOUT:
      body[@"state"] = @"ICC_ONLINE_TIMEOUT";
      break;
    case DHError_AMOUNT_OUT_OF_LIMIT:
      body[@"state"] = @"AMOUNT_OUT_OF_LIMIT";
      break;
    case DHError_DIGITS_UNAVAILABLE:
      body[@"state"] = @"DIGITS_UNAVAILABLE";
      break;
    case DHError_QPOS_MEMORY_OVERFLOW:
      body[@"state"] = @"QPOS_MEMORY_OVERFLOW";
      break;
    case DHError_UNKNOWN:
    default:
      body[@"state"] = @"UNKNOWN";
      break;
  }
  body[@"code"] = @(errorState);
  [self sendEventWithName:@"error" body:body];
}

@end
