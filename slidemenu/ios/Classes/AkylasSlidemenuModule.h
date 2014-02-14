#import "TiModule.h"

@interface AkylasSlidemenuModule : TiModule 
{
}
@property(nonatomic,readonly) NSNumber *PANNING_NONE;
@property(nonatomic,readonly) NSNumber *PANNING_ALL_VIEWS;
@property(nonatomic,readonly) NSNumber *PANNING_CENTER_VIEW;
@property(nonatomic,readonly) NSNumber *PANNING_BORDERS;


@property(nonatomic,readonly) NSNumber *ANIMATION_NONE;
@property(nonatomic,readonly) NSNumber *ANIMATION_ZOOM;
@property(nonatomic,readonly) NSNumber *ANIMATION_SCALE;
@property(nonatomic,readonly) NSNumber *ANIMATION_SLIDE;


@property(nonatomic,readonly) NSString *PROPERTY_ANIMATION_LEFT;
@property(nonatomic,readonly) NSString *PROPERTY_ANIMATION_RIGHT;
@property(nonatomic,readonly) NSString *PROPERTY_LEFT_VIEW;
@property(nonatomic,readonly) NSString *PROPERTY_LEFT_VIEW_DISPLACEMENT;
@property(nonatomic,readonly) NSString *PROPERTY_LEFT_VIEW_WIDTH;
@property(nonatomic,readonly) NSString *PROPERTY_RIGHT_VIEW;
@property(nonatomic,readonly) NSString *PROPERTY_RIGHT_VIEW_DISPLACEMENT;
@property(nonatomic,readonly) NSString *PROPERTY_RIGHT_VIEW_WIDTH;
@property(nonatomic,readonly) NSString *PROPERTY_PANNING_MODE;
@property(nonatomic,readonly) NSString *PROPERTY_CENTER_VIEW;
@property(nonatomic,readonly) NSString *PROPERTY_FADING;

@end