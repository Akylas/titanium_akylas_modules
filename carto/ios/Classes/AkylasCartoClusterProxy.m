//
//  AkylasCartoClusterSetProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "AkylasCartoClusterProxy.h"

#import "AkylasCartoAnnotationProxy.h"
#import "AkylasCartoViewProxy.h"
#import "AkylasMapBaseViewProxy.h"
#import "AkylasCartoView.h"
#import "AkylasCartoModule.h"

@interface MyMarkerClusterElementBuilder : NTClusterElementBuilder
@property AkylasCartoClusterProxy* clusterProxy;

@end
@interface MyMarkerClusterElementBuilder ()

@property NSMutableDictionary* markerStyles;

@end


@implementation MyMarkerClusterElementBuilder

-(NTVectorElement*)buildClusterElement:(NTMapPos *)mapPos elements:(NTVectorElementVector *)elements
{
    if (!self.markerStyles) {
        self.markerStyles = [NSMutableDictionary new];
    }
    
    NSString* styleKey = [NSString stringWithFormat:@"%d",(int)[elements size]];
    
    NTMarkerStyle* markerStyle = [self.markerStyles valueForKey:styleKey];
    
    if ([elements size] == 1) {
        markerStyle = [(NTMarker*)[elements get:0] getStyle];
    }
    
    if (!markerStyle) {
        
        markerStyle = [[self.clusterProxy getMarkerStyleBuilder] buildStyle];
        [self.markerStyles setValue:markerStyle forKey:styleKey];
    }
    
    NTMarker* marker = [[NTMarker alloc] initWithPos:mapPos style:markerStyle];
    
    return marker;
}

@end



@implementation AkylasCartoClusterProxy
{
    UIFont* _font;
    CGFloat _strokeWidth;
    UIColor *_color;
    UIColor *_strokeColor;
    
    UIFont* _selectedFont;
    CGFloat _selectedStrokeWidth;
    UIColor *_selectedColor;
    UIColor *_selectedStrokeColor;
    
    NTLocalVectorDataSource* vectorDataSource;
    MyMarkerClusterElementBuilder* clusterElementBuilder;
    NTClusteredVectorLayer* vectorLayer;
    
}
@synthesize minDistance, maxZoom;

-(void)dealloc
{
//    if (_algorithm) {
//        ((AkylasClusterAlgorithm*)_algorithm).proxy = nil;
//        RELEASE_TO_NIL(_algorithm);
//    }
    RELEASE_TO_NIL(vectorDataSource)
    RELEASE_TO_NIL(clusterElementBuilder)
    RELEASE_TO_NIL(vectorLayer)
    RELEASE_TO_NIL(_strokeColor)
    RELEASE_TO_NIL(_font)
    
    RELEASE_TO_NIL(_selectedColor)
    RELEASE_TO_NIL(_selectedStrokeColor)
    RELEASE_TO_NIL(_selectedFont)

    [super dealloc];
}


-(void)_configure
{
    minDistance = 100;
    maxZoom = 24;
    _showText = YES;
    _strokeWidth = 2;
    _font  = [[UIFont boldSystemFontOfSize:14.0f] retain];
    _color  = [[UIColor whiteColor] retain];
    _strokeColor = nil;
    _selectedColor = nil;
    _selectedStrokeColor = nil;
    _selectedFont = nil;
    [super _configure];
}

//-(NSUInteger) uniqueId {
//    if (_algorithm) {
//        return ((AkylasClusterAlgorithm*)_algorithm).uniqueId;
//    }
//    return -1;
//}

//-(AkylasClusterAlgorithm*)algorithm
//{
//    if (!_algorithm) {
//        _algorithm = [[AkylasClusterAlgorithm alloc] init];
//        _algorithm.minDistance = self.minDistance;
//        _algorithm.maxDistanceAtZoom = self.maxDistance;
//        _algorithm.proxy = self;
//        _algorithm.visible = self.visible;
//        _algorithm.minZoom = self.minZoom;
//        _algorithm.maxZoom = self.maxZoom;
//    }
//    return _algorithm;
//}

