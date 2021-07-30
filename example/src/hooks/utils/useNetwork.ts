import { useEffect, useState } from 'react';
import NetInfo, { NetInfoState } from '@react-native-community/netinfo';

function useNetwork() {
  const [connectionInfo, setConnectionInfo] = useState<NetInfoState>();

  useEffect(() => {
    NetInfo.fetch().then(setConnectionInfo);
    const unsubscribe = NetInfo.addEventListener(setConnectionInfo);
    return () => unsubscribe();
  }, []);

  return { connectionInfo };
}

export default useNetwork;
