#import "TiModule.h"

#define PROP_ADSIZE @"adSize"
#define PROP_ADUNITID @"adUnitId"
#define SETTINGS_APP_ID @"mopub.appid"

@interface AkylasMopubModule : TiModule
//+(NSDictionary*) dictionaryFromData:(SmartAdServerAd*)data;
//+(SmartAdServerAd*) dataFromDictionary:(NSDictionary*)dict;


@property(nonatomic,readonly) NSValue *SIZE_BANNER;
@property(nonatomic,readonly) NSValue *SIZE_MEDIUM_RECT;
@property(nonatomic,readonly) NSValue *SIZE_LEADERBOARD;
@property(nonatomic,readonly) NSValue *SIZE_WIDE_SKYSCRAPER;

@property(nonatomic,readonly) NSString *PROPERTY_ADSIZE;

@end
