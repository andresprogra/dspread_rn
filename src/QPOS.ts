import {NativeEventEmitter, NativeModule, NativeModules} from 'react-native';
import {QPOSDevice} from './types';

const QPOS: QPOSDevice & NativeModule = NativeModules.QPOS;
export const QPOSEmitter = new NativeEventEmitter(QPOS);

export default QPOS;
