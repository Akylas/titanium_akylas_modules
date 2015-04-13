//
//  RMIgnMapSource.m
//  akylas.map
//
//  Created by Martin Guillon on 01/04/2015.
//
//

#import "RMIgnMapSource.h"

@implementation RMIgnMapSource

- (id)init
{
    if (!(self = [super init]))
        return nil;
    
    self.minZoom = 1;
    self.maxZoom = 21;
    self.layer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS";
    
    return self;
}

- (NSURL *)URLForTile:(RMTile)tile
{
    NSAssert4(((tile.zoom >= self.minZoom) && (tile.zoom <= self.maxZoom)),
              @"%@ tried to retrieve tile with zoomLevel %d, outside source's defined range %f to %f",
              self, tile.zoom, self.minZoom, self.maxZoom);
    
    ;
    return [NSURL URLWithString:[NSString stringWithFormat:@"http://gpp3-wxs.ign.fr/%@/geoportail/wmts?LAYER=%@&EXCEPTIONS=text/xml&FORMAT=%@&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX=%hd&TILEROW=%u&TILECOL=%u",
                                 _key,
                                 _layer,
                                 _format,
                                 tile.zoom,
                                 tile.y,
                                 tile.x
                                 ]];
}

- (NSString *)uniqueTilecacheKey
{
    return @"IGN";
}

- (NSString *)shortName
{
    return @"IGN";
}

- (NSString *)longDescription
{
    return @"Copyright (c) 2008-2014, Institut National de l'Information Géographique et Forestière France";
}

- (NSString *)shortAttribution
{
    return @"Copyright (c) 2008-2014, IGN";
}

- (NSString *)longAttribution
{
    return @"Copyright (c) 2008-2014, Institut National de l'Information Géographique et Forestière France";
}

@end