-(NTLocalVectorDataSource*) dataSource {
    if (!vectorDataSource) {
        vectorDataSource = [[NTLocalVectorDataSource alloc] initWithProjection:[AkylasCartoModule baseProjection]];

    }
    return vectorDataSource;
}

-(MyMarkerClusterElementBuilder*) clusterElementBuilder {
    if (!clusterElementBuilder) {
        clusterElementBuilder = [[MyMarkerClusterElementBuilder alloc] init];
        
    }
    return clusterElementBuilder;
}

-(NTClusteredVectorLayer*) getLayer {
    if (!vectorLayer) {
        vectorLayer = [[NTClusteredVectorLayer alloc] initWithDataSource:[self dataSource] clusterElementBuilder:[self clusterElementBuilder]];
    }
}

-(void)setMaxZoom:(CGFloat)newValue
{
    maxZoom = newValue;
    [self replaceValue:@(newValue) forKey:@"maxZoom" notification:NO];
    [[self getLayer] setMaximumClusterZoom:maxZoom];
}

-(void)setMinDistance:(CGFloat)newValue
{
    minDistance = newValue;
    [self replaceValue:@(newValue) forKey:@"minDistance" notification:NO];
    [[self getLayer] setMinimumClusterDistance:minDistance];

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
    TiThreadPerformBlockOnMainThread(^{
        NTVectorElementVector *elements = [[NTVectorElementVector alloc]init];
        [annots enumerateObjectsUsingBlock:^(AkylasCartoAnnotationProxy*  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [elements add:[obj getMarker]];
        }];
//        [self cluster];
        // Add them all at once to avoid flickering
        [[self dataSource] addAll:elements];
    }, YES);

    
}
//-(void)internalRemoveAnnotations:(id)annots {
//    if (IS_OF_CLASS(NSArray, annots)) {
//        __block id<GClusterAlgorithm> algo = [self algorithm];
//        [annots enumerateObjectsUsingBlock:^(AkylasCartoAnnotationProxy* anno, NSUInteger idx, BOOL *stop) {
//            [algo removeItem:anno];
//        }];
//    } else {
//        [[self algorithm] removeItem:annots];
//    }
//    [self cluster];
//}

-(void)removeAllAnnotations:(id)unused
{
    [super removeAllAnnotations:nil];
    [vectorDataSource clear];

}

-(void)removeAnnotation:(id)args
{
    
    PREPARE_ARRAY_ARGS(args)
    
    [super removeAnnotation:args];
    if (!IS_OF_CLASS(value, NSArray)) {
        [self removeAnnotation:@[args]];
        return;
    }
//    TiThreadPerformBlockOnMainThread(^{
//        [[self algorithm] removeClusterItemsInSet:IS_OF_CLASS(value, NSArray)?[NSSet setWithArray:(NSArray*)value]:[NSSet setWithObject:value] fromMap:[(AkylasCartoViewProxy*)self.delegate map]];
//        [self cluster];
//    }, YES);
}

//-(void)internalRemoveAnnotation:(AkylasCartoAnnotationProxy*)annot
//{
//    [super internalRemoveAnnotation:annot];
//}

-(Class)annotationClass
{
    return [AkylasCartoAnnotationProxy class];
}

-(void)refreshAnnotation:(AkylasCartoAnnotationProxy*)proxy reAdd:(BOOL)yn {
    [self cluster];
}

+(int)gZIndexDelta {
    static int lastIndex = 0;
    return lastIndex;
}


//-(NTMarker*)createClusterMarker:(GStaticCluster*) cluster {
//    AkylasClusterMarker *marker = [[AkylasClusterMarker alloc] init];
//    
//    NSUInteger count = cluster.items.count;
//    marker.cluster = cluster;
//    marker.icon = [self generateClusterIconWithCount:count selected:NO];
//    marker.userData = self;
//    marker.flat = self.flat;
//    marker.tappable = self.touchable;
//    marker.position = cluster.position;
//    marker.infoWindowAnchor = [self nGetCalloutAnchorPoint];
//    marker.groundAnchor = [self nGetAnchorPoint];
//    marker.zIndex = (int)self.zIndex;
//    return [marker autorelease];
//}

