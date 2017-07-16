/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import "AkylasBluetoothBLEDeviceProxy.h"
#import "TiUtils.h"
#import "TiBlob.h"
#import "AkylasBluetoothModule.h"
#import <objc/runtime.h>



//@implementation CBPeripheral (AkylasBluetoothBLEDeviceProxy)
////NSString * const kAkylasBTProxy = @"kAkylasBTProxy";
//
//
////- (void)setProxy:(AkylasBluetoothBLEDeviceProxy *)proxy
////{
////    objc_setAssociatedObject(self, kAkylasBTProxy, proxy, OBJC_ASSOCIATION_ASSIGN);
////}
////
////- (AkylasBluetoothBLEDeviceProxy*)proxy
////{
////    return objc_getAssociatedObject(self, kAkylasBTProxy);
////}
//
//-(void)didConnect
//{
//    NSLog(@"didConnect1 %@, %@, %@", self, [self.identifier UUIDString], [self proxy])
//    [[peripheralMapping objectForKey:self.identifier] didConnect];
//}
//-(void)didDisconnect
//{
//    [[peripheralMapping objectForKey:self.identifier] didDisconnect];
//}
//
//@end

@implementation AkylasBluetoothBLEDeviceProxy
{
    CBPeripheral *_peripheral;
    NSString* _identifier;
//    KrollCallback * _readRSSICallback;
    
    CBService *uartService;
    CBCharacteristic *rxCharacteristic;
    CBCharacteristic *txCharacteristic;
}
+ (CBUUID *) uartServiceUUID
{
    return [CBUUID UUIDWithString:@"6e400001-b5a3-f393-e0a9-e50e24dcca9e"];
}

+ (CBUUID *) txCharacteristicUUID
{
    return [CBUUID UUIDWithString:@"6e400002-b5a3-f393-e0a9-e50e24dcca9e"];
}

+ (CBUUID *) rxCharacteristicUUID
{
    return [CBUUID UUIDWithString:@"6e400003-b5a3-f393-e0a9-e50e24dcca9e"];
}

//+ (CBUUID *) deviceInformationServiceUUID
//{
//    return [CBUUID UUIDWithString:@"180A"];
//}
//
//+ (CBUUID *) hardwareRevisionStringUUID
//{
//    return [CBUUID UUIDWithString:@"2A27"];
//}


- (void)dealloc {
//    RELEASE_TO_NIL(_readRSSICallback)
    RELEASE_TO_NIL(uartService)
    RELEASE_TO_NIL(rxCharacteristic)
    RELEASE_TO_NIL(txCharacteristic)
    [self disconnect:nil];
    if (_peripheral) {
        [AkylasBluetoothModule removePeripheralMapping:_peripheral];
        RELEASE_TO_NIL(_peripheral)
    }
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Bluetooth.BLEDevice";
}

-(void)_initWithProperties:(NSDictionary *)properties
{
    self.uartMode = false;
    id arg = [properties valueForKey:@"id"];
    
    if (IS_NULL_OR_NIL(arg)) {
        [self throwException:@"Invalid argument passed to protocol property" subreason:@"You must pass a protocol String" location:CODELOCATION];
    }
    _identifier = [[TiUtils stringValue:arg] retain];
    [super _initWithProperties:properties];
    [self setParentForBubbling:[AkylasBluetoothModule sharedInstance]];
}

-(NSString*)id {
    return _identifier;
}

-(CBPeripheral*)peripheral {
    if (!_peripheral && _identifier) {
        NSUUID* uuid = [[NSUUID alloc] initWithUUIDString:_identifier];
        NSArray* devices = [[AkylasBluetoothModule btManager] retrievePeripheralsWithIdentifiers:@[uuid]];
        if ([devices count] > 0) {
            _peripheral = [[devices objectAtIndex:0] retain];
            [AkylasBluetoothModule addPeripheralMapping:self forPeripheral:_peripheral];
//            [_peripheral setProxy:self];
            _peripheral.delegate = self;
        }
        [uuid release];
    }
    return _peripheral;
}

