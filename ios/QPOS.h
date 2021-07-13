
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import "BTDeviceFinder.h"
#import "QPOSService.h"

@interface QPOS : RCTEventEmitter <RCTBridgeModule, QPOSServiceListener, BluetoothDelegate2Mode>
@end