-(void)setFont:(id)font
{
    RELEASE_TO_NIL(_font)
    WebFont *f = [TiUtils fontValue:font def:nil];
    _font = [[f font] retain];
    [self cluster];
}

-(void)setSelectedFont:(id)font
{
    RELEASE_TO_NIL(_selectedFont)
    WebFont *f = [TiUtils fontValue:font def:nil];
    _selectedFont = [[f font] retain];
}

-(void)setVisible:(BOOL)visible
{
    [super setVisible:visible];
//    if (_algorithm) {
//        TiThreadPerformBlockOnMainThread(^{
//        _algorithm.visible = self.visible;
//            [self cluster];
//        }, NO);
//    }
}

-(void)setMinZoom:(CGFloat)minZoom
{
    [super setMinZoom:minZoom];
    if (_algorithm) {
        TiThreadPerformBlockOnMainThread(^{
            _algorithm.minZoom = self.minZoom;
            [self cluster];
        }, NO);
    }
}

-(void)setMaxZoom:(CGFloat)maxZoom
{
    [super setMaxZoom:maxZoom];
    if (_algorithm) {
        TiThreadPerformBlockOnMainThread(^{
            _algorithm.maxZoom = self.maxZoom;
            [self cluster];
        }, NO);
    }
}

-(void)cluster {
    if (IS_OF_CLASS(self.delegate, AkylasCartoViewProxy)) {
        TiThreadPerformBlockOnMainThread(^{
            [[(AkylasCartoViewProxy*)self.delegate clusterManager] clusterAlgo:[self algorithm]];
        }, NO);
    }
}




-(void)setColor:(id)color
{
    RELEASE_TO_NIL(_color)
    [self replaceValue:color forKey:@"color" notification:NO];
    _color = [[[TiUtils colorValue:color] _color] retain];
    [self cluster];
}

-(id)color {
    return [self valueForUndefinedKey:@"color"];
}

-(void)setSelectedColor:(id)color
{
    RELEASE_TO_NIL(_selectedColor)
    [self replaceValue:color forKey:@"selectedColor" notification:NO];
    _selectedColor = [[[TiUtils colorValue:color] _color] retain];
//    [self cluster];
}

-(id)selectedColor {
    return [self valueForUndefinedKey:@"selectedColor"];
}


-(void)setStrokeColor:(id)color
{
    RELEASE_TO_NIL(_strokeColor)
    [self replaceValue:color forKey:@"strokeColor" notification:NO];
    _strokeColor = [[[TiUtils colorValue:color] _color] retain];
    [self cluster];
}

-(id)strokeColor {
    return [self valueForUndefinedKey:@"strokeColor"];
}


-(void)setSelectedStrokeColor:(id)color
{
    RELEASE_TO_NIL(_selectedStrokeColor)
    [self replaceValue:color forKey:@"selectedStrokeColor" notification:NO];
    _selectedStrokeColor = [[[TiUtils colorValue:color] _color] retain];
}

-(id)selectedStrokeColor {
    return [self valueForUndefinedKey:@"selectedStrokeColor"];
}

-(void)setStrokeWidth:(id)value
{
    [self replaceValue:value forKey:@"strokeWidth" notification:NO];
    _strokeWidth = [TiUtils floatValue:value];
    [self cluster];
}

-(void)setSelectedStrokeWidth:(id)value
{
    [self replaceValue:value forKey:@"selectedStrokeWidth" notification:NO];
    _selectedStrokeWidth = [TiUtils floatValue:value];
}