- (BOOL) isConnected {
    return [[self peripheral] state] == CBPeripheralStateConnected;
}

-(void)didConnect
{
//    [_peripheral didConnect];
    NSLog(@"didConnect")
    if (self.uartMode) {
       [[self peripheral] discoverServices:@[self.class.uartServiceUUID]];
    } else {
        [self fireEvent:@"connected"];
    }
}
-(void)didDisconnect
{
    [self fireEvent:@"disconnected"];
    NSLog(@"didDisconnect")
//    [_peripheral didDisconnect];
    [AkylasBluetoothModule removePeripheralMapping:_peripheral];
//    [[self peripheral] setProxy:nil];
    RELEASE_TO_NIL(_peripheral)
}

-(void)discoverServices:(id)args
{
    //    if ([self isConnected])
    //    {
    //        return;
    //    }
    [[self peripheral] discoverServices:nil];
}

-(void)discoverService:(id)args
{
    ENSURE_SINGLE_ARG(args, NSString)

    [[self peripheral] discoverServices:@[[AkylasBluetoothBLEDeviceProxy uuidFromString:[TiUtils stringValue:args]]]];
}

-(void)discoverCharacteristics:(id)args
{
    ENSURE_ARG_COUNT(args,2);
    CBService* service = [self serviceFromUUID:[args objectAtIndex:0]];
     [[self peripheral] discoverCharacteristics:@[[AkylasBluetoothBLEDeviceProxy uuidFromString:[TiUtils stringValue:[args objectAtIndex:1]]]] forService:service];
}



-(void)connect:(id)args
{
//    if ([self isConnected])
//    {
//        return;
//    }
    [AkylasBluetoothModule connectBLEDevice:[self peripheral]];
}

-(void)disconnect:(id)args
{
    if (!_peripheral || ![self isConnected])
    {
        return;
    }
    [AkylasBluetoothModule disconnectBLEDevice:[self peripheral]];
}

-(id)paired
{
    return NUMBOOL(YES);
}

// This is called when we get an incoming data event. Notify the appDelegate that we have data to print.
//- (void)handleIncoming:(NSInputStream*)stream {
//    double timestamp = [[NSDate date] timeIntervalSince1970]*1000;
//    NSError* error = nil;
//    NSData* result = [self dataWithContentsOfStream:stream initialCapacity:DATA_CHUNK_SIZE error:&error];
//    if (error) {
//        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]];
//    } else if (result.length > 0) {
//        [self fireEvent:@"read" withObject:@{
//                                             @"timestamp":NUMDOUBLE(timestamp),
//                                             @"length":NUMUINTEGER(result.length),
//                                             @"data":[[[TiBlob alloc] initWithData:result mimetype:@"application/octet-stream"] autorelease]}];
//    }
//    //        unsigned char buf[DATA_CHUNK_SIZE];
//    //        NSUInteger len;
//    //        len = [stream read:buf maxLength:DATA_CHUNK_SIZE];
//    //        if(len>0) {
//    //
//    //        }
//}

-(void)writeData:(NSData*)data toCharacteristic:(CBCharacteristic*)characteristic
{
    if (!characteristic || !data) {
        return;
    }
    NSInteger writeType;
    if ((characteristic.properties & CBCharacteristicPropertyWriteWithoutResponse) != 0)
    {
        writeType = CBCharacteristicWriteWithoutResponse;
//        [_peripheral writeValue:data forCharacteristic:characteristic type:CBCharacteristicWriteWithoutResponse];
    }
    else if ((characteristic.properties & CBCharacteristicPropertyWrite) != 0)
    {
        writeType = CBCharacteristicWriteWithResponse;
//        [_peripheral writeValue:data forCharacteristic:characteristic type:CBCharacteristicWriteWithResponse];
    } else {
        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:0 message:@"Unable to write data without characteristic write property"]];
        return;
    }
    
    //send data in lengths of <= 20 bytes
//    NSUInteger dataLength = data.length;
//    NSUInteger limit = 20;
//    
//    //Below limit, send as-is
//    if (dataLength <= limit) {
        [_peripheral writeValue:data forCharacteristic:characteristic type:writeType];
