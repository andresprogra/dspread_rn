import {useEffect, useState, useCallback} from 'react';

import QPOS, {QPOSEmitter} from '../QPOS';
import {Currency, Events, TransactionType} from '../types';

function useQPOS() {
  const [transactionInProgress, setTransactionInProgress] =
    useState<boolean>(false);

  useEffect(() => {
    const tradeListener = QPOSEmitter.addListener(Events.TRADE, event => {
      console.log('event', event);
      // setConnected(event.connected);
    });

    return () => {
      tradeListener?.remove();
    };
  }, []);

  const doTrade = async ({
    amount,
    cashbackAmount = '0',
    currencyCode = Currency.MXN,
    type = TransactionType.GOODS,
    timeout = 60,
  }: {
    amount: string;
    cashbackAmount?: string;
    currencyCode?: Currency;
    type?: TransactionType;
    timeout?: number;
  }) => {
    setTransactionInProgress(true);
    QPOS.doTrade(amount, cashbackAmount, currencyCode, type, timeout);
    // console.log('doTrade result', result);
    // doTrade result {"DoTradeResult": "ICC", "code": "true", "result": "false"}
    /*
{
  "DoTradeResult": "MCR",
  "code": "true",
  "content": "{newPin=, encTrack3=, pinRandomNumber=, encTrack2=9CDB68735ED94ED6E0E51A9C5786D68F03765CF0A39F8D72, pinKsn=09221022001050E00001, encTrack1=19E50C93ABBDF978884B90E270AA8B1DD9127CB101613B7FC0D6C1C8AB5E52BCFE561A02D852F0CB73F6954D95DB52C1A61D4DCA358962B8CFC0D10DF92E2ED4, track3Length=0, pinBlock=9EED78FC2428D9FC, serviceCode=201, maskedPAN=521567XXXXXX2525, cardholderName=STEPAN/RUDENKO, formatID=30, psamNo=, partialTrack=, encPAN=, encTracks=9CDB68735ED94ED6E0E51A9C5786D68F03765CF0A39F8D72, trackRandomNumber=, track2Length=37, track1Length=53, trackksn=09221022001050E00001, expiryDate=2211}",
  "result": "true"
}
*/
  };

  const cancelTrade = () => {
    QPOS.cancelTrade(true);
  };

  return {transactionInProgress, doTrade, cancelTrade};
}

export default useQPOS;
