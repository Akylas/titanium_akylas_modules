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




#define PREPARE_ARRAY_ARGS(args) \
ENSURE_TYPE(args, NSArray) \
NSNumber* num = nil; \
NSInteger index = -1; \
NSObject* value = nil; \
ENSURE_ARG_OR_NIL_AT_INDEX(value, args, 0, NSObject); \
ENSURE_ARG_OR_NIL_AT_INDEX(num, args, 1, NSNumber); \
if (num) { \
index = [num integerValue]; \
} \


static AkylasBluetoothModule *_sharedInstance = nil;
@implementation AkylasBluetoothModule
{
    BOOL _registeredForNotifs;
    BOOL _pairing;
    BOOL _enabled;
    BOOL _discovering;
    EAAccessory * _pairingAccessory;
    CBCentralManager *_centralManager;
    CBPeripheralManager* _peripheralManager;
    NSMutableDictionary* _discoveredBLEDevices;
    KrollCallback * _discoveryCallback;
    dispatch_queue_t _cbQueue;
    dispatch_queue_t _cpQueue;
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
    _enabled = NO;
    _cbQueue = dispatch_queue_create("akylas.bt.cbqueue", DISPATCH_QUEUE_SERIAL);
    _cpQueue = dispatch_queue_create("akylas.bt.cpqueue", DISPATCH_QUEUE_SERIAL);
}

- (void)dealloc
{
    dispatch_release(_cbQueue);
    dispatch_release(_cpQueue);
    RELEASE_TO_NIL(_pairingAccessory)
    RELEASE_TO_NIL(_centralManager)
    RELEASE_TO_NIL(_peripheralManager)
    RELEASE_TO_NIL(_discoveryCallback)
    RELEASE_TO_NIL(_discoveredBLEDevices)
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Bluetooth";
}

-(id)createBLEDevice:(id)args
{
    id value = nil;
    ENSURE_ARG_OR_NIL_AT_INDEX(value, args, 0, NSObject);
    if (IS_OF_CLASS(value, AkylasBluetoothBLEDeviceProxy)) {
        return value;
    }
    return [[[AkylasBluetoothBLEDeviceProxy alloc] _initWithPageContext:[self executionContext] args:args] autorelease];
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
        
        _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:_cbQueue options:@{CBCentralManagerOptionShowPowerAlertKey: @([TiUtils boolValue:[self valueForUndefinedKey:@"showPowerAlert"] def:YES])}];
        _enabled = [_centralManager state] == CBCentralManagerStatePoweredOn;
        _peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:_cpQueue options:@{CBCentralManagerOptionShowPowerAlertKey: @([TiUtils boolValue:[self valueForUndefinedKey:@"showPowerAlert"] def:YES])}];
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

-(id)dictFromPeripheral:(CBPeripheral*)peripheral
{
//    if (peripheral.proxy) {
//        return peripheral.proxy;
//    }
    return @{
             @"connected" : NUMBOOL([peripheral state] == CBPeripheralStateConnected),
             @"name" : peripheral.name,
             @"identifier" : [[peripheral identifier] UUIDString]
             };
}

- (NSString *)stringFromCBUUID:(CBUUID *)cbuuid;
{
    NSData *data = [cbuuid data];
    
    NSUInteger bytesToConvert = [data length];
    const unsigned char *uuidBytes = [data bytes];
    NSMutableString *outputString = [NSMutableString stringWithCapacity:16];
    
    for (NSUInteger currentByteIndex = 0; currentByteIndex < bytesToConvert; currentByteIndex++)
    {
        switch (currentByteIndex)
        {
            case 3:
            case 5:
            case 7:
            case 9:[outputString appendFormat:@"%02x-", uuidBytes[currentByteIndex]]; break;
            default:[outputString appendFormat:@"%02x", uuidBytes[currentByteIndex]];
        }
        
    }
    
    return outputString;
}