//    }
    
    //Above limit, send in lengths <= 20 bytes
//    else {
//        
//        NSUInteger len = limit;
//        NSUInteger loc = 0;
//        NSUInteger idx = 0; //for debug
//        
//        while (loc < dataLength) {
//            
//            NSUInteger rmdr = dataLength - loc;
//            if (rmdr <= len) {
//                len = rmdr;
//            }
//            
//            NSRange range = NSMakeRange(loc, len);
//            NSInteger newBytes[len];
//            [data getBytes:newBytes range:range];
//            NSData* newData = [NSData dataWithBytes:newBytes length:len];
//            [_peripheral writeValue:newData forCharacteristic:characteristic type:writeType];
//            loc += len;
//            idx += 1;
//        }
//    }
}

-(void)writeToCharacteristic:(id)args
{
    CBCharacteristic* characteristic = [self internalGetCharacteristic:args];
    
    if (!characteristic)
    {
        return;
    }
    ENSURE_ARG_COUNT(args,3);
    NSData* data = [self dataFromArgs:[args objectAtIndex:2]];
    if (!data)
    {
        return;
    }
    [self writeData:data toCharacteristic:characteristic];
}

-(NSData*)dataFromArgs:(id)args
{
//    ENSURE_SINGLE_ARG(args, NSObject)
    if (IS_OF_CLASS(args, NSString)) {
        // called within this class
        return [args dataUsingEncoding:NSUTF8StringEncoding];
    }else if (IS_OF_CLASS(args, NSArray) || IS_OF_CLASS(args, NSMutableArray)) {
        NSMutableData *data = [[NSMutableData alloc] initWithCapacity: [args count]];
        for( id obj in args) {
            char byte = (char)[TiUtils intValue:obj];
            [data appendBytes: &byte length: 1];
        }
        return [data autorelease];
//        return [NSKeyedArchiver archivedDataWithRootObject:args];
    } else if ([args respondsToSelector:@selector(data)]) {
       return [args data];
    }
}
-(void)send:(id)args
{
    if (self.uartMode && txCharacteristic) {
        ENSURE_SINGLE_ARG(args, NSObject)
        NSData* data = [self dataFromArgs:args];
        [self writeData:data toCharacteristic:txCharacteristic];
    }
}

-(void)readRSSI:(id)args
{
//    ENSURE_SINGLE_ARG_OR_NIL(args, KrollCallback);
//
//    RELEASE_TO_NIL(_readRSSICallback)
//    _readRSSICallback = [(KrollCallback*)args retain];
    if (_peripheral) {
        [[self peripheral] readRSSI];
    }
    //    [self performSelector:@selector(stopDiscoverBLE:) withObject:nil afterDelay:10];
}

+(CBUUID*)uuidFromString:(NSString*)uuidString {
//    if ([uuidString length] == 4) {
//        return [CBUUID UUIDWithString:[NSString stringWithFormat:@"0000%@-0000-1000-8000-00805f9b34fb", uuidString]];
//    }
    return [CBUUID UUIDWithString:uuidString];
}


-(CBService*) serviceFromUUID:(NSString *)uuidString
{
    if (uuidString == nil || !_peripheral)
    {
        return nil;
    }
    CBPeripheral* peri = [self peripheral];
    if (peri.services == nil)
    {
        return nil;
    }
    
    CBUUID* uuid = [AkylasBluetoothBLEDeviceProxy uuidFromString:uuidString];
    
    if (uuid == nil)
    {
        return nil;
    }
    
    for (CBService* item in peri.services)
    {
//        NSLog(@"test %@", item.UUID.UUIDString);
        if ([item.UUID isEqual: uuid])
        {
            return item;
        }
    }
    
    return nil;
}