- (UIImage*)generateClusterIconWithCount:(NSUInteger)count selected:(BOOL)selected {
    
    int diameter = 30;
    CGContextRef ctx;
    UIImage* theImage = (selected && _internalSelectedImage) ? _internalSelectedImage : _internalImage;
    BOOL showText = selected ? _selectedShowText : _showText;
    if (theImage) {
        if (!showText) {
            return theImage;
        }
        CGSize size = theImage.size;
        //set the graphics context to be the size of the image
        UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
        diameter = size.width;
        [theImage drawInRect:CGRectMake(0.0, 0.0, size.width, size.height)];
        ctx = UIGraphicsGetCurrentContext();
    } else {
        float inset = selected ? _selectedStrokeWidth : _strokeWidth;
        CGRect rect = CGRectMake(0, 0, diameter, diameter);
        UIGraphicsBeginImageContextWithOptions(rect.size, NO, 0);
        
        ctx = UIGraphicsGetCurrentContext();
        
        UIColor* tintColor = (selected?[self nGetSelectedTintColor]:nil);
        if (!tintColor) {
            tintColor =[self nGetTintColor];
        }
        if (tintColor) {
            [tintColor setFill];
        }
        
        
        
        // make circle rect 5 px from border
        CGRect circleRect = CGRectMake(0, 0, diameter, diameter);
        circleRect = CGRectInset(circleRect, inset, inset);
        
        // draw circle
        CGContextFillEllipseInRect(ctx, circleRect);

        
        tintColor = (selected && _selectedStrokeColor) ? _selectedStrokeColor : _strokeColor;
        if (tintColor && inset > 0) {
            [tintColor setStroke];
            CGContextSetLineWidth(ctx, inset);
            CGContextStrokeEllipseInRect(ctx, circleRect);
        }
    }

   
    UIColor* color = (selected && _selectedColor) ? _selectedColor : _color;
    UIFont* font = (selected && _selectedFont) ? _selectedFont : _font;
    if (showText && color && font) {
        CGFloat fontHeight = font.pointSize;
        CGFloat yOffset = (diameter - fontHeight) / 2.0f - 2.0f;
        
        CGRect textRect = CGRectMake(0, yOffset, diameter, fontHeight);
        CGContextSetFillColorWithColor(ctx, color.CGColor);
        NSMutableParagraphStyle *style = [[NSParagraphStyle defaultParagraphStyle] mutableCopy];
        [style setAlignment:NSTextAlignmentCenter];
        [style setLineBreakMode:NSLineBreakByClipping];
        
        NSDictionary *attr = [NSDictionary dictionaryWithObjectsAndKeys:
                              NSFontAttributeName,font,
                              NSParagraphStyleAttributeName,style
                              , nil];
        [[NSString stringWithFormat:@"%lu", (unsigned long)count] drawInRect:textRect withAttributes:attr];
    }
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    
    return image;
}


-(void)onSelected:(NTVectorElement*)overlay {
    if (IS_OF_CLASS(overlay, AkylasClusterMarker)) {
        AkylasClusterMarker* theMarker = (AkylasClusterMarker*)overlay;
        theMarker.zIndex = 10000;
        theMarker.selected =(_internalSelectedImage ||
                          _selectedFont ||
                          _selectedStrokeColor ||
                          [self nGetSelectedTintColor] ||
                          _selectedColor ||
                          (_selectedShowText != _showText) ||
                          (_strokeWidth != _selectedStrokeWidth));
        if (theMarker.selected) {
            theMarker.icon = [self generateClusterIconWithCount:[[((AkylasClusterMarker*)theMarker) cluster] count] selected:YES];
        }
        if (self.showInfoWindow) {
            if (theMarker.map && IS_OF_CLASS(theMarker.map.delegate, AkylasCartoView)) {
                [ (AkylasCartoView*)(theMarker.map.delegate) showCalloutForOverlay:theMarker];
            }
        }
    }
}
-(void)onDeselected:(NTVectorElement*)overlay {
    if (IS_OF_CLASS(overlay, AkylasClusterMarker)) {
        AkylasClusterMarker* theMarker = (AkylasClusterMarker*)overlay;
        if (theMarker.selected) {
            theMarker.icon = [self generateClusterIconWithCount:[[((AkylasClusterMarker*)theMarker) cluster] count] selected:NO];
        }
        
        theMarker.zIndex = (int)self.zIndex;
        if (theMarker.map && IS_OF_CLASS(theMarker.map.delegate, AkylasCartoView)) {
            [ (AkylasCartoView*)(theMarker.map.delegate) hideCalloutForOverlay:theMarker];
        }
    }
}


-(id)visibleAnnotations
{
    return [[self algorithm] visibleItems];
}

@end
