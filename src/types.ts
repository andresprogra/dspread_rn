export enum Currency {
  USD = '840',
  MXN = '484',
}

export enum Events {
  DEVICES = 'bluetooth_devices',
  CONNECTION = 'connection',
  ERROR = 'error',
  TRADE = 'trade',
}

export enum Mode {
  BLUETOOTH = 'BLUETOOTH',
  BLE = 'BLUETOOTH',
  AUDIO = 'AUDIO',
  USB = 'USB',
}

export enum TransactionType {
  GOODS = 'GOODS',
  SERVICES = 'SERVICES',
  CASH = 'CASH',
  CASHBACK = 'CASHBACK',
  INQUIRY = 'INQUIRY',
  TRANSFER = 'TRANSFER',
  ADMIN = 'ADMIN',
  CASHDEPOSIT = 'CASHDEPOSIT',
  PAYMENT = 'PAYMENT',
  PBOCLOG = 'PBOCLOG',
  ECQ_INQUIRE_LOG = 'ECQ_INQUIRE_LOG',
  SALE = 'SALE',
  PREAUTH = 'PREAUTH',
  ECQ_DESIGNATED_LOAD = 'ECQ_DESIGNATED_LOAD',
  ECQ_UNDESIGNATED_LOAD = 'ECQ_UNDESIGNATED_LOAD',
  ECQ_CASH_LOAD = 'ECQ_CASH_LOAD',
  ECQ_CASH_LOAD_VOID = 'ECQ_CASH_LOAD_VOID',
  REFUND = 'REFUND',
  SALES_NEW = 'SALES_NEW',
  LEGACY_MONEY_ADD = 'LEGACY_MONEY_ADD',
  NON_LEGACY_MONEY_ADD = 'NON_LEGACY_MONEY_ADD',
  BALANCE_UPDATE = 'BALANCE_UPDATE',
  UPDATE_PIN = 'UPDATE_PIN',
}

export enum TransactionResult {
  APPROVED = 'APPROVED',
  TERMINATED = 'TERMINATED',
  DECLINED = 'DECLINED',
  CANCEL = 'CANCEL',
  CAPK_FAIL = 'CAPK_FAIL',
  NOT_ICC = 'NOT_ICC',
  SELECT_APP_FAIL = 'SELECT_APP_FAIL',
  DEVICE_ERROR = 'DEVICE_ERROR',
  CARD_NOT_SUPPORTED = 'CARD_NOT_SUPPORTED',
  MISSING_MANDATORY_DATA = 'MISSING_MANDATORY_DATA',
  CARD_BLOCKED_OR_NO_EMV_APPS = 'CARD_BLOCKED_OR_NO_EMV_APPS',
  INVALID_ICC_DATA = 'INVALID_ICC_DATA',
  FALLBACK = 'FALLBACK',
  NFC_TERMINATED = 'NFC_TERMINATED',
  CARD_REMOVED = 'CARD_REMOVED',
  TRADE_LOG_FULL = 'TRADE_LOG_FULL',
  TRANSACTION_NOT_ALLOWED_AMOUNT_EXCEED = 'TRANSACTION_NOT_ALLOWED_AMOUNT_EXCEED',
  CONTACTLESS_TRANSACTION_NOT_ALLOW = 'CONTACTLESS_TRANSACTION_NOT_ALLOW',
  TRANS_TOKEN_INVALID = 'TRANS_TOKEN_INVALID',
  CARD_BLOCKED = 'CARD_BLOCKED',
  APP_BLOCKED = 'APP_BLOCKED',
  MULTIPLE_CARDS = 'MULTIPLE_CARDS',
}

export enum Request {
  APP = 'app',
  CARD = 'card',
  DISPLAY = 'display',
  CONFIRM = 'confirm',
  ONLINE = 'online',
  SERVER_CONNECTED = 'server',
}

export enum TradeResult {
  NONE = 'NONE', // no-op
  ICC = 'ICC', // no-op
  NOT_ICC = 'NOT_ICC', // no-op
  BAD_SWIPE = 'BAD_SWIPE', // no-op
  CARD_NOT_SUPPORT = 'CARD_NOT_SUPPORT', // no-op
  PLS_SEE_PHONE = 'PLS_SEE_PHONE', // no-op
  MCR = 'MCR', // magnetic card
  NFC_ONLINE = 'NFC_ONLINE',
  NFC_OFFLINE = 'NFC_OFFLINE',
  NFC_DECLINED = 'NFC_DECLINED', // no-op
  NO_RESPONSE = 'NO_RESPONSE', // no-op
}

export enum Error {
  CMD_NOT_AVAILABLE = 'CMD_NOT_AVAILABLE',
  TIMEOUT = 'TIMEOUT',
  DEVICE_RESET = 'DEVICE_RESET',
  UNKNOWN = 'UNKNOWN',
  DEVICE_BUSY = 'DEVICE_BUSY',
  INPUT_OUT_OF_RANGE = 'INPUT_OUT_OF_RANGE',
  INPUT_INVALID_FORMAT = 'INPUT_INVALID_FORMAT',
  INPUT_ZERO_VALUES = 'INPUT_ZERO_VALUES',
  INPUT_INVALID = 'INPUT_INVALID',
  CASHBACK_NOT_SUPPORTED = 'CASHBACK_NOT_SUPPORTED',
  CRC_ERROR = 'CRC_ERROR',
  COMM_ERROR = 'COMM_ERROR',
  MAC_ERROR = 'MAC_ERROR',
  CMD_TIMEOUT = 'CMD_TIMEOUT',
}

export type QPOSScanDevice = {
  name: string;
  address: string;
  bonded: boolean;
};

export interface QPOSInfo {
  PCIHardwareVersion?: string;
  SUB?: string;
  batteryLevel?: string;
  batteryPercentage?: string;
  bootloaderVersion?: string;
  csn?: string;
  deviceNumber?: string;
  firmwareVersion?: string;
  hardwareVersion?: string;
  isCharging?: boolean;
  isKeyboard?: boolean;
  isSupportNFC?: string;
  isSupportedTrack1?: boolean;
  isSupportedTrack2?: boolean;
  isSupportedTrack3?: boolean;
  isUsbConnected?: boolean;
  macAddress?: string;
  merchantId?: string;
  posId?: number;
  psamId?: number;
  psamNo?: string;
  tmk0Status?: number;
  tmk1Status?: number;
  tmk2Status?: number;
  tmk3Status?: number;
  tmk4Status?: number;
  updateWorkKeyFlag?: boolean;
  vendorCode?: string;
}
export interface QPOSDevice {
  getConstants(): Map<String, any>;

  getSdkVersion(callback: Function): void;

  open(mode: Mode): void;

  close(): void;

  startScan(timeout: number): void;

  stopScan(): void;

  connect(address: string): void;

  disconnect(): void;

  getQposInfo(timeout: number): Promise<any>;

  doTrade(
    amount: string,
    cashbackAmount: string,
    currencyCode: Currency,
    type: string,
    timeout: number
  ): void;

  cancelTrade(isUserCancel: boolean): void;

  sendOnlineProcessResult(tlv: string): void;

  setPin(pin: string): void;

  selectEmvApp(position: number): void;

  setServerConnected(connected: boolean): void;

  finalConfirm(isConfirmed: boolean): void;
}
