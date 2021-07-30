import { useState, useEffect, useCallback } from 'react';
import { PermissionsAndroid, Platform } from 'react-native';
import RNAndroidLocationEnabler from 'react-native-android-location-enabler';
import usePermission from './usePermission';

function useLocationChecker(autoEnable: boolean = Platform.OS === 'android') {
  const { permissionsGranted: locationPermissionsGranted } = usePermission(
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
  );
  const [enabled, setEnabled] = useState<boolean>(Platform.OS === 'ios');

  const enable = useCallback(async () => {
    if (!locationPermissionsGranted) {
      return;
    }

    try {
      const result =
        await RNAndroidLocationEnabler.promptForEnableLocationIfNeeded({
          interval: 10000,
          fastInterval: 5000,
        });
      if (result === 'enabled' || result === 'already-enabled') {
        setEnabled(true);
      } else {
        setEnabled(false);
      }
    } catch (e) {}
  }, [locationPermissionsGranted]);

  useEffect(() => {
    if (!enabled && autoEnable) {
      enable();
    }
  }, [enabled, autoEnable, enable]);

  return { locationEnabled: enabled, enable };
}

export default useLocationChecker;
