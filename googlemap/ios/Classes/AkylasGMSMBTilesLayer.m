//
//  AkylasGMSMBTilesLayer.m
//
//  Created by Justin R. Miller on 6/18/10.
//  Copyright 2012-2013 Mapbox.
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are met:
//
//      * Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//
//      * Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//
//      * Neither the name of Mapbox, nor the names of its contributors may be
//        used to endorse or promote products derived from this software
//        without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
//  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

#import "AkylasGMSMBTilesLayer.h"
#import "TiBase.h"
#import "FMDB.h"

@implementation AkylasGMSMBTilesLayer
{
    NSString *_uniqueTilecacheKey;
    CGFloat _minZoom;
    CGFloat _maxZoom;
    dispatch_queue_t _dbQueue;
}

@synthesize cacheable;

- (id)initWithTileSetResource:(NSString *)name
{
    return [self initWithTileSetResource:name ofType:([[[name pathExtension] lowercaseString] isEqualToString:@"mbtiles"] ? @"" : @"mbtiles")];
}

- (id)initWithTileSetResource:(NSString *)name ofType:(NSString *)extension
{
    return [self initWithTileSetURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:name ofType:extension]]];
}

- (id)initWithTileSetURL:(NSURL *)tileSetURL
{
    if (self = [super init]) {
        _minZoom = -1;
        _maxZoom = -1;
        queue = [[FMDatabaseQueue databaseQueueWithPath:[tileSetURL path]] retain];
        _dbQueue = dispatch_queue_create("AkylasGMSMBTilesLayer.dbQueue", DISPATCH_QUEUE_SERIAL);
    
        if ( ! queue) {
            return nil;
        }
    
//    _uniqueTilecacheKey = [[NSString stringWithFormat:@"MBTiles%@", [queue.path lastPathComponent]] retain];
    
        [queue inDatabase:^(FMDatabase *db) {
            [db setShouldCacheStatements:YES];
            FMResultSet *results = [db executeQuery:@"select min(zoom_level) from tiles"];
            [results next];
            if (![self dbHadError:db])
                _minZoom = [results doubleForColumnIndex:0];
            [results close];
            results = [db executeQuery:@"select max(zoom_level) from tiles"];
            [results next];
            if (![self dbHadError:db])
                _maxZoom = [results doubleForColumnIndex:0];
            
            [results close];
        }];
        
            self.cacheable = NO;
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(queue);
//    RELEASE_TO_NIL(_uniqueTilecacheKey);
    dispatch_release(_dbQueue);
    _dbQueue = nil;
    [super dealloc];
}

-(void)getImageForTileX:(NSUInteger)x
                      y:(NSUInteger)y
                   zoom:(NSUInteger)zoom
              comletion:(void(^)(UIImage*)) completion
{
    
    dispatch_async(_dbQueue, ^{
        [queue inDatabase:^(FMDatabase *db)
         {
             int dbY = pow(2, zoom) - y - 1;
             UIImage *image = kGMSTileLayerNoTile;
             FMResultSet *results = [db executeQuery:@"select tile_data from tiles where zoom_level = ? and tile_column = ? and tile_row = ?",
                                     [NSNumber numberWithUnsignedLongLong:zoom],
                                     [NSNumber numberWithUnsignedLongLong:x],
                                     [NSNumber numberWithUnsignedLongLong:dbY]];
             
             if (![self dbHadError:db] && [results next]) {
                 
                 NSData *data = ([[results columnNameToIndexMap] count] ? [results dataForColumn:@"tile_data"] : nil);
                 
                 if (data) {
                     image = [UIImage imageWithData:data];
                 }
             }
             
             [results close];
             completion(image);
         }];
    });    
//    [receiver receiveTileWithX:x y:y zoom:zoom image:nil];
}

//- (UIImage *)tileForX:(NSUInteger)x y:(NSUInteger)y zoom:(NSUInteger)zoom;
//{
//    
//    y = pow(2, zoom) - y - 1;
//    
//    __block UIImage *image = kGMSTileLayerNoTile;
//    
//    [queue inDatabase:^(FMDatabase *db)
//     {
//         FMResultSet *results = [db executeQuery:@"select tile_data from tiles where zoom_level = ? and tile_column = ? and tile_row = ?",
//                                 [NSNumber numberWithUnsignedLongLong:zoom],
//                                 [NSNumber numberWithUnsignedLongLong:x],
//                                 [NSNumber numberWithUnsignedLongLong:y]];
//         
//         if (![self dbHadError:db] && [results next]) {
//             
//             NSData *data = ([[results columnNameToIndexMap] count] ? [results dataForColumn:@"tile_data"] : nil);
//             
//             if (data) {
//                 image = [UIImage imageWithData:data];
//             }
//         }
//         
//         [results close];
//     }];
//    
//    return image;
//}
//
//- (float)minZoom
//{
//    if (_minZoom == -1) {
//        [queue inDatabase:^(FMDatabase *db)
//         {
//             FMResultSet *results = [db executeQuery:@"select min(zoom_level) from tiles"];
//             
//             [results next];
//             
//             if ([self dbHadError:db])
//                 _minZoom = kMBTilesDefaultMinTileZoom;
//             else
//                 _minZoom = [results doubleForColumnIndex:0];
//             
//             [results close];
//         }];
//    }
//    return _minZoom;
//}

-(BOOL)dbHadError:(FMDatabase *)db{
    BOOL result = [db hadError];
    if (result) {
        NSLog(@"DB error %@", [db lastErrorMessage]);
    }
    return result;
}

//- (float)maxZoom
//{
//    if (_maxZoom == -1) {
//        [queue inDatabase:^(FMDatabase *db)
//         {
//             FMResultSet *results = [db executeQuery:@"select max(zoom_level) from tiles"];
//             
//             [results next];
//             if ([self dbHadError:db])
//                 _maxZoom = kMBTilesDefaultMaxTileZoom;
//             else
//                 _maxZoom = [results doubleForColumnIndex:0];
//             
//             [results close];
//         }];
//    }
//    return _maxZoom;
//}

//- (RMSphericalTrapezium)latitudeLongitudeBoundingBox
//{
//    __block RMSphericalTrapezium bounds = kMBTilesDefaultLatLonBoundingBox;
//    
//    [queue inDatabase:^(FMDatabase *db)
//     {
//         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'bounds'"];
//         
//         [results next];
//         
//         NSString *boundsString = [results stringForColumnIndex:0];
//         
//         [results close];
//         
//         if (boundsString)
//         {
//             NSArray *parts = [boundsString componentsSeparatedByString:@","];
//             
//             if ([parts count] == 4)
//             {
//                 bounds.southWest.longitude = [[parts objectAtIndex:0] doubleValue];
//                 bounds.southWest.latitude  = [[parts objectAtIndex:1] doubleValue];
//                 bounds.northEast.longitude = [[parts objectAtIndex:2] doubleValue];
//                 bounds.northEast.latitude  = [[parts objectAtIndex:3] doubleValue];
//             }
//         }
//     }];
//    
//    return bounds;
//}

- (NSString *)legend
{
    __block NSString *legend  = nil;
    
    [queue inDatabase:^(FMDatabase *db)
     {
         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'legend'"];
         
         if ([self dbHadError:db])
             legend = nil;
         
         [results next];
         
         legend = [results stringForColumn:@"value"];
         
         [results close];
     }];
    
    return legend;
}

- (CLLocationCoordinate2D)centerCoordinate
{
    __block CLLocationCoordinate2D centerCoordinate = CLLocationCoordinate2DMake(0, 0);
    
    [queue inDatabase:^(FMDatabase *db)
     {
         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'center'"];
         [self dbHadError:db];
         
         [results next];
         
         if ([results stringForColumn:@"value"] && [[[results stringForColumn:@"value"] componentsSeparatedByString:@","] count] >= 2)
             centerCoordinate = CLLocationCoordinate2DMake([[[[results stringForColumn:@"value"] componentsSeparatedByString:@","] objectAtIndex:1] doubleValue],
                                                           [[[[results stringForColumn:@"value"] componentsSeparatedByString:@","] objectAtIndex:0] doubleValue]);
         
         [results close];
     }];
    
    return centerCoordinate;
}

- (float)centerZoom
{
    __block CGFloat centerZoom = [self minZoom];
    
    [queue inDatabase:^(FMDatabase *db)
     {
         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'center'"];
         [self dbHadError:db];
         
         [results next];
         
         if ([results stringForColumn:@"value"] && [[[results stringForColumn:@"value"] componentsSeparatedByString:@","] count] >= 3)
             centerZoom = [[[[results stringForColumn:@"value"] componentsSeparatedByString:@","] objectAtIndex:2] floatValue];
         
         [results close];
     }];
    
    return centerZoom;
}

//- (BOOL)coversFullWorld
//{
//    RMSphericalTrapezium ownBounds     = [self latitudeLongitudeBoundingBox];
//    RMSphericalTrapezium defaultBounds = kMBTilesDefaultLatLonBoundingBox;
//    
//    if (ownBounds.southWest.longitude <= defaultBounds.southWest.longitude + 10 &&
//        ownBounds.northEast.longitude >= defaultBounds.northEast.longitude - 10)
//        return YES;
//    
//    return NO;
//}

- (void)didReceiveMemoryWarning
{
//    NSLog(@"*** didReceiveMemoryWarning in %@", [self class]);
}

//- (NSString *)uniqueTilecacheKey
//{
//    return _uniqueTilecacheKey;
//}

- (NSString *)shortName
{
    __block NSString *shortName = nil;
    
    [queue inDatabase:^(FMDatabase *db)
     {
         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'name'"];
         
         if ([self dbHadError:db])
             shortName = nil;
         
         [results next];
         
         shortName = [results stringForColumnIndex:0];
         
         [results close];
     }];
    
    return shortName;
}

- (NSString *)longDescription
{
    __block NSString *description = nil;
    
    [queue inDatabase:^(FMDatabase *db)
     {
         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'description'"];
         
         if ([self dbHadError:db])
             description = nil;
         
         [results next];
         
         description = [results stringForColumnIndex:0];
         
         [results close];
     }];
    
    return [NSString stringWithFormat:@"%@ - %@", [self shortName], description];
}

- (NSString *)shortAttribution
{
    __block NSString *attribution = nil;
    
    [queue inDatabase:^(FMDatabase *db)
     {
         FMResultSet *results = [db executeQuery:@"select value from metadata where name = 'attribution'"];
         
         if ([self dbHadError:db])
             attribution = @"Unknown MBTiles attribution";
         
         [results next];
         
         attribution = [results stringForColumnIndex:0];
         
         [results close];
     }];
    
    return attribution;
}

- (NSString *)longAttribution
{
    return [NSString stringWithFormat:@"%@ - %@", [self shortName], [self shortAttribution]];
}
@end
