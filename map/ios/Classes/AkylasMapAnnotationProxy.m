//
//  AkylasGooglemapAnnotationProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapAnnotationProxy.h"
#import "TiButtonUtil.h"

@implementation AkylasMapAnnotationProxy
{
    MKAnnotationView* _annView;
}
@synthesize annView = _annView;
@synthesize pinColor;
-(void)_configure
{
    pinColor = MKPinAnnotationColorRed;
    [super _configure];
}

-(void)dealloc
{
    RELEASE_TO_NIL(_annView);
    [super dealloc];
}


-(NSString*)apiName
{
    return @"Akylas.Map.Annotation";
}

-(void)setShowInfoWindow:(BOOL)showInfoWindow
{
    [super setShowInfoWindow:showInfoWindow];
    if (_annView) {
        _annView.canShowCallout = self.showInfoWindow;
    }
}

-(void)setDraggable:(BOOL)draggable
{
    [super setDraggable:draggable];
    if (_annView) {
        _annView.draggable = self.draggable;
    }
}

-(void)setColor:(id)color
{
    [self replaceValue:color forKey:@"color" notification:NO];
    if ([color isKindOfClass:[NSNumber class]]) {
        self.pinColor = [TiUtils intValue:color];
        self.tintColor = nil;
    }
    else {
        self.tintColor = [[TiUtils colorValue:color] _color];
    }
    [self setNeedsRefreshingWithSelection:YES];
}

-(void)setInternalImage:(UIImage*)image {
    [super setInternalImage:image];
    if (_annView) {
        _annView.image = _internalImage;
    }
}
-(void)proxyDidRelayout:(id)sender
{
    id current = [self valueForUndefinedKey:@"pinView"];
    if ( ([current isEqual:sender] == YES) && (self.placed) ) {
        [self setNeedsRefreshingWithSelection:YES];
    }
}

-(int)tag
{
    return tag;
}

-(UIView*)makeButton:(id)button tag:(int)buttonTag
{
    UIView *button_view = nil;
    if ([button isKindOfClass:[NSNumber class]])
    {
        // this is button type constant
        NSInteger type = [TiUtils intValue:button];
        button_view = [TiButtonUtil buttonWithType:type];
    }
    else
    {
        UIImage *image = [[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:button proxy:self]];
        if (image!=nil)
        {
            CGSize size = [image size];
            UIButton *bview = [UIButton buttonWithType:UIButtonTypeCustom];
            [TiUtils setView:bview positionRect:CGRectMake(0,0,size.width,size.height)];
            bview.backgroundColor = [UIColor clearColor];
            [bview setImage:image forState:UIControlStateNormal];
            button_view = bview;
        }
    }
    if (button_view!=nil)
    {
        button_view.tag = buttonTag;
    }
    return button_view;
}

@end
