//
//  UARTPeripheral.h
//  nRF UART
//
//  Created by Ole Morten on 1/12/13.
//  Copyright (c) 2013 Nordic Semiconductor. All rights reserved.
//


@protocol UARTPeripheralDelegate
- (void) didReceiveData:(NSData *) string;
@optional
- (void) didReadHardwareRevisionString:(NSString *) string;
- (void) didDiscoverUARTCharacteristics;
- (void) peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristic:(CBCharacteristic *)c inService:(CBService *)service;
- (void) peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic;
- (void) peripheral:(CBPeripheral *)peripheral didDiscoverService:(CBService*)service;
@end


@interface UARTPeripheral : NSObject <CBPeripheralDelegate>
@property CBPeripheral *peripheral;
@property id<UARTPeripheralDelegate> delegate;

+ (CBUUID *) uartServiceUUID;

- (UARTPeripheral *) initWithPeripheral:(CBPeripheral*)peripheral delegate:(id<UARTPeripheralDelegate>) delegate;

- (void) writeString:(NSString *) string;
- (void) writeRawData:(NSData *) data;

- (void) didConnect;
- (void) didDisconnect;
- (BOOL) isConnected;
@end
