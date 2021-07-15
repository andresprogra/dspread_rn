import { useEffect, useState } from 'react';
import useLocationChecker from './utils/useLocationChecker';
import useBluetoothChecker from './utils/useBluetoothChecker';

import QPOS, {
  QPOSEmitter,
  SCAN_BLE_TIMEOUT,
  Mode,
  Events,
} from 'react-native-dspread';
import type { QPOSScanDevice } from 'react-native-dspread';

function useQPOSScan() {
  const { locationEnabled } = useLocationChecker();
  const { bluetoothEnabled } = useBluetoothChecker();
  const [scanning, setScanning] = useState<boolean>(false);
  const [devices, setDevices] = useState<QPOSScanDevice[]>([]);

  useEffect(() => {
    const scanListener = QPOSEmitter.addListener(
      Events.DEVICES,
      (event: any) => {
        setDevices(event.devices);
        setScanning(!!event.scanning);
      }
    );

    return () => {
      scanListener?.remove();
    };
  }, []);

  const scan = () => {
    QPOS.startScan(SCAN_BLE_TIMEOUT);
  };

  useEffect(() => {
    if (locationEnabled && bluetoothEnabled) {
      QPOS.open(Mode.BLE);
      scan();
    }
    return () => {
      QPOS.close();
    };
  }, [locationEnabled, bluetoothEnabled]);

  return { scanning, devices, scan };
}

export default useQPOSScan;
