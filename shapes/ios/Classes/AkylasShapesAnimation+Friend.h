#import "AkylasShapesAnimation.h"
#import "TiHLSAnimation+Friend.h"

@class ShapeProxy;
@interface AkylasShapesAnimation (Friend)

@property (nonatomic, retain) ShapeProxy* shapeProxy;
@property (nonatomic, assign) BOOL autoreverse;
@property (nonatomic, assign) BOOL restartFromBeginning;

@end
