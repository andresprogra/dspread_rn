import { useEffect, useState, useCallback } from 'react';
import useLocationChecker from './utils/useLocationChecker';
import useBluetoothChecker from './utils/useBluetoothChecker';

import QPOS, { QPOSEmitter, Events, Mode } from 'react-native-dspread';
import type { QPOSInfo } from 'react-native-dspread';

function useQPOS() {
  const { locationEnabled } = useLocationChecker();
  const { bluetoothEnabled } = useBluetoothChecker();
  const [connected, setConnected] = useState<Boolean>(false);
  const [connecting, setConnecting] = useState<Boolean>(false);
  const [qpos, setQpos] = useState<QPOSInfo>();
  const [error, setError] = useState();

  useEffect(() => {
    const errorListener = QPOSEmitter.addListener(
      Events.ERROR,
      (event: any) => {
        setError(event.status);
      }
    );

    const connectionListener = QPOSEmitter.addListener(
      Events.CONNECTION,
      (event: any) => {
        console.log(Events.CONNECTION, event);
        setConnecting(false);
        setConnected(event.connected);
      }
    );

    return () => {
      connectionListener?.remove();
      errorListener?.remove();
    };
  }, []);

  useEffect(() => {
    if (locationEnabled && bluetoothEnabled) {
      QPOS.open(Mode.BLE);
    }
    return () => {
      QPOS.disconnect();
      QPOS.close();
    };
  }, [locationEnabled, bluetoothEnabled]);

  const getInfo = useCallback(async () => {
    const info = await QPOS.getQposInfo(10);
    setQpos(info);
  }, []);

  const connect = useCallback(
    (address: string) => {
      if (connected) {
        return;
      }
      setConnecting(true);
      QPOS.connect(address);
    },
    [connected]
  );

  useEffect(() => {
    if (connected) {
      getInfo();
    }
  }, [connected, getInfo]);

  return { qpos, connected, connecting, error, connect, getInfo };
}

export default useQPOS;
