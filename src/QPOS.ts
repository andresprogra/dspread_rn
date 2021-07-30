import { NativeEventEmitter, NativeModules } from 'react-native';
import type { EventSubscriptionVendor } from 'react-native';
import type { QPOSDevice } from './types';

const QPOS: QPOSDevice & EventSubscriptionVendor = NativeModules.QPOS;
export const QPOSEmitter = new NativeEventEmitter(QPOS);

export default QPOS;
