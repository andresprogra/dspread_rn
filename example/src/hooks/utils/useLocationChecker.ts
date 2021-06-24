import { useState, useEffect } from 'react';
import { PermissionsAndroid } from 'react-native';
import usePermission from './usePermission';
import LocationEnabler from 'react-native-location-enabler';

const {
  PRIORITIES: { HIGH_ACCURACY },
  addListener,
  checkSettings,
  requestResolutionSettings,
} = LocationEnabler;

const LOCATION_CONFIG = {
  priority: HIGH_ACCURACY,
  alwaysShow: true, // default false
  needBle: true,
};

function useLocationChecker(autoEnable: boolean = true) {
  const { permissionsGranted: locationPermissionsGranted } = usePermission(
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
  );
  const [enabled, setEnabled] = useState<boolean>();

  useEffect(() => {
    if (!locationPermissionsGranted) {
      return;
    }

    const onStateChange = ({ locationEnabled }: { locationEnabled: boolean }) =>
      setEnabled(locationEnabled);

    const listener = addListener(onStateChange);
    checkSettings(LOCATION_CONFIG);
    return () => listener.remove();
  }, [locationPermissionsGranted]);

  const enable = async () => await requestResolutionSettings(LOCATION_CONFIG);

  useEffect(() => {
    if (enabled === false && autoEnable) {
      console.log('location. enable');
      enable();
    }
  }, [enabled, autoEnable]);

  return { locationEnabled: enabled, enable };
}

export default useLocationChecker;
