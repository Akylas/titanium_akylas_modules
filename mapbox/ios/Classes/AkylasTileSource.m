#import "AkylasTileSource.h"
#import "AkylasMapboxModule.h"
#import "AkylasMapboxTileSourceProxy.h"

@implementation AkylasTileSource
@synthesize tileSource, proxy;

-(void)dealloc
{
    RELEASE_TO_NIL(tileSource)
	[super dealloc];
}

+tileSourceWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy
{
    if (source == nil) return nil;
    if ([source isKindOfClass:[AkylasMapboxTileSourceProxy class]]) return [((AkylasMapboxTileSourceProxy*)source) tileSource];
    AkylasTileSource* result = [[AkylasTileSource alloc] init];
    result.tileSource = (id<RMTileSource>)[AkylasMapboxModule sourceFromObject:source proxy:proxy];
    [result.tileSource setCacheable:YES];
    return [result autorelease];
}


@end
