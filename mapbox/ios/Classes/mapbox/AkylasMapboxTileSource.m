#import "AkylasMapboxTileSource.h"
#import "AkylasMapModule.h"
#import "AkylasMapTileSourceProxy.h"

@implementation AkylasMapboxTileSource
@synthesize tileSource, proxy;

-(void)dealloc
{
    RELEASE_TO_NIL(tileSource)
	[super dealloc];
}

+(AkylasMapboxTileSource*)tileSourceWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy
{
    if (source == nil) return nil;
    if ([source isKindOfClass:[AkylasMapTileSourceProxy class]]) return [((AkylasMapTileSourceProxy*)source) mpTileSource];
    AkylasMapboxTileSource* result = [[AkylasMapboxTileSource alloc] init];
    result.tileSource = (id<RMTileSource>)[AkylasMapModule sourceFromObject:source proxy:proxy];
    [result.tileSource setCacheable:YES];
    return [result autorelease];
}

+(AkylasMapTileSourceProxy*)tileSourceProxyWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy
{
    if (source == nil) return nil;
    if ([source isKindOfClass:[AkylasMapTileSourceProxy class]]) return ((AkylasMapTileSourceProxy*)source);
    else return [[[AkylasMapTileSourceProxy alloc] initWithSource:source] autorelease];
}



@end