-(CBCharacteristic*) getCharacteristic:(NSString *)uuidString forService:(CBService*) service
{
    if (service.characteristics == nil)
    {
        return nil;
    }
    
    if (uuidString == nil)
    {
        return nil;
    }
    
    if (![uuidString isKindOfClass:[NSString class]])
    {
        return nil;
    }
    
    CBUUID* uuid = [AkylasBluetoothBLEDeviceProxy uuidFromString:uuidString];
    
    if (uuid == nil)
    {
        return nil;
    }
    
    for (CBCharacteristic* item in service.characteristics)
    {
        if ([item.UUID isEqual: uuid])
        {
            return item;
        }
    }
    
    return nil;
}

-(CBDescriptor*) getDescriptor:(NSString *)uuidString forCharacteristic:(CBCharacteristic*) characteristic
{
    if (characteristic.descriptors == nil)
    {
        return nil;
    }
    
    if (uuidString == nil)
    {
        return nil;
    }
    
    if (![uuidString isKindOfClass:[NSString class]])
    {
        return nil;
    }
    
    CBUUID* uuid = [AkylasBluetoothBLEDeviceProxy uuidFromString:uuidString];
    
    if (uuid == nil)
    {
        return nil;
    }
    
    for (CBDescriptor* item in characteristic.descriptors)
    {
        if ([item.UUID isEqual: uuid])
        {
            return item;
        }
    }
    return nil;
}

-(CBCharacteristic*)internalGetCharacteristic:(id)args
{
    if (!_peripheral)
    {
        return nil;
    }
    ENSURE_ARG_COUNT(args,2);
    
    CBService* service = [self serviceFromUUID:[args objectAtIndex:0]];
    
    if (!service)
    {
        return nil;
    }
    
    CBCharacteristic* characteristic = [self getCharacteristic:[args objectAtIndex:1] forService:service];
    
    return characteristic;
}

- (void)startCharacteristicNotifications:(id)args
{
    
    CBCharacteristic* characteristic = [self internalGetCharacteristic:args];
    
    if (!characteristic)
    {
        return;
    }
    [[self peripheral] setNotifyValue:YES forCharacteristic:characteristic];
}

- (void)stopCharacteristicNotifications:(id)args
{
    CBCharacteristic* characteristic = [self internalGetCharacteristic:args];
    
    if (!characteristic)
    {
        return;
    }
    [[self peripheral] setNotifyValue:NO forCharacteristic:characteristic];
}

-(void)requestMTU:(id)args
{
    //not working on ios
}

- (void)readCharacteristicValue:(id)args
{
    CBCharacteristic* characteristic = [self internalGetCharacteristic:args];
    
    if (!characteristic)
    {
        return;
    }
    [[self peripheral] readValueForCharacteristic:characteristic];
}

-(void)fireDataEvent:(NSString*)type withData:(NSData *)data  extraData:(NSDictionary*)extraData {
    if ([self _hasListeners:type]) {
        double timestamp = [[NSDate date] timeIntervalSince1970]*1000;
        NSMutableDictionary* eventDict = [NSMutableDictionary dictionaryWithDictionary:@{
                                                                                   @"timestamp":NUMDOUBLE(timestamp),
                                                                                   @"length":NUMUINTEGER([data length]),
                                                                                   @"data":[[[TiBlob alloc] initWithData:data mimetype:@"application/octet-stream"] autorelease]}];
        if (extraData) {
            [eventDict addEntriesFromDictionary:extraData];
        }
        [self fireEvent:type withObject:eventDict];
    }
}


- (void)peripheralDidUpdateName:(CBPeripheral *)peripheral{
    if ([self _hasListeners:@"name"]) {
        [self replaceValue:peripheral.name forKey:@"name" notification:NO];
        [self fireEvent:@"name" withObject:@{
                                             @"name":peripheral.name}];
    }
}

/*!
 *  @method peripheralDidUpdateRSSI:error:
 *
 *  @param peripheral	The peripheral providing this update.
 *	@param error		If an error occurred, the cause of the failure.
 *
 *  @discussion			This method returns the result of a @link readRSSI: @/link call.
 *
 *  @deprecated			Use {@link peripheral:didReadRSSI:error:} instead.
 */
