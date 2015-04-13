/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapCustomAnnotationView.h"

@implementation AkylasMapCustomAnnotationView

- (id)initWithAnnotation:(id <MKAnnotation>)annotation reuseIdentifier:(NSString *)reuseIdentifier map:(AkylasMapMapView*)map
{
    if (self = [super initWithAnnotation:annotation reuseIdentifier:reuseIdentifier]) {
        self.backgroundColor = [UIColor clearColor];
        wrapperView = [[UIView alloc] initWithFrame:CGRectZero];
//        wrapperView.userInteractionEnabled = false;
        [self addSubview:wrapperView];
    }
    return self;
}

- (void)setProxy:(TiViewProxy*)pinView
{
    if (theProxy != pinView) {
        [[theProxy view] removeFromSuperview];
        RELEASE_TO_NIL(theProxy);
        [self initWithProxy:pinView];
    }
    else {
        TiUIView* theView = [theProxy barButtonViewForRect:[TiUtils appFrame]];
        self.frame = wrapperView.frame = [theView bounds];
    }
}

- (void)initWithProxy:(TiViewProxy*)pinView
{
    theProxy = [pinView retain];
    TiUIView* theView = [theProxy barButtonViewForRect:[TiUtils appFrame]];
    self.frame = wrapperView.frame = [theView bounds];
    [wrapperView addSubview:theView];
}

-(void)dealloc
{
    RELEASE_TO_NIL(wrapperView);
    RELEASE_TO_NIL(lastHitName);
    RELEASE_TO_NIL(theProxy);
    [super dealloc];
}

-(NSString *)lastHitName
{
    NSString * result = lastHitName;
    [lastHitName autorelease];
    lastHitName = nil;
    return result;
}

- (UIView *)hitTest:(CGPoint) point withEvent:(UIEvent *)event
{
    UIView * result = [self.calloutView hitTest:[self.calloutView convertPoint:point fromView:self] withEvent:event];
	
	if (result==nil) {
        for (UIView * ourSubView in [self subviews]) {
            CGPoint subPoint = [self convertPoint:point toView:ourSubView];
            for (UIView * ourSubSubView in [ourSubView subviews]) {
                if (CGRectContainsPoint([ourSubSubView frame], subPoint) && [ourSubSubView isKindOfClass:[UILabel class]]) {
                    NSString * labelText = [(UILabel *)ourSubSubView text];
                    AkylasMapAnnotationProxy * ourProxy = (AkylasMapAnnotationProxy *)[self annotation];
                    RELEASE_TO_NIL(lastHitName);
                    if ([labelText isEqualToString:[ourProxy title]]) {
                        lastHitName = [@"title" retain];
                    }
                    else if ([labelText isEqualToString:[ourProxy subtitle]]) {
                        lastHitName = [@"subtitle" retain];
                    }
                    return nil;
                }
            }
            if (CGRectContainsPoint([ourSubView bounds], subPoint)) {
                RELEASE_TO_NIL(lastHitName);
                lastHitName = [@"annotation" retain];
                return nil;
            }
        }
    }
    RELEASE_TO_NIL(lastHitName);
    return result;
}

@end
