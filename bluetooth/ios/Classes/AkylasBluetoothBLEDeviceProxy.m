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

@implementation CBPeripheral (AkylasBluetoothBLEDeviceProxy)
NSString * const kAkylasBTProxy = @"kAkylasBTProxy";


- (void)setProxy:(AkylasBluetoothBLEDeviceProxy *)proxy
{
    objc_setAssociatedObject(self, kAkylasBTProxy, proxy, OBJC_ASSOCIATION_ASSIGN);
    if (proxy != nil) {
    }
}

- (AkylasBluetoothBLEDeviceProxy*)proxy
{
    return objc_getAssociatedObject(self, kAkylasBTProxy);
}

-(void)didConnect
{
    [[self proxy] didConnect];
}
-(void)didDisconnect
{
    [[self proxy] didDisconnect];
}

@end

@implementation AkylasBluetoothBLEDeviceProxy
{
    UARTPeripheral *_peripheral;
    NSString* _identifier;
    NSString* _hwRevision;
}

-(NSString*)apiName
{
    return @"Akylas.Bluetooth.BLEDevice";
}

-(void)_initWithProperties:(NSDictionary *)properties
{
    id arg = [properties valueForKey:@"identifier"];
    
    if (IS_NULL_OR_NIL(arg)) {
        [self throwException:@"Invalid argument passed to protocol property" subreason:@"You must pass a protocol String" location:CODELOCATION];
    }
    _identifier = [[TiUtils stringValue:arg] retain];
    [super _initWithProperties:properties];
}

-(NSString*)identifier {
    return _identifier;
}

-(UARTPeripheral*)getUARTPeripheral {
    if (!_peripheral && _identifier) {
        NSUUID* uuid = [[NSUUID alloc] initWithUUIDString:_identifier];
        NSArray* devices = [[AkylasBluetoothModule btManager] retrievePeripheralsWithIdentifiers:@[uuid]];
        if ([devices count] > 0) {
            _peripheral = [[UARTPeripheral alloc] initWithPeripheral:[devices objectAtIndex:0] delegate:self];
            [[_peripheral peripheral] setProxy:self];
        }
        [uuid release];
    }
    return _peripheral;
}

-(CBPeripheral*)peripheral {
    return [[self getUARTPeripheral] peripheral];
}

- (BOOL) isConnected {
    return [[self peripheral] state] == CBPeripheralStateConnected;
}

-(void)didConnect
{
    [self fireEvent:@"connected"];
    [_peripheral didConnect];
}
-(void)didDisconnect
{
    [self fireEvent:@"disconnected"];
    [_peripheral didDisconnect];
    [[self peripheral] setProxy:nil];
    RELEASE_TO_NIL(_peripheral)
}

-(void)connect:(id)args
{
    if ([self isConnected])
    {
        return;
    }
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

-(id)hardwareRevision
{
    return _hwRevision;
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

- (void)sendData:(NSData*)data {

    [[self getUARTPeripheral] writeRawData:data];
}

-(void)send:(id)args
{
    
    ENSURE_SINGLE_ARG(args, NSObject)
    if (IS_OF_CLASS(args, NSString)) {
        // called within this class
        [self sendData: [args dataUsingEncoding:NSUTF8StringEncoding]];
    }else if (IS_OF_CLASS(args, NSArray)) {
        //supposed to be a byte array
        [self sendData: [NSKeyedArchiver archivedDataWithRootObject:args]];
    } else if ([args respondsToSelector:@selector(data)]) {
        [self sendData:[args data]];
    }
}


- (void)dealloc {
    [self disconnect:nil];
    if (_peripheral) {
        [[self peripheral] setProxy:nil];
        RELEASE_TO_NIL(_peripheral)
    }
    [super dealloc];
}

- (void) didReadHardwareRevisionString:(NSString *)string
{
    _hwRevision = [string retain];
}

- (void) didReceiveData:(NSData *)data
{
    if ([self _hasListeners:@"read"]) {
        double timestamp = [[NSDate date] timeIntervalSince1970]*1000;
        [self fireEvent:@"read" withObject:@{
                                             @"timestamp":NUMDOUBLE(timestamp),
                                             @"length":NUMUINTEGER([data length]),
                                             @"data":[[[TiBlob alloc] initWithData:data mimetype:@"application/octet-stream"] autorelease]}];
    }
    
}

@end
