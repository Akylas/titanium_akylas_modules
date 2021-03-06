/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import <Foundation/Foundation.h>
#import "TiProtectedModule.h"
#import <CoreBluetooth/CoreBluetooth.h>

@class AkylasBluetoothBLEDeviceProxy;
@interface AkylasBluetoothModule : TiProtectedModule<CBCentralManagerDelegate, CBPeripheralManagerDelegate>
//+(void)addManagedBLEDevice:(AkylasBluetoothBLEDeviceProxy*)device;
//+(void)removeManagedBLEDevice:(AkylasBluetoothBLEDeviceProxy*)device ;
+(void)connectBLEDevice:(CBPeripheral*)peripheral;
+(void)disconnectBLEDevice:(CBPeripheral*)peripheral;
+(CBCentralManager*) btManager;
+(AkylasBluetoothModule*)sharedInstance;
+(void)addPeripheralMapping:(TiProxy*)proxy forPeripheral:(CBPeripheral*)peripheral;
+(void)removePeripheralMapping:(CBPeripheral*)peripheral;
@end
