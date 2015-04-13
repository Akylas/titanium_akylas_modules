//
//  AkylasGooglemapAnnotationProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapboxAnnotationProxy.h"
#import "AkylasMapboxView.h"

@implementation AkylasMapboxAnnotationProxy
{
    RMAnnotation* _rmannotation;
    RMMarker* _marker;
    RMMarkerMapboxImageSize _size;
}
@synthesize rmannotation = _rmannotation;


-(void)_configure
{
    _size = RMMarkerMapboxImageSizeMedium;
    [super _configure];
}

-(void)dealloc
{
    RELEASE_TO_NIL(_rmannotation);
    RELEASE_TO_NIL(_marker);
    [super dealloc];
}


-(NSString*)apiName
{
    return @"Akylas.Mapbox.Annotation";
}

-(void)refreshCoords {
    if (_rmannotation != nil) {
        _rmannotation.coordinate = self.coordinate;
    }
    [super refreshCoords];
}

-(void)setTitle:(id)value
{
    [super setTitle:value];
    if (_rmannotation) {
        _rmannotation.title = self.title;
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setSubtitle:(id)value
{
    [super setSubtitle:value];
    
    if (_rmannotation) {
        _rmannotation.subtitle = self.subtitle;
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setSortKey:(id)sortKey
{
    [super setSortKey:sortKey];

    if (_rmannotation) {
        _rmannotation.sortKey = self.sortKey;
    }
}

- (void)setLeftButton:(id)button
{
    [super setLeftButton:button];
    if (_marker) {
        _marker.leftCalloutAccessoryView = [self nGetLeftViewAccessory];
        [self setNeedsRefreshingWithSelection:NO];
    }
}

- (void)setRightButton:(id)button
{
    [super setRightButton:button];

    if (_marker) {
        _marker.rightCalloutAccessoryView = [self nGetRightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setRightView:(id)rightview
{
    [super setRightView:rightview];
    if (_marker) {
        _marker.rightCalloutAccessoryView = [self nGetRightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setLeftView:(id)leftview
{
    [super setLeftView:leftview];
    if (_marker) {
        _marker.leftCalloutAccessoryView = [self nGetLeftViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setCustomView:(id)leftview
{
    [super setCustomView:leftview];
    if (_marker) {
        _marker.customCalloutAccessoryView = [self nGetCustomViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setAnchorPoint:(id)value
{
    [super setAnchorPoint:value];
    if (_marker) {
        [_marker replaceUIImage:_internalImage anchorPoint:[self nGetAnchorPoint]];
    }
}

-(void)setInternalImage:(UIImage*)image {
    [super setInternalImage:image];
    if (_marker) {
        [_marker replaceUIImage:_internalImage anchorPoint:[self nGetAnchorPoint]];
    }
}

-(void)setMbImage:(id)image
{
    RELEASE_TO_NIL(_mbImage)
    _mbImage = [[TiUtils stringValue:image] retain];
    [self replaceValue:image forKey:@"mbImage" notification:NO];
    [self setNeedsRefreshingWithSelection:(_marker != nil)];
}



-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView
{
    if (_rmannotation == nil) {
        _rmannotation = [[RMAnnotation alloc] initWithMapView:mapView coordinate:self.coordinate andTitle:nil];
        _rmannotation.userInfo = self;
        _rmannotation.sortKey = self.sortKey;
        _rmannotation.minZoom = self.minZoom;
        _rmannotation.maxZoom = self.maxZoom;
    }
    else if (_rmannotation.mapView != mapView) {
        RELEASE_TO_NIL(_rmannotation)
        return [self getRMAnnotationForMapView:mapView];
    }
    return _rmannotation;
}

-(RMAnnotation*)getRMAnnotation
{
    return _rmannotation;
}

-(RMMarker*)marker
{
    return _marker;
}

-(UIImage*)coloredImage:(UIImage*)image withColor:(UIColor*)color
{
    if (image == nil || color == nil) return image;
    // begin a new image context, to draw our colored image onto
    UIGraphicsBeginImageContext(image.size);
    
    // get a reference to that context we created
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // set the fill color
    [color setFill];
    
    // translate/flip the graphics context (for transforming from CG* coords to UI* coords
    CGContextTranslateCTM(context, 0, image.size.height);
    CGContextScaleCTM(context, 1.0, -1.0);
    
    // set the blend mode to color burn, and the original image
    CGContextSetBlendMode(context, kCGBlendModeColorBurn);
    CGRect rect = CGRectMake(0, 0, image.size.width, image.size.height);
    CGContextDrawImage(context, rect, image.CGImage);
    
    // set a mask that matches the shape of the image, then draw (color burn) a colored rectangle
    CGContextClipToMask(context, rect, image.CGImage);
    CGContextAddRect(context, rect);
    CGContextDrawPath(context,kCGPathFill);
    
    // generate a new UIImage from the graphics context we drew onto
    UIImage *coloredImg = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    //return the color-burned image
    return coloredImg;
}

-(UIImage*)getMapBoxImage:(NSString *)symbolName color:(UIColor*)color size:(RMMarkerMapboxImageSize)size
{
    NSString *sizeString = nil;
    
    switch (size)
    {
        case RMMarkerMapboxImageSizeSmall:
            sizeString = @"small";
            break;
            
        case RMMarkerMapboxImageSizeMedium:
        default:
            sizeString = @"medium";
            break;
            
        case RMMarkerMapboxImageSizeLarge:
            sizeString = @"large";
            break;
    }
    
    BOOL useRetina = ([[UIScreen mainScreen] scale] > 1.0);
    
    NSString *colorHex = nil;
    
    if (color)
    {
        CGFloat white, red, green, blue, alpha;
        
        if ([color getRed:&red green:&green blue:&blue alpha:&alpha])
        {
            colorHex = [NSString stringWithFormat:@"%02lx%02lx%02lx", (unsigned long)(red * 255), (unsigned long)(green * 255), (unsigned long)(blue * 255)];
        }
        else if ([color getWhite:&white alpha:&alpha])
        {
            colorHex = [NSString stringWithFormat:@"%02lx%02lx%02lx", (unsigned long)(white * 255), (unsigned long)(white * 255), (unsigned long)(white * 255)];
        }
    }
    NSURL *imageURLForCache = [NSURL URLWithString:[NSString stringWithFormat:@"http://api.tiles.mapbox.com/v3/marker/pin-%@%@%@%@.png",
                                                    (sizeString ? [sizeString substringToIndex:1] : @"m"),
                                                    (symbolName ? [@"-" stringByAppendingString:symbolName] : @""),
                                                    (colorHex?colorHex:@"+000000"),
                                                    (useRetina  ? @"@2x" : @"")]];
    NSURL *imageURLForDownload = [NSURL URLWithString:[NSString stringWithFormat:@"http://api.tiles.mapbox.com/v3/marker/pin-%@%@%@%@.png",
                                                       (sizeString ? [sizeString substringToIndex:1] : @"m"),
                                                       (symbolName ? [@"-" stringByAppendingString:symbolName] : @""),
                                                       (colorHex?@"+cccccc":@"+000000"),
                                                       (useRetina  ? @"@2x" : @"")]];
    UIImage* image = [[ImageLoader sharedLoader] loadImmediateImage:imageURLForCache];
    if (image == nil)  {
        image = [[ImageLoader sharedLoader] loadImmediateImage:imageURLForDownload];
        if (image == nil) {
            NSData* data = [NSData brandedDataWithContentsOfURL:imageURLForDownload];
            image = [UIImage imageWithData:data scale:(useRetina ? 2.0 : 1.0)];
            [[ImageLoader sharedLoader] cache:image forURL:imageURLForDownload];
        }
        if (color) {
            image = [self coloredImage:image withColor:color];
        }
        [[ImageLoader sharedLoader] cache:image forURL:imageURLForCache];
    }
    
    return image;
}

-(RMMapLayer*)shapeLayerForMapView:(AkylasMapboxView*)mapView
{
    if (_marker == nil) {
        if (_internalImage) {
            _marker = [[RMMarker alloc] initWithUIImage:_internalImage anchorPoint:[self nGetAnchorPoint]];
        }
        else if (mapView.defaultPinImage) {
            _marker = [[RMMarker alloc] initWithUIImage:mapView.defaultPinImage anchorPoint:mapView.defaultPinAnchor];
        }
        else {
            
            _marker = [[RMMarker alloc] initWithUIImage:[self getMapBoxImage:_mbImage color:self.tintColor size:_size] anchorPoint:mapView.defaultPinAnchor];
        }
        if ([self valueForUndefinedKey:@"anchorPoint"]) {
            _marker.anchorPoint = [self nGetAnchorPoint];
        }
        else {
            _marker.anchorPoint = mapView.defaultPinAnchor;
        }
        
        if (![self valueForUndefinedKey:@"calloutAnchorPoint"]) {
            _calloutAnchorPoint = mapView.defaultCalloutAnchor;
        }
        _marker.canShowCallout = [TiUtils boolValue:[self valueForUndefinedKey:@"canShowCallout"] def:YES];
    }
    return _marker;
}

@end
