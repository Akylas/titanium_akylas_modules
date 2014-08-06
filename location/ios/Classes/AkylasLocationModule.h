#import "TiModule.h"
#import "LocationTracker.h"

@interface AkylasLocationModule : TiModule <LocationTrackerDelegate>
{

}
@property(nonatomic,assign) double minAge;
@property(nonatomic,assign) double backgroundMinAge;
@property(nonatomic,assign) BOOL useSignificantChanges;
@property (nonatomic) BOOL useSignificantChangesInBackground;
@property(nonatomic,assign) CLLocationDistance distanceFilter;
@property(nonatomic,assign) double desiredAccuracy;

@property(nonatomic,readonly) NSNumber *ACCURACY_BEST;
@property(nonatomic,readonly) NSNumber *ACCURACY_HIGH;
@property(nonatomic,readonly) NSNumber *ACCURACY_NEAREST_TEN_METERS;
@property(nonatomic,readonly) NSNumber *ACCURACY_HUNDRED_METERS;
@property(nonatomic,readonly) NSNumber *ACCURACY_KILOMETER;
@property(nonatomic,readonly) NSNumber *ACCURACY_LOW;
@property(nonatomic,readonly) NSNumber *ACCURACY_THREE_KILOMETERS;
@property(nonatomic,readonly) NSNumber *ACCURACY_BEST_FOR_NAVIGATION; //Since 3.1.0
@property(nonatomic,readonly) NSString *APP_PROPERTY_SCRIPT;
@property(nonatomic,readonly) NSString *APP_PROPERTY_LAST_LOCATION;
@end
