/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import "AkylasBluetoothModule.h"
#import <ExternalAccessory/ExternalAccessory.h>
#import "UARTPeripheral.h"
#import "AkylasBluetoothBLEDeviceProxy.h"





static AkylasBluetoothModule *_sharedInstance = nil;
@implementation AkylasBluetoothModule
{
    BOOL _registeredForNotifs;
    BOOL _pairing;
    BOOL _enabled;
    BOOL _discovering;
    EAAccessory * _pairingAccessory;
    CBCentralManager *_centralManager;
    NSMutableDictionary* _discoveredBLEDevices;
    KrollCallback * _discoveryCallback;
}

+(AkylasBluetoothModule*)sharedInstance
{
    return _sharedInstance;
}

- (void)_configure
{
    [super _configure];
    _sharedInstance = self;
    _registeredForNotifs = NO;
    _pairing = NO;
    _discovering = NO;
    _enabled = [[self btManager] state] == CBCentralManagerStatePoweredOn;
}

- (void)dealloc
{
    RELEASE_TO_NIL(_pairingAccessory)
    RELEASE_TO_NIL(_centralManager)
    RELEASE_TO_NIL(_discoveryCallback)
    RELEASE_TO_NIL(_discoveredBLEDevices)
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Bluetooth";
}

-(void)addDiscoveredBLEDevice:(CBPeripheral*)device {
    if (!_discoveredBLEDevices) {
        _discoveredBLEDevices = [[NSMutableDictionary alloc] init];
    }
    [_discoveredBLEDevices setObject:device forKey:[device identifier]];
}

-(void)removeManagedBLEDevice:(CBPeripheral*)device {
    if (!_discoveredBLEDevices) {
        return;
    }
    [_discoveredBLEDevices removeObjectForKey:[device identifier]];
}

//+(void)addManagedBLEDevice:(AkylasBluetoothBLEDeviceProxy*)device {
//    [[self sharedInstance] addManagedBLEDevice:device];
//}
//
//+(void)removeManagedBLEDevice:(AkylasBluetoothBLEDeviceProxy*)device {
//    [[self sharedInstance] removeManagedBLEDevice:device];
//}

-(CBCentralManager*) btManager {
    if (!_centralManager) {
        _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    }
    return _centralManager;
}

+(CBCentralManager*) btManager {
    return [[self sharedInstance] btManager];
}

- (id)pairedDevices {
    // Get list of connected accessories
    NSArray *accList = [[EAAccessoryManager sharedAccessoryManager] connectedAccessories];
    NSMutableArray* accs = [NSMutableArray arrayWithCapacity:[accList count]];
    [accList enumerateObjectsUsingBlock:^(EAAccessory* obj, NSUInteger idx, BOOL *stop) {
        [accs addObject:[self dictFromAccessory:obj]];
    }];
    return accs;
}

-(NSDictionary*)dictFromAccessory:(EAAccessory*)accessory
{
    return @{
             @"connected" : NUMBOOL([accessory isConnected]),
             @"connectionID" : NUMUINTEGER(accessory.connectionID),
             @"manufacturer" : accessory.manufacturer,
             @"name" : accessory.name,
             @"modelNumber" : accessory.modelNumber,
             @"serialNumber" : accessory.serialNumber,
             @"firmwareRevision" : accessory.firmwareRevision,
             @"hardwareRevision" : accessory.hardwareRevision};
}

-(NSDictionary*)dictFromPeripheral:(CBPeripheral*)peripheral
{
    return @{
             @"connected" : NUMBOOL([peripheral state] == CBPeripheralStateConnected),
//             @"connectionID" : NUMUINTEGER(accessory.connectionID),
//             @"manufacturer" : accessory.manufacturer,
             @"name" : peripheral.name,
             @"identifier" : [[peripheral identifier] UUIDString],
//             @"rssi" : peripheral.RSSI,
//             @"modelNumber" : accessory.modelNumber,
//             @"serialNumber" : accessory.serialNumber,
//             @"firmwareRevision" : accessory.firmwareRevision,
//             @"hardwareRevision" : accessory.hardwareRevision
             };
}

