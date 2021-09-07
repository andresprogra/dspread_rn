import { useEffect, useState, useCallback } from 'react';
import useNetwork from './utils/useNetwork';
import QPOS, {
  QPOSEmitter,
  Events,
  TransactionType,
  Request,
  Currency,
  TradeResult,
} from 'react-native-dspread';

function useQPOSTrade() {
  const { connectionInfo } = useNetwork();
  const [processing, setProcessing] = useState<boolean>(false);
  const [messages, setMessages] = useState<string[]>([]);

  const log = useCallback(
    (message: string) => {
      setMessages([message, ...messages]);
    },
    [messages, setMessages]
  );

  const processRequest = useCallback(
    (event: any) => {
      if (!event.request) {
        return;
      }
      switch (event.request) {
        case Request.APP: {
          //TODO: select an app
          log('Select an app');
          QPOS.selectEmvApp(0);
          break;
        }
        case Request.DISPLAY: {
          log(event.data.message);
          break;
        }
        case Request.CARD: {
          log('Waiting for a card');
          break;
        }
        case Request.CONFIRM: {
          log('Final confirm');
          //TODO: make a final confirm logic here
          QPOS.finalConfirm(true);
          break;
        }
        case Request.ONLINE: {
          log('Processing online');
          //TODO: make a backend call here
          break;
        }
        case Request.SERVER_CONNECTED: {
          //TODO: check server is connected
          QPOS.setServerConnected(connectionInfo?.isConnected ?? false);
          break;
        }
      }
    },
    [log, connectionInfo]
  );

  useEffect(() => {
    const tradeListener = QPOSEmitter.addListener(
      Events.TRADE,
      (event: any) => {
        console.log('event', event);
        if (event.request) {
          processRequest(event);
        }
        if (event.transaction) {
          setProcessing(false);
        }
        if (event.result) {
          switch (event.result) {
            case TradeResult.NFC_ONLINE:
              setTimeout(
                () =>
                  QPOS.getNFCBatchData((data: any) =>
                    console.log('getNFCBatchData', data)
                  ),
                200
              );
          }
          // setProcessing(false);
        }
      }
    );

    return () => {
      tradeListener?.remove();
    };
  }, [processRequest]);

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
    setProcessing(true);
    QPOS.doTrade(amount, cashbackAmount, currencyCode, type, timeout);
  };

  const cancelTrade = () => {
    QPOS.cancelTrade(true);
  };

  return { messages, processing, doTrade, cancelTrade };
}

export default useQPOSTrade;
