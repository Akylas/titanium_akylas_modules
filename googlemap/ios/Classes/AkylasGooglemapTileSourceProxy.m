//
//  AkylasGoogleMapTileSourceProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasGooglemapTileSourceProxy.h"
#import "AkylasMapBaseModule.h"
#import "TiUtils.h"

#import "AkylasGMSURLTileLayer.h"
#import "AkylasGMSMBTilesLayer.h"

@implementation AkylasGooglemapTileSourceProxy
{
    GMSTileLayer*  _gTileLayer;
}
-(void)dealloc
{
    RELEASE_TO_NIL(_gTileLayer);
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.GoogleMap.TileSource";
}

//-(NSDictionary*)region
//{
//    return [AkylasMapBaseModule dictFromRegion:[[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox]];
//}


-(void)setFadeIn:(BOOL)fadeIn
{
    [super setFadeIn:fadeIn];
    if (_gTileLayer) {
        _gTileLayer.fadeIn = self.fadeIn;
    }
}

-(void)setZIndex:(NSInteger)zIndex
{
    [super setZIndex:zIndex];
    if (_gTileLayer) {
        _gTileLayer.zIndex = (int)self.zIndex;
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    [super setOpacity:opacity];
    if (_gTileLayer) {
        _gTileLayer.opacity = self.opacity;
    }
}

-(void)setTileSize:(NSInteger)tileSize
{
    [super setTileSize:tileSize];
    if (_gTileLayer) {
        _gTileLayer.tileSize = self.tileSize;
    }
}


-(GMSTileLayer*)tileLayer
{
    id source = [self valueForKey:@"source"];
    if (!IS_OF_CLASS(source, NSString)) {
        if (IS_OF_CLASS(source, AkylasGooglemapTileSourceProxy) && source != self) {
            return [source tileLayer];
        }
        else if ([source respondsToSelector:@selector(nativePath)]) {
            
        }
        else  {
            return nil;
        }
    }
    NSString* typeLower = [source lowercaseString];
    GMSTileLayer* result = nil;
    GMSTileURLConstructor constructor = nil;
    
    //    if ([typeLower hasSuffix:@"mbtiles"])
    //    {
    //        result = [[RMMBTilesSource alloc] initWithTileSetURL:[TiUtils toURL:type proxy:proxy]];
    //    }
    
    if ([typeLower isEqualToString:@"openstreetmap"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://tile.openstreetmap.org/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
        
    }
    else if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[AkylasGMSMBTilesLayer alloc] initWithTileSetURL:[TiUtils toURL:source proxy:self]];
    }
    else if ([typeLower isEqualToString:@"openseamap"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://tiles.openseamap.org/seamark/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapquest"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://otile1.mqcdn.com/tiles/1.0.0/map/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapquest-sat"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://otile1.mqcdn.com/tiles/1.0.0/sat/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"tilemill"] )
    {
        NSString* name = [TiUtils stringValue:[self valueForKey:@"mapName"]];
        NSString* url = [NSString stringWithFormat:@"%@:20008/tile/%@", [TiUtils stringValue:[self valueForKey:@"host"]], name];
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"%@/%lu/%lu/%lu.png?updated=%i", url, (unsigned long)x, (unsigned long)y, (unsigned long)zoom, (int)[[NSDate date] timeIntervalSince1970]];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapbox"] )
    {
        NSString* mapID = [TiUtils stringValue:[self valueForKey:@"mapId"]];
        NSString* imageQuality = [TiUtils stringValue:[self valueForKey:@"imageQuality"] def:@"png"];
        NSString* accessToken = [TiUtils stringValue:[self valueForKey:@"accessToken"] def:[AkylasMapBaseModule valueForKey:@"accessToken"]];
        CGFloat contentScaleFactor = [UIScreen mainScreen].scale;
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            return  [NSURL URLWithString:[NSString stringWithFormat:@"https://a.tiles.mapbox.com/v4/%@/%lu/%lu/%lu%@.%@?access_token=%@",
                                          mapID,
                                          (unsigned long)zoom,
                                          (unsigned long)x,
                                          (unsigned long)y,
                                          contentScaleFactor > 1.0 ? @"@2x" : @"",
                                          imageQuality,
                                          accessToken
                                          ]];
        };
    }
    else if ([typeLower isEqualToString:@"ign"] )
    {
        NSString* key = [TiUtils stringValue:[self valueForKey:@"key"]];
        NSString* layer = [TiUtils stringValue:[self valueForKey:@"layer"]];
        NSString* format = [TiUtils stringValue:[self valueForKey:@"format"] def:@"image/jpeg"];
        
        NSString* realLayer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS";
        if ([layer isEqualToString:@"express"]) {
            realLayer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.CLASSIQUE";
        } else if ([layer isEqualToString:@"expressStandard"]) {
            realLayer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD";
        } else if ([layer isEqualToString:@"plan"]) {
            realLayer = @"GEOGRAPHICALGRIDSYSTEMS.PLANIGN";
        } else if ([layer isEqualToString:@"buildings"]) {
            realLayer = @"BUILDINGS.BUILDINGS";
        } else if ([layer isEqualToString:@"parcels"]) {
            realLayer = @"CADASTRALPARCELS.PARCELS";
        } else if ([layer isEqualToString:@"slopes"]) {
            realLayer = @"ELEVATION.SLOPES.HIGHRES";
        }
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString* result = [NSString stringWithFormat:@"http://gpp3-wxs.ign.fr/%@/geoportail/wmts?LAYER=%@&EXCEPTIONS=text/xml&FORMAT=%@&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX=%lu&TILEROW=%lu&TILECOL=%lu",
                                key,
                                realLayer,
                                format,
                                (unsigned long)zoom,
                                (unsigned long)y,
                                (unsigned long)x
                                ];
            return  [NSURL URLWithString:result];
        };
    }
    
    if (constructor) {
        result = [[AkylasGMSURLTileLayer alloc] initWithConstructor:constructor];
        ((AkylasGMSURLTileLayer*)result).userAgent = [TiUtils stringValue:[self valueForKey:@"userAgent"]];
        ((AkylasGMSURLTileLayer*)result).cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:YES];
    }
    return [result autorelease];
}


-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView
{
    if (_gTileLayer == nil) {
        _gTileLayer = [[self tileLayer] retain];
        if (!_gTileLayer) return nil;
        _gTileLayer.fadeIn = self.fadeIn;
        _gTileLayer.zIndex = (int)self.zIndex;
        _gTileLayer.opacity = self.opacity;
        _gTileLayer.tileSize = self.tileSize;
    }
    else if (_gTileLayer.map && _gTileLayer.map != mapView) {
        RELEASE_TO_NIL(_gTileLayer)
        return [self getGTileLayerForMapView:mapView];
    }
    return _gTileLayer;
}


-(GMSTileLayer*)gTileLayer
{
    return _gTileLayer;
}

@end
