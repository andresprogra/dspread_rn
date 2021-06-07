import {useEffect, useState, useCallback} from 'react';
import useLocationChecker from './permissions/useLocationChecker';
import useBluetoothChecker from './permissions/useBluetoothChecker';

import QPOS, {QPOSEmitter} from '../QPOS';
import {Mode, Events} from '../types';

function useQPOS() {
  const {locationEnabled} = useLocationChecker();
  const {bluetoothEnabled} = useBluetoothChecker();
  const [connected, setConnected] = useState<Boolean>(false);
  const [error, setError] = useState();

  useEffect(() => {
    const errorListener = QPOSEmitter.addListener(Events.ERROR, event => {
      setError(event.status);
    });

    const connectionListener = QPOSEmitter.addListener(
      Events.CONNECTION,
      event => {
        console.log(Events.CONNECTION, event);
        setConnected(event.connected);
      },
    );

    return () => {
      connectionListener?.remove();
      errorListener?.remove();
    };
  }, []);

  useEffect(() => {
    if (locationEnabled && bluetoothEnabled) {
      QPOS.open(Mode.BLE);

      return () => {
        QPOS.disconnect();
        QPOS.close();
      };
    }
  }, [locationEnabled, bluetoothEnabled]);

  const connect = useCallback(
    (address: string) => {
      if (connected) {
        return;
      }
      QPOS.connect(address);
    },
    [connected],
  );

  return {connected, error, connect};
}

export default useQPOS;
