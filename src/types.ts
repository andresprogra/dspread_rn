export const SCAN_BLUETOOTH_TIMEOUT = 20;
export const SCAN_BLE_TIMEOUT = 6;

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

export type QPOSScanDevice = {
  name: string;
  address: string;
  bonded: boolean;
};

export interface QPOSDevice {
  getConstants(): Map<String, any>;

  getSdkVersion(callback: Function): void;

  open(mode: Mode): void;

  close(): void;

  startScan(timeout: number): void;

  stopScan(): void;

  connect(address: string): void;

  disconnect(): void;

  getInfo(timeout: number): Promise<any>;

  doTrade(
    amount: string,
    cashbackAmount: string,
    currencyCode: Currency,
    type: string,
    timeout: number,
  ): void;

  cancelTrade(isUserCancel: boolean): void;

  sendOnlineProcessResult(tlv: string): void;

  selectEmvApp(position: number): void;

  setServerConnected(connected: boolean): void;
}
