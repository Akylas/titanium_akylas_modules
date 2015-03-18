#import "AkylasMopubModule.h"

#import "TiHost.h"
#import "SBJSON.h"
#import "ListenerEntry.h"
#import "TiApp.h"
#import "UIColor+Expanded.h"
#import "MPIdentityProvider.h"
#import "MPAdConversionTracker.h"
#import "MPConstants.h"

#import <Foundation/Foundation.h>
#import <objc/runtime.h>

//extern NSString * const TI_APPLICATION_DEPLOYTYPE;

@implementation AkylasMopubModule

#if defined(DEBUG) || defined(DEVELOPER)

-(void)_restart:(id)unused
{
    TiThreadPerformOnMainThread(^{
        [[[TiApp app] controller] shutdownUi:self];
    }, NO);
}

-(void)_resumeRestart:(id)unused
{
//    UIApplication * app = [UIApplication sharedApplication];
}

#endif

-(void)dealloc
{
	[[NSNotificationCenter defaultCenter] removeObserver:self];
	[super dealloc];
}

- (void)_configure
{
	[super _configure];
}


// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"3dee9b25-1db0-4285-adc5-d8e96112e42f";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.mopub";
}

//-(NSNumber*)proximityState
//{
//	return NUMBOOL([UIDevice currentDevice].proximityState);
//}


#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	[super didReceiveMemoryWarning:notification];
}

-(void)willShutdown:(id)sender;
{
}

-(void)willShutdownContext:(NSNotification*)note
{
	// we have to check and see if this context has any listeners
	// that are registered at the global scope and that haven't been
	// removed and if so, we need to remove them since their context
	// is toast.
}

-(void)startup
{
	WARN_IF_BACKGROUND_THREAD_OBJ;	//NSNotificationCenter is not threadsafe!
    NSNotificationCenter * nc = [NSNotificationCenter defaultCenter];
    [nc addObserver:self selector:@selector(willShutdown:) name:kTiWillShutdownNotification object:nil];
    [nc addObserver:self selector:@selector(willShutdownContext:) name:kTiContextShutdownNotification object:nil];
    
    NSUserDefaults *defaultsObject = [NSUserDefaults standardUserDefaults];
    NSString* appId =[defaultsObject stringForKey:SETTINGS_APP_ID];
    if (appId) {
        [[MPAdConversionTracker sharedConversionTracker] reportApplicationOpenForApplicationID:appId];
    }

    [super startup];
}

-(void)paused:(id)sender
{
}

-(void)suspend:(id)sender
{

}

-(void)resume:(id)sender
{
}

-(void)resumed:(id)sender
{
}

#pragma mark Public APIs

#define MAKE_SIZE_SYSTEM_PROP(name,map) \
-(NSValue*)name \
{\
return [NSValue valueWithCGSize:map];\
}\

MAKE_SIZE_SYSTEM_PROP(SIZE_BANNER,MOPUB_BANNER_SIZE);
MAKE_SIZE_SYSTEM_PROP(SIZE_MEDIUM_RECT,MOPUB_MEDIUM_RECT_SIZE);
MAKE_SIZE_SYSTEM_PROP(SIZE_LEADERBOARD,MOPUB_LEADERBOARD_SIZE);
MAKE_SIZE_SYSTEM_PROP(SIZE_WIDE_SKYSCRAPER, MOPUB_WIDE_SKYSCRAPER_SIZE);
MAKE_SYSTEM_PROP(PROPERTY_ADSIZE, PROP_ADSIZE);

// this is generated for your module, please do not change it
-(id)deviceIdentifier
{
	return [MPIdentityProvider identifier];
}

//-(void)setSiteIdAndBaseUrl:(id)args
//{
//    NSNumber* siteId = nil;
//    NSString* baseUrl = nil;
//    ENSURE_ARG_AT_INDEX(siteId, args, 0, NSNumber);
//    ENSURE_ARG_AT_INDEX(baseUrl, args, 1, NSString);
//    [SmartAdServerView setSiteID:siteId baseURL:baseUrl];
//}


//MAKE_SYSTEM_STR(EVENT_ACCESSIBILITY_ANNOUNCEMENT,@"accessibilityannouncement");
//MAKE_SYSTEM_STR(EVENT_ACCESSIBILITY_CHANGED,@"accessibilitychanged");


+(NSDictionary*)dictFromSize:(CGSize)size
{
    return @{@"width": NUMFLOAT(size.width), @"height": NUMFLOAT(size.height)};
}

+(NSString*)colorString:(UIColor*)color
{
    if (color) {
        return [NSString stringWithFormat:@"#%@", [color hexStringValue]];
    }
    return nil;
}

//+(NSDictionary*) dictionaryFromData:(SmartAdServerAd*)data
//{
//    if(data == nil) return nil;
//    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
//    unsigned int outCount, i;
//    objc_property_t *properties = class_copyPropertyList([SmartAdServerAd class], &outCount);
//    for (i = 0; i < outCount; i++) {
//        objc_property_t property = properties[i];
//        const char *propName = property_getName(property);
//        if(propName) {
//            NSString *propertyName = [NSString stringWithUTF8String:propName];
//            id value = [data valueForKey:propertyName];
//            if (value) {
//                if ([value isKindOfClass:[NSURL class]]) {
//                    value = [value absoluteURL];
//                }
//                else if ([value isKindOfClass:[UIColor class]]) {
//                    value = [self colorString:value];
//                }
//                else if ([value isKindOfClass:[NSDate class]]) {
//                    value = [TiUtils UTCDateForDate:value];
//                }
//                else if ([value isKindOfClass:[NSValue class]]) {
//                    if(strcmp((const char *)[value objCType],(const char *)@encode(CGSize))==0) {
//                        value = [TiUtils sizeToDictionary:[value CGSizeValue]];
//                    } else if(strcmp((const char *)[value objCType],(const char *)@encode(CGRect))==0) {
//                        value = [TiUtils rectToDictionary:[value CGRectValue]];
//                    }
//                }
//                [dict setObject:value forKey:propertyName];
//            }
//        }
//    }
//    free(properties);
//    return dict;
//}
//
//+(SmartAdServerAd*) dataFromDictionary:(NSDictionary*)dict;
//{
//    SmartAdServerAd* data = [[SmartAdServerAd alloc] init];
//    [dict enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
//        [data setValue:obj forKey:key];
//    }];
//    return data;
//}

@end