- (void)peripheralDidUpdateRSSI:(CBPeripheral *)peripheral error:(NSError *)error {
    if (![TiUtils isIOS8OrGreater]) {
        [self peripheral:peripheral didReadRSSI:peripheral.RSSI error:error];
    }
    
}

/*!
 *  @method peripheral:didReadRSSI:error:
 *
 *  @param peripheral	The peripheral providing this update.
 *  @param RSSI			The current RSSI of the link.
 *  @param error		If an error occurred, the cause of the failure.
 *
 *  @discussion			This method returns the result of a @link readRSSI: @/link call.
 */
- (void)peripheral:(CBPeripheral *)peripheral didReadRSSI:(NSNumber *)RSSI error:(NSError *)error {
    if (error) {
        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]];
    } else if ([self _hasListeners:@"change"]) {
        [self fireEvent:@"change" withObject:@{@"rssi":RSSI}];
    }
}
- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForDescriptor:(CBDescriptor *)descriptor error:(NSError *)error {
    
}

- (void) peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
   if (error)
    {
        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]];
        return;
    }
//    uartMode = false;
//    if (uartMode) {
    NSMutableArray* servicesIds = [NSMutableArray array];
        for (CBService *s in [peripheral services])
        {
            [servicesIds addObject:s.UUID.UUIDString];
            DebugLog(@"didDiscoverService %@", s.UUID.UUIDString);
            if ([s.UUID isEqual:self.class.uartServiceUUID])
            {
//                uartMode = true;
                uartService = [s retain];
                
                [peripheral discoverCharacteristics:@[self.class.txCharacteristicUUID, self.class.rxCharacteristicUUID] forService:uartService];
            }
//            else if ([s.UUID isEqual:self.class.deviceInformationServiceUUID])
//            {
//                [peripheral discoverCharacteristics:nil forService:s];
//            }
        }
//    }
    if ([self _hasListeners:@"discoveredServices"]) {
        [self fireEvent:@"discoveredServices" withObject:@{@"services":servicesIds}];
    }
//    if (!uartMode) {
//        [self fireEvent:@"connected"];
//    }
}

- (void) peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    if (error)
    {
        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]];
        return;
    }
    BOOL isUartService = (self.uartMode && [service.UUID isEqual:self.class.uartServiceUUID]);
    
    NSMutableArray* characteristicsIds = [NSMutableArray array];
    for (CBCharacteristic *c in [service characteristics])
    {
//        DebugLog(@"didDiscoverCharacteristic %@", c.UUID.UUIDString);
       [characteristicsIds addObject:c.UUID.UUIDString];
        if (isUartService) {
            if ([c.UUID isEqual:self.class.rxCharacteristicUUID])
            {
                rxCharacteristic = [c retain];
                [self.peripheral setNotifyValue:YES forCharacteristic:rxCharacteristic];
            }
            else if ([c.UUID isEqual:self.class.txCharacteristicUUID])
            {
                txCharacteristic = [c retain];
            }
        }
    }
//    DebugLog(@"didDiscoverCharacteristics %@, %@, %@", isUartService?@"1":@"0" , rxCharacteristic, txCharacteristic);
    if (isUartService && rxCharacteristic && txCharacteristic) {
        [self fireEvent:@"connected"];
    }
    if ([self _hasListeners:@"discoveredCharacteristics"]) {
        [self fireEvent:@"discoveredCharacteristics" withObject:@{
                                                                  @"service":service.UUID.UUIDString,
                                                                  @"characteristics":characteristicsIds
                                                                  }];
    }
}

- (void) peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if (error)
    {
        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]];
        return;
    }
    
    [self fireDataEvent:@"read" withData:characteristic.value extraData:@{
                                                                          @"notify":@(characteristic.isNotifying),
                                                                          @"characteristic":characteristic.UUID.UUIDString,
                                                                          @"service":characteristic.service.UUID.UUIDString
                                                                          }];
}
- (void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(nullable NSError *)error
{
    [self fireEvent:@"didWrite" withObject:error?[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]:nil];
//    if (error)
//    {
//        [self fireEvent:@"error" withObject:[TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]]];
//    }
}
@end
