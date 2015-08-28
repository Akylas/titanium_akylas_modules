/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import "TiProxy.h"

@class AkylasBluetoothBLEDeviceProxy;
@interface CBPeripheral (AkylasBluetoothBLEDeviceProxy)
- (void)setProxy:(AkylasBluetoothBLEDeviceProxy *)proxy;
- (AkylasBluetoothBLEDeviceProxy*)proxy;
-(void)didConnect;
-(void)didDisconnect;
@end

@interface AkylasBluetoothBLEDeviceProxy : TiProxy<CBPeripheralDelegate>


-(NSString*)identifier;
-(CBPeripheral*)peripheral;
-(void)didConnect;
-(void)didDisconnect;
@end
