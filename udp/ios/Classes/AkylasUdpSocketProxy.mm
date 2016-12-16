/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "AkylasUdpSocketProxy.h"
#import "GCDAsyncUdpSocket.h"
#import "TiUtils.h"
#import <arpa/inet.h>
#import <fcntl.h>
#import <ifaddrs.h>
#import <netdb.h>
#import <net/if.h>



@implementation AkylasUdpSocketProxy
{
    GCDAsyncUdpSocket* socket;
    //    bool isServer;
    bool listening;
//    NSString* _address;
//    NSData* _hostAddress;
    NSUInteger _port;
//    CFHostRef _cfHost;
//    CFSocketRef _cfSocket;
    NSInteger maxPacketSize;
    NSInteger readTimeout;
    NSInteger writeTimeout;
//    CFRunLoopSourceRef rls;
//    BOOL reuseAddress;
}

#pragma mark Initialization and Deinitialization

-(id)init
{
    if ((self = [super init]))
    {
        socket = [[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
        maxPacketSize = 1024;
        readTimeout = -1;
        listening = false;
    }
    return self;
}

-(void)_initWithProperties:(NSDictionary *)properties
{
    if ([properties objectForKey:@"maxPacketSize"])
    {
        maxPacketSize = [[properties objectForKey:@"maxPacketSize"] intValue];
    }
    [super _initWithProperties:properties];
}

-(void)dealloc
{
    [self stop:nil];
    RELEASE_TO_NIL(socket)
    [super dealloc];
}

-(void)_listenerAdded:(NSString *)type count:(NSInteger)count
{
    if (count == 1 && socket && ([type isEqualToString:@"listening"] || [type isEqualToString:@"message"]))
    {
        [self startListening];
    }
}

-(void)_listenerRemoved:(NSString *)type count:(NSInteger)count
{
    if (count == 0 && socket)
    {
        if (([type isEqualToString:@"listening"] && ![self _hasListeners:@"message"]) ||
            ([type isEqualToString:@"message"] && ![self _hasListeners:@"listening"]))
        {
            [self stopListening];
        }
    }
}

#pragma mark Public API

-(void)bind:(id)args
{
    ENSURE_TYPE(args, NSArray)
    NSNumber *portArg = nil;
    KrollCallback* callback = nil;
    NSString* address = nil;
    ENSURE_ARG_AT_INDEX(portArg, args, 0, NSNumber);
    ENSURE_ARG_OR_NIL_AT_INDEX(address, args, 1, NSString);
    ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 2, KrollCallback);
    
    if (callback) {
        [self addEventListener:callback forEventType:@"listening"];
    }
    _port = [TiUtils intValue:portArg];
    
    NSError *error = nil;
    if (address) {
        [socket bindToPort:_port interface:address error:&error];
    } else {
        [socket bindToPort:_port error:&error];
    }
    
    if (error) //check ff of dit werkt!
    {
        [self fireError:error];
        return;
    }
    BOOL shouldListen = [self _hasListeners:@"message"] || [self _hasListeners:@"listening"];
    if (shouldListen) [self startListening];
}

-(void)stop:(id)args
{
    if (socket) {
        [socket close];
    }
    listening = false;
}

-(void)close:(id)args
{
    [self stop:args];
}

-(void)setBroadcast:(id)args
{
    ENSURE_SINGLE_ARG(args, NSNumber);
    if (self->socket) {
        NSError* error = nil;
        [self->socket enableBroadcast:[args boolValue] error:&error];
        if (error) {
            [self fireError:error];
        }
    }
}

//-(void)setReuseAddress:(id)args
//{
//    ENSURE_SINGLE_ARG(args, NSNumber);
//    reuseAddress = [args boolValue];
//    if (self->_cfSocket != NULL)
//    {
//        int existingValue = reuseAddress? 1 : 0;
//        setsockopt( CFSocketGetNative(self->_cfSocket),
//                   SOL_SOCKET, SO_REUSEADDR, (void *)&existingValue,
//                   sizeof(existingValue));
//    }
//}

-(void)setTimeout:(id)args
{
    readTimeout = [TiUtils intValue:args];
    writeTimeout = readTimeout;
}

//-(void)setKeepAlive:(id)args
//{
//    ENSURE_SINGLE_ARG(args, NSNumber);
//    if (self->_cfSocket != NULL)
//    {
//        int existingValue = [args boolValue]? 1 : 0;
//        setsockopt( CFSocketGetNative(self->_cfSocket),
//                   SOL_SOCKET, SO_KEEPALIVE, (void *)&existingValue,
//                   sizeof(existingValue));
//    }
//}

#pragma mark Private Utility Methods

-(NSData*)dataFromArg:(id)arg {
    if (([arg respondsToSelector:@selector(data)])) {
        return [arg data];
    } else if (IS_OF_CLASS(arg, NSString)) {
        return [arg dataUsingEncoding:NSUTF8StringEncoding];
    } else if (IS_OF_CLASS(arg, NSArray) || IS_OF_CLASS(arg, NSMutableArray)) {
        NSMutableData *theBufferData = [[[NSMutableData alloc] initWithCapacity: [arg count]] autorelease];
        for( NSString *string in arg) {
            char byte = (char)[string intValue];
            [theBufferData appendBytes: &byte length: 1];
        }
        return theBufferData;
    }
    return nil;
}

-(void)send:(id)args
{
    if (!socket) {
        [self fireStringError:@"can't send before socket is bound"];
        return;
    }
    ENSURE_TYPE(args, NSArray)
    NSObject *dataArg = nil;
    NSNumber *portArg = nil;
    NSString *address = nil;
    ENSURE_ARG_AT_INDEX(dataArg, args, 0, NSObject);
    ENSURE_ARG_OR_NIL_AT_INDEX(portArg, args, 1, NSNumber);
    ENSURE_ARG_OR_NIL_AT_INDEX(address, args, 2, NSString);
    
    NSData* data  = [self dataFromArg:dataArg];
    if (!data) {
        [self fireStringError:@"no data to send"];
        return;
    }
    NSInteger port = [TiUtils intValue:portArg def:_port];
    [socket sendData:data toHost:address port:port withTimeout:writeTimeout tag:0];
}

// Stops the object, reporting the supplied error to the delegate.
-(void)_stopWithError:(NSError*)error
{
    [self stop:nil];
    [self fireError:error];
}

-(void)fireError:(NSError*)error {
    [self fireStringError:[error localizedDescription]];
}
-(void)fireStringError:(NSString*)error {
    [self fireEvent:@"error" withObject:[NSDictionary dictionaryWithObjectsAndKeys:error, @"error", nil]];
}

-(void)startListening {
    if (listening) {
        return;
    }
    NSError* error = nil;
    [socket beginReceiving:&error];
    if (error) {
        [self fireError:error];
    } else {
        [self fireEvent:@"listening"];
        listening = true;
    }
}

-(void)stopListening {
    if (!listening) {
        return;
    }
    listening = false;
    [socket pauseReceiving];
}

#pragma Delegates

- (void)udpSocket:(GCDAsyncUdpSocket *)sock didConnectToAddress:(NSData *)address{}


- (void)udpSocket:(GCDAsyncUdpSocket *)sock didNotConnect:(NSError * _Nullable)error
{
    [self fireError:error];
}


//- (void)udpSocket:(GCDAsyncUdpSocket *)sock didSendDataWithTag:(long)tag{}


- (void)udpSocket:(GCDAsyncUdpSocket *)sock didNotSendDataWithTag:(long)tag dueToError:(NSError * _Nullable)error
{
    [self fireError:error];
}


- (void)udpSocket:(GCDAsyncUdpSocket *)sock didReceiveData:(NSData *)data
      fromAddress:(NSData *)address
withFilterContext:(nullable id)filterContext
{
//    [self fireCallback:@"message" withArg:@[
//                                            [[[TiBlob alloc] _initWithPageContext:[self pageContext] andData:data mimetype:@"application/octet-stream"] autorelease],
//                                            @{
//                                                @"data":[[[TiBlob alloc] _initWithPageContext:[self pageContext] andData:data mimetype:@"application/octet-stream"] autorelease],
//                                                @"address":[GCDAsyncUdpSocket hostFromAddress:address],
//                                                @"port":@([GCDAsyncUdpSocket portFromAddress:address])
//                                                }] withSource:this];
    [self fireEvent:@"message" withObject:@{
                                                @"data":[[[TiBlob alloc] _initWithPageContext:[self pageContext] andData:data mimetype:@"application/octet-stream"] autorelease],
                                                @"address":[GCDAsyncUdpSocket hostFromAddress:address],
                                                @"port":@([GCDAsyncUdpSocket portFromAddress:address])
                                                }];
}


- (void)udpSocketDidClose:(GCDAsyncUdpSocket *)sock withError:(NSError  * _Nullable)error
{
    [self fireEvent:@"close"];
}




@end
