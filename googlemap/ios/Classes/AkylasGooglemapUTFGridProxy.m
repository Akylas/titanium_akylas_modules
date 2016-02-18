//
//  AkylasUTFGridProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 02/02/2016.
//
//

#import "AkylasGooglemapUTFGridProxy.h"
#import "AkylasUTFGridTileLayer.h"
#import "AkylasMapBaseModule.h"
#import "TiUtils.h"
@interface AkylasGooglemapTileSourceProxy()
{
@protected
    GMSTileLayer*  _gTileLayer;
    BOOL _needsClearCache;
    NSString* _url;
}
@end

@implementation AkylasGooglemapUTFGridProxy
-(void)_configure
{
    [super _configure];
    self.canChangeTileSize = NO;
}

-(GMSTileLayer*)tileLayer
{
//    _url = [TiUtils stringValue:[self valueForKey:@"url"]];
    GMSTileLayer* result = nil;
    if ([self valueForKey:@"url"]) {
        result = [[AkylasUTFGridTileLayer alloc] initWithConstructor:nil];
        ((AkylasUTFGridTileLayer*)result).url = [TiUtils stringValue:[self valueForKey:@"url"]];
    }
    return result;
}

-(id)getData:(id)args {
    GMSTileLayer* layer = [self gTileLayer];
    if (!IS_OF_CLASS(layer, AkylasUTFGridTileLayer)) {
        return nil;
    }
    ENSURE_TYPE(args, NSArray)
    id pos = nil;
    NSNumber *zoom = nil;
    ENSURE_ARG_AT_INDEX(pos, args, 0, NSObject);
    ENSURE_ARG_OR_NIL_AT_INDEX(zoom, args, 1, NSNumber);
    return [(AkylasUTFGridTileLayer*)layer getData:[AkylasMapBaseModule locationFromObject:pos] atZoom:[zoom floatValue]];
}
@end