-(NSString*)advKeyNameForKey:(NSString*)key {
    if([key isEqualToString:@"kCBAdvDataTxPowerLevel"]) {
        return @"power_level";
    }
    if([key isEqualToString:@"kCBAdvDataLocalName"]) {
        return @"local_name";
    }
    if([key isEqualToString:@"kCBAdvDataServiceUUIDs"]) {
        return @"services";
    }
    if([key isEqualToString:@"kCBAdvDataIsConnectable"]) {
        return @"connectable";
    }
    if([key isEqualToString:@"kCBAdvDataChannel"]) {
        return @"channel";
    }
    if([key isEqualToString:@"kCBAdvDataManufacturerData"]) {
        return @"manufacturer";
    }
    return key;
}
-(NSDictionary *)summarizeAdvertisement:(NSDictionary*)advertisementData
{
    DebugLog(@"[TRACE] advertisementData = %@", advertisementData);
    
    NSMutableDictionary *summary = [[NSMutableDictionary alloc] init];
    NSMutableArray *services = [[NSMutableArray alloc] init];
    
    NSArray *keys = [advertisementData allKeys];
    for (int i = 0; i < [keys count]; ++i) {
        
        id key = [keys objectAtIndex: i];
        
        NSString *keyName = [self advKeyNameForKey:(NSString *) key];
        NSObject *value = [advertisementData objectForKey: key];
        DebugLog(@"[TRACE] advertisementData handling %@=%@", keyName, value);
        
        if ([value isKindOfClass: [NSArray class]]) {
            
            NSArray *values = (NSArray *) value;
            
            for (int j = 0; j < [values count]; ++j) {
                if ([[values objectAtIndex: j] isKindOfClass: [CBUUID class]]) {
                    
                    CBUUID *uuid = [values objectAtIndex: j];
                    
                    NSString *uuidString = [self stringFromCBUUID:uuid];
                    [services addObject:uuidString];
                    
                }
                else {
                    if ([[values objectAtIndex: j] description]) {
                        [services addObject:[[values objectAtIndex: j] description]];
                    }
                }
            }
        }
        else if ([value isKindOfClass: [NSDictionary class]]) {
            NSLog(@"skipping advertised NSDictionary %@", value);
            /*
             NSDictionary *subvalues = (NSDictionary *)value;
             NSArray *subkeys = [subvalues allKeys];
             for (int i = 0; i < [subkeys count]; ++i) {
             id subkey = [keys objectAtIndex: i];
             
             NSString *subkeyName = (NSString *)subkey;
             NSObject *subvalue = [subvalues objectForKey: subkey];
             if ([[subvalues objectForKey:subkey] isKindOfClass: [CBUUID class]]) {
             CBUUID *uuid = [subvalues objectForKey:subkey];
             
             NSString *uuidString = [self stringFromCBUUID:uuid];
             [summary addObject:uuidString forKey:subkeyName];
             }
             else {
             [summary addObject:[subvalues objectForKey:subkey] forKey:subkeyName];
             }
             }
             */
        }
        else {
            NSData* data = IS_OF_CLASS(value, NSData) ? (NSData*)value : [NSKeyedArchiver archivedDataWithRootObject:value];
            if (data)  {
                [summary setObject:[[[TiBlob alloc] initWithData:data mimetype:@"application/octet-stream"] autorelease] forKey:keyName];
            }
        }
    }
    [summary setObject:services forKey:@"services"];
    [services release];
    
    DebugLog(@"[TRACE] advertisementData return %@", summary);
    return [summary autorelease];
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
    if (count == 0 && (([type isEqual:@"connected"] && ![self _hasListeners:@"disconnected"])
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
#if TARGET_IPHONE_SIMULATOR
    return NUMBOOL(NO);
#else
    return NUMBOOL(YES);
#endif
}

-(id)enabled
{
    [self btManager]; //ensure created
    return NUMBOOL(_enabled);
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

-(void)showBluetoothSettings:(id)args
{
    if ([TiUtils isIOS8OrGreater]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
    }
}

-(id)discovering {
    return @(_discovering);
}

-(void)discoverBLE:(id)args
{
    PREPARE_ARRAY_ARGS(args)
    ENSURE_TYPE_OR_NIL(value, KrollCallback);
    if (_discovering) {
        return;
    }
    
    _discovering = YES;
    RELEASE_TO_NIL(_discoveryCallback)
    _discoveryCallback = [(KrollCallback*)value retain];
    [[self btManager] scanForPeripheralsWithServices:nil options:@{CBCentralManagerScanOptionAllowDuplicatesKey: @(NO)}];
    if (num) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, [num integerValue] * NSEC_PER_MSEC), dispatch_get_main_queue(), ^{
            [self stopDiscoverBLE:nil];
        });
    }
    
}

-(void)stopDiscoverBLE:(id)args
{
    if (!_discovering) {
        return;
    }
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
        NSLog(@"found %@", peripheral.name);
        [self addDiscoveredBLEDevice:peripheral];
        if ([self _hasListeners:@"found"]) {
            [self fireEvent:@"found" withObject:@{
                                                  @"discovering":@(_discovering),
                                                  @"advertising":[self summarizeAdvertisement:advertisementData],
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
    if(error) {
        NSDictionary* data = [TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]];
        [self fireEvent:@"error" withObject:data];
        [peripheral.proxy fireEvent:@"error" withObject:data];
    }
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
    _enabled = [central state] == CBCentralManagerStatePoweredOn;
    if (_oldValue != _enabled)
    {
        if ([self _hasListeners:@"change"]) {
            [self fireEvent:@"change" withObject:@{
                                                   @"enabled":@(_enabled)} propagate:NO checkForListener:NO];
        }
    }
}
- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    
}
- (void)peripheralManager:(CBPeripheralManager *)peripheral central:(CBCentral *)central didSubscribeToCharacteristic:(CBCharacteristic *)characteristic {
}

@end
