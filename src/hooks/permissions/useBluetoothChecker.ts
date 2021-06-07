import {useState, useEffect} from 'react';
import RNBluetoothClassic from 'react-native-bluetooth-classic';

function useBluetoothChecker(autoEnable = true) {
  const [bluetoothEnabled, setBluetoothEnabled] = useState<boolean>();

  useEffect(() => {
    const onStateChange = ({enabled}: {enabled: boolean}) => {
      setBluetoothEnabled(enabled);
    };

    const check = async () => {
      const enabled = await RNBluetoothClassic.isBluetoothEnabled();
      setBluetoothEnabled(enabled);
    };

    const subscription = RNBluetoothClassic.onBluetoothEnabled(onStateChange);
    check();
    return () => subscription.remove();
  }, []);

  const enable = async () => await RNBluetoothClassic.requestBluetoothEnabled();

  useEffect(() => {
    if (bluetoothEnabled == false && autoEnable) {
      enable();
    }
  }, [bluetoothEnabled, autoEnable]);

  return {bluetoothEnabled, enable};
}

export default useBluetoothChecker;
