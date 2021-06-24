import { useEffect, useState, useCallback } from 'react';
import useLocationChecker from './utils/useLocationChecker';
import useBluetoothChecker from './utils/useBluetoothChecker';

import QPOS, { QPOSEmitter, Events, Mode } from 'react-native-dspread';

function useQPOS() {
  const { locationEnabled } = useLocationChecker();
  const { bluetoothEnabled } = useBluetoothChecker();
  const [connected, setConnected] = useState<Boolean>(false);
  const [connecting, setConnecting] = useState<Boolean>(false);
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

  return { connected, connecting, error, connect };
}

export default useQPOS;
