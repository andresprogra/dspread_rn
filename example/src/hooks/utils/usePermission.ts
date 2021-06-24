import { useState, useEffect, useCallback } from 'react';
import { Permission, PermissionsAndroid } from 'react-native';

type Result = {
  permissionsGranted: Boolean;
  checkPermissions: Function;
};

function usePermission(permission: Permission): Result {
  const [permissionsGranted, setPermissionsGranted] = useState(false);

  const checkPermissions = useCallback(async () => {
    try {
      const checkGranted = await PermissionsAndroid.check(permission);
      // console.log(`${permission} permission checked: ${checkGranted}`);
      if (checkGranted) {
        setPermissionsGranted(true);
        return;
      }

      const requestGranted = await PermissionsAndroid.request(permission);
      const granted = requestGranted === PermissionsAndroid.RESULTS.GRANTED;
      // console.log(`${permission} permission request: ${granted}`);
      setPermissionsGranted(granted);
    } catch (e) {
      setPermissionsGranted(false);
      console.warn(`Fail to request ${permission} permission`, e);
    }
  }, [permission]);

  useEffect(() => {
    checkPermissions();
  }, [checkPermissions]);
  return { permissionsGranted, checkPermissions };
}

export default usePermission;