-(void)_listenerAdded:(NSString*)type count:(NSInteger)count
{
    if (count == 1 && ([type isEqual:@"connected"] || [type isEqual:@"disconnected"]))
    {
        if (!_registeredForNotifs) {
            _registeredForNotifs = YES;
            TiThreadPerformOnMainThread(^{
                [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(accessoryDidDisconnect:) name:EAAccessoryDidDisconnectNotification object:nil];
                [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(accessoryDidConnect:) name:EAAccessoryDidConnectNotification object:nil];
                [[ EAAccessoryManager sharedAccessoryManager] registerForLocalNotifications];
                
            }, YES);
        }
        
    }
}

-(void)_listenerRemoved:(NSString*)type count:(NSInteger)count
{
    if (count == 1 && (([type isEqual:@"connected"] && ![self _hasListeners:@"disconnected"])
                       || ([type isEqual:@"disconnected"] && ![self _hasListeners:@"connected"])))
    {
        if (_registeredForNotifs) {
            _registeredForNotifs = NO;
            TiThreadPerformOnMainThread(^{
                [[NSNotificationCenter defaultCenter] removeObserver:self];
                [[ EAAccessoryManager sharedAccessoryManager] unregisterForLocalNotifications];
            }, YES);
        }
    }
}

- (void)accessoryDidConnect:(NSNotification *)notification
{
    
    EAAccessory * connectedAccessory = [[notification userInfo] objectForKey:EAAccessoryKey];
    if(connectedAccessory == nil)
        return;
    if (_pairing) {
        _pairingAccessory = [connectedAccessory retain];
    }
    [self fireEvent:@"connected" withObject:[self dictFromAccessory:connectedAccessory]];
}


- (void)accessoryDidDisconnect:(NSNotification *)notification
{
    EAAccessory * disconnectedAccessory = [[notification userInfo] objectForKey:EAAccessoryKey];
    if(disconnectedAccessory == nil)
        return;
    [self fireEvent:@"disconnected" withObject:[self dictFromAccessory:disconnectedAccessory]];

}

-(void)pairDevice:(id)args
{
    if (_pairing) {
        return;
    }
    ENSURE_SINGLE_ARG_OR_NIL(args, NSDictionary)
    NSString* predicateString = [TiUtils stringValue:@"predicate" properties:args];
    if (args) {
        KrollCallback* successCallback = [args valueForKey:@"success"];
        ENSURE_TYPE_OR_NIL(successCallback, KrollCallback);
        KrollCallback* errorCallback = [args valueForKey:@"error"];
        ENSURE_TYPE_OR_NIL(errorCallback, KrollCallback);
    }
    NSPredicate* predicated = nil;
    if (predicateString) {
        predicated = [NSPredicate predicateWithFormat:predicateString];
    }
    TiThreadPerformOnMainThread(^{
        if (!_registeredForNotifs) {
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(accessoryDidConnect:) name:EAAccessoryDidConnectNotification object:nil];
            [[ EAAccessoryManager sharedAccessoryManager] registerForLocalNotifications];
        }
        _pairing = YES;
        [[ EAAccessoryManager sharedAccessoryManager] showBluetoothAccessoryPickerWithNameFilter:predicated completion:^(NSError *error) {
            if (!_registeredForNotifs) {
                [[NSNotificationCenter defaultCenter] removeObserver:self];
                [[ EAAccessoryManager sharedAccessoryManager] unregisterForLocalNotifications];
            }
            if (error != nil) {
                [self _fireEventToListener:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]] listener:[args valueForKey:@"error"] thisObject:nil];
            } else {
                [self _fireEventToListener:@"success" withObject:_pairingAccessory?@{@"device":[self dictFromAccessory:_pairingAccessory]}:@{} listener:[args valueForKey:@"success"] thisObject:nil];
            }
            RELEASE_TO_NIL(_pairingAccessory)
            _pairing = NO;
        }];
    }, YES);
}

-(void)enableBluetooth:(id)args
{
    //for android compat
}

-(void)disableBluetooth:(id)args
{
    //for android compat
}

-(id)supported
{
    //for android compat
    return NUMBOOL(YES);
}

-(id)enabled
{
    //for android compat
    return NUMBOOL(YES);
}


-(void)discover:(id)args
{
    //for android compat
}

-(void)stopDiscover:(id)args
{
    //for android compat
}

-(void)unpairDevice:(id)args
{
    //for android compat
}

