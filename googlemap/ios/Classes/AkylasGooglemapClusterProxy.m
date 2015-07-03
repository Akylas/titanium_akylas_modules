//
//  AkylasGooglemapClusterSetProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "AkylasGooglemapClusterProxy.h"
#import "GClusterAlgorithm.h"
#import "AkylasGooglemapAnnotationProxy.h"
#import "AkylasGooglemapView.h"
#import "GCluster.h"
#import <CoreText/CoreText.h>
#import "DTCoreTextFunctions.h"

@implementation AkylasClusterAlgorithm
static NSInteger idIncrement = 0;

- (id)initWithMaxDistanceAtZoom:(NSInteger)aMaxDistanceAtZoom {
    if (self = [super initWithMaxDistanceAtZoom:aMaxDistanceAtZoom]) {
        _uniqueId = idIncrement++;
    }
    return self;
}


@end
@implementation AkylasGooglemapClusterProxy
{
    id<GClusterAlgorithm> _algorithm;
    UIFont* _font;
}
@synthesize maxDistance;

-(void)dealloc
{
    if (_algorithm) {
        ((AkylasClusterAlgorithm*)_algorithm).proxy = nil;
        RELEASE_TO_NIL(_algorithm);
    }
    RELEASE_TO_NIL(_color)
    RELEASE_TO_NIL(_font)

    [super dealloc];
}


-(void)_configure
{
    maxDistance = 50;
    _showText = YES;
    _font  = [[UIFont boldSystemFontOfSize:14.0f] retain];
    _color  = [[UIColor whiteColor] retain];

    [super _configure];
}

-(NSUInteger) uniqueId {
    if (_algorithm) {
        return ((AkylasClusterAlgorithm*)_algorithm).uniqueId;
    }
    return -1;
}

-(id<GClusterAlgorithm>)algorithm
{
    if (!_algorithm) {
        _algorithm = [[AkylasClusterAlgorithm alloc] init];
        ((AkylasClusterAlgorithm*)_algorithm).maxDistanceAtZoom = self.maxDistance;
        ((AkylasClusterAlgorithm*)_algorithm).proxy = self;
    }
    return _algorithm;
}

-(void)setMaxDistance:(CGFloat)newValue
{
    maxDistance = newValue;
    [self replaceValue:@(newValue) forKey:@"maxDistance" notification:NO];
    if (_algorithm) {
        ((AkylasClusterAlgorithm*)_algorithm).maxDistanceAtZoom = maxDistance;
    }
}

-(void)setInternalImage:(UIImage*)image {
    [super setInternalImage:image];
    [self cluster];
}

-(NSString*)apiName
{
    return @"Akylas.GoogleMap.Cluster";
}

-(void)internalAddAnnotations:(NSArray*)annots {
    __block id<GClusterAlgorithm> algo = [self algorithm];
    [annots enumerateObjectsUsingBlock:^(AkylasGooglemapAnnotationProxy* anno, NSUInteger idx, BOOL *stop) {
        anno.delegate = self;
        [algo addItem:anno];
    }];
    [self cluster];
}
-(void)internalRemoveAnnotations:(id)annots {
    if (IS_OF_CLASS(NSArray, annots)) {
        __block id<GClusterAlgorithm> algo = [self algorithm];
        [annots enumerateObjectsUsingBlock:^(AkylasGooglemapAnnotationProxy* anno, NSUInteger idx, BOOL *stop) {
            [algo removeItem:anno];
        }];
    } else {
        [[self algorithm] removeItem:annots];
    }
    [self cluster];
}

-(void)removeAllAnnotations:(id)unused
{
    [super removeAllAnnotations:nil];
    [[self algorithm] removeItems];
    [self cluster];

}

-(Class)annotationClass
{
    return [AkylasGooglemapAnnotationProxy class];
}

-(void)refreshAnnotation:(AkylasGooglemapAnnotationProxy*)proxy reAdd:(BOOL)yn {
    [self cluster];
}

-(GMSMarker*)createClusterMarker:(id <GCluster>)cluster {
    GMSMarker *marker = [[GMSMarker alloc] init];
    
    NSUInteger count = cluster.items.count;
    marker.icon = [self generateClusterIconWithCount:count];
    marker.userData = self;
    marker.tappable = false;
    marker.position = cluster.position;
    return [marker autorelease];
}

-(void)setFont_:(id)font
{
    RELEASE_TO_NIL(_font)
    WebFont *f = [TiUtils fontValue:font def:nil];
    _font = [[f font] retain];
    [self cluster];
}


-(void)cluster {
    if (IS_OF_CLASS(self.delegate, AkylasGooglemapView)) {
        TiThreadPerformBlockOnMainThread(^{
            [[((AkylasGooglemapView*)self.delegate) clusterManager] clusterAlgo:[self algorithm]];
        }, YES);
    }
}

-(void)setColor:(id)color
{
    RELEASE_TO_NIL(_color)
    [self replaceValue:color forKey:@"color" notification:NO];
    _color = [[[TiUtils colorValue:color] _color] retain];
    [self cluster];
}


- (UIImage*)generateClusterIconWithCount:(NSUInteger)count {
    
    int diameter = 30;
    CGContextRef ctx;
    UIColor* color = _color;

    if (_internalImage) {
        if (!_showText) {
            return _internalImage;
        }
        CGSize size = _internalImage.size;
        //set the graphics context to be the size of the image
        UIGraphicsBeginImageContextWithOptions(size, YES, 0.0);
        diameter = size.width;
        [_internalImage drawInRect:CGRectMake(0.0, 0.0, size.width, size.height)];
        ctx = UIGraphicsGetCurrentContext();
    } else {
        float inset = 2;
        CGRect rect = CGRectMake(0, 0, diameter, diameter);
        UIGraphicsBeginImageContextWithOptions(rect.size, NO, 0);
        
        ctx = UIGraphicsGetCurrentContext();
        
        [_color setStroke];
        
        if ([self nGetTintColor]) {
            [[self nGetTintColor] setFill];
        } else {
            if (count > 100) [[UIColor orangeColor] setFill];
            else if (count > 10) [[UIColor yellowColor] setFill];
            else [[UIColor colorWithRed:0.0/255.0 green:100.0/255.0 blue:255.0/255.0 alpha:1] setFill];
        }
        
        
        CGContextSetLineWidth(ctx, inset);
        
        // make circle rect 5 px from border
        CGRect circleRect = CGRectMake(0, 0, diameter, diameter);
        circleRect = CGRectInset(circleRect, inset, inset);
        
        // draw circle
        CGContextFillEllipseInRect(ctx, circleRect);
        CGContextStrokeEllipseInRect(ctx, circleRect);
    }

   
    if (_showText && _color && _font) {
        CGFloat fontHeight = _font.pointSize;
        CGFloat yOffset = (diameter - fontHeight) / 2.0f - 2.0f;
        
        CGRect textRect = CGRectMake(0, yOffset, diameter, fontHeight);
        CGContextSetFillColorWithColor(ctx, color.CGColor);
        [[NSString stringWithFormat:@"%lu", (unsigned long)count] drawInRect: textRect
             withFont: _font
        lineBreakMode: UILineBreakModeClip
            alignment: UITextAlignmentCenter];
        
    }
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    
    return image;
}
@end
