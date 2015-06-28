//
//  AkylasWebSource.m
//  akylas.mapbox
//
//  Created by Martin Guillon on 06/06/2015.
//
//

#import "AkylasWebSource.h"
#import "TiUtils.h"

@implementation AkylasWebSource
{
    NSString* _name;
    NSString* _id;
    NSString* _description;
    NSString* _attribution;
    NSString* _url;
}

- (id)initWithDictionary:(NSDictionary*)dict
{
    if (!(self = [super init])) {
        return nil;
    }
    _name = [TiUtils stringValue:@"name" properties:dict];
    _id = [TiUtils stringValue:@"id" properties:dict];
    _description = [TiUtils stringValue:@"description" properties:dict];
    _attribution = [TiUtils stringValue:@"attribution" properties:dict];
    _url = [TiUtils stringValue:@"url" properties:dict];
    self.minZoom = 1;
    self.maxZoom = 22;
    
    return self;
}

- (NSURL *)URLForTile:(RMTile)tile
{
    NSAssert4(((tile.zoom >= self.minZoom) && (tile.zoom <= self.maxZoom)),
              @"%@ tried to retrieve tile with zoomLevel %d, outside source's defined range %f to %f",
              self, tile.zoom, self.minZoom, self.maxZoom);
    
    ;
    NSString* x = [NSString stringWithFormat:@"%u", tile.x];
    NSString* y = [NSString stringWithFormat:@"%u", tile.y];
    NSString* z = [NSString stringWithFormat:@"%u", tile.zoom];
    return [NSURL URLWithString:[[[_url stringByReplacingOccurrencesOfString:@"{x}" withString:x] stringByReplacingOccurrencesOfString:@"{y}" withString:y] stringByReplacingOccurrencesOfString:@"{z}" withString:z]];
}

- (NSString *)uniqueTilecacheKey
{
    return _id;
}

- (NSString *)shortName
{
    return _name;
}

- (NSString *)longDescription
{
    return _description;
}

- (NSString *)shortAttribution
{
    return _attribution;
}

- (NSString *)longAttribution
{
    return _attribution;
}

@end