- (id)getConnectedBLEDevices:(id)args {
    // Get list of connected accessories
    NSArray *accList = [[EAAccessoryManager sharedAccessoryManager] connectedAccessories];
    NSMutableArray* accs = [NSMutableArray arrayWithCapacity:[accList count]];
    [accList enumerateObjectsUsingBlock:^(EAAccessory* obj, NSUInteger idx, BOOL *stop) {
        [accs addObject:[self dictFromAccessory:obj]];
    }];
    return accs;
}

-(void)discoverBLE:(id)args
{
    if (_discovering) {
        return;
    }
    _discovering = YES;
    ENSURE_SINGLE_ARG_OR_NIL(args, KrollCallback);
    RELEASE_TO_NIL(_discoveryCallback)
    _discoveryCallback = [args retain];
    [[self btManager] scanForPeripheralsWithServices:@[UARTPeripheral.uartServiceUUID] options:@{CBCentralManagerScanOptionAllowDuplicatesKey: [NSNumber numberWithBool:NO]}];
//    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(stopDiscoverBLE:) object:nil];
// Delay execution of my block for 10 seconds.
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 10 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self stopDiscoverBLE:nil];
    });
//    [self performSelector:@selector(stopDiscoverBLE:) withObject:nil afterDelay:10];
}

-(void)stopDiscoverBLE:(id)args
{
    if (!_discovering) {
        return;
    }
//    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(stopDiscoverBLE:) object:nil];
    _discovering = NO;
    if (_discoveryCallback) {
        NSMutableArray* result = [NSMutableArray arrayWithCapacity:[_discoveredBLEDevices count]];
        [_discoveredBLEDevices enumerateKeysAndObjectsUsingBlock:^(id key, CBPeripheral* obj, BOOL *stop) {
            [result addObject:[self dictFromPeripheral:obj]];
        }];
        [self _fireEventToListener:@"discovery" withObject:result listener:_discoveryCallback thisObject:nil];
        RELEASE_TO_NIL(_discoveryCallback)
    }
    [_discoveredBLEDevices removeAllObjects];
    [[self btManager] stopScan];

}

-(void)connectBLEDevice:(CBPeripheral*)peripheral {
    [[self btManager] connectPeripheral:peripheral options:@{CBConnectPeripheralOptionNotifyOnDisconnectionKey: [NSNumber numberWithBool:YES]}];

}

-(void)disconnectBLEDevice:(CBPeripheral*)peripheral {
    [[self btManager] cancelPeripheralConnection:peripheral];
}


+(void)connectBLEDevice:(CBPeripheral*)peripheral {
    [[self sharedInstance] connectBLEDevice:peripheral];
}

+(void)disconnectBLEDevice:(CBPeripheral*)peripheral {
    [[self sharedInstance] disconnectBLEDevice:peripheral];
}

- (void) centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI
{
    if (_discovering) {
        [self addDiscoveredBLEDevice:peripheral];
        if ([self _hasListeners:@"found"]) {
            [self fireEvent:@"found" withObject:@{
                                                  @"discovering":@(_discovering),
                                                  @"device":[self dictFromPeripheral:peripheral],
                                                  @"rssi":RSSI} propagate:NO checkForListener:NO];
        }
    }
}

- (void) centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral
{
    [peripheral didConnect];
    if ([self _hasListeners:@"connected"]) {
        [self fireEvent:@"connected" withObject:@{
                                              @"device":[self dictFromPeripheral:peripheral]
                                              } propagate:NO checkForListener:NO];
    }
}

- (void) centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    [peripheral didDisconnect];
    if ([self _hasListeners:@"disconnected"]) {
        [self fireEvent:@"disconnected" withObject:@{
                                                  @"device":[self dictFromPeripheral:peripheral]
                                                  } propagate:NO checkForListener:NO];
    }
}

- (void) centralManagerDidUpdateState:(CBCentralManager *)central
{
    BOOL _oldValue = _enabled;
    _enabled = [[self btManager] state] == CBCentralManagerStatePoweredOn;
    if (_oldValue == _enabled)
    {
        if ([self _hasListeners:@"change"]) {
            [self fireEvent:@"change" withObject:@{
                                                   @"enabled":@(_enabled)} propagate:NO checkForListener:NO];
        }
    }
}

@end
