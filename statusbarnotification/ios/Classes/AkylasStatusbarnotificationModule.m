/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import "AkylasStatusbarnotificationModule.h"
#import "CWStatusBarNotification.h"
#import "NSString+DTUtilities.h"
#import "DTCoreText.h"

@implementation AkylasStatusbarnotificationModule
{
}


- (void)_configure
{
    [super _configure];
}

- (void)dealloc
{
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Statusbarnotification";
}

-(void)showMessage:(id)args
{
    ENSURE_SINGLE_ARG(args, NSObject)
    __block CWStatusBarNotification* notification = [[CWStatusBarNotification alloc] init];
    notification.notificationTappedBlock = ^(void) {
        NSLog(@"notification tapped");
        if ([self _hasListeners:@"click"]) {
            [self fireEvent:@"click" propagate:NO checkForListener:NO];
        }
        [notification dismissNotification];
    };
    if (IS_OF_CLASS(args, NSString)) {
        [notification displayNotificationWithMessage:args forDuration:3.0];
    } else if (IS_OF_CLASS(args, NSDictionary)) {
        __block NSAttributedString* htmlContent = nil;
        __block NSString* text = [[TiUtils stringValue:@"text" properties:args] retain];
        if (!text) {
            text = [TiUtils stringValue:@"html" properties:args];
            if (text) {
                  htmlContent = [[NSAttributedString alloc] initWithHTMLData:[text dataUsingEncoding:NSUTF8StringEncoding] options:@{
                                                                DTDefaultTextAlignment: @(kCTLeftTextAlignment),
                                                                DTDefaultFontFamily : @"Helvetica Neue",
                                                                NSFontAttributeName: @"Helvetica Neue",
                                                                DTUseiOS6Attributes: @YES,
                                                                NSTextSizeMultiplierDocumentOption: @(1),
                                                                DTDefaultLineBreakMode:@(kCTLineBreakByWordWrapping)
                                                                } documentAttributes:nil];
            }
        }
        TiColor* color = [TiUtils colorValue:@"backgroundColor" properties:args];
        if (color) {
            notification.notificationLabelBackgroundColor = color.color;
        }
        color = [TiUtils colorValue:@"color" properties:args];
        if (color) {
            notification.notificationLabelTextColor = color.color;
        }
        NSInteger style = [TiUtils intValue:@"style" properties:args def:CWNotificationStyleStatusBarNotification];
        notification.notificationStyle = style;
        
        
        style = [TiUtils intValue:@"animationType" properties:args def:CWNotificationAnimationTypeOverlay];
        notification.notificationAnimationType = style;
        
        style = [TiUtils intValue:@"inAnimationStyle" properties:args def:CWNotificationAnimationStyleTop];
        notification.notificationAnimationInStyle = style;
        style = [TiUtils intValue:@"outAnimationStyle" properties:args def:CWNotificationAnimationStyleTop];
        notification.notificationAnimationOutStyle = style;
        
        CGFloat duration = [TiUtils floatValue:@"duration" properties:args def:3000]/1000;
        
        TiThreadPerformBlockOnMainThread(^{
            if (htmlContent) {
                [notification displayNotificationWithAttributedString:htmlContent forDuration:duration];
                [htmlContent release];
            } else {
                [notification displayNotificationWithMessage:text forDuration:duration];
                [text release];
            }
            [notification autorelease];
        }, NO);
    

    }
    
}
@end
