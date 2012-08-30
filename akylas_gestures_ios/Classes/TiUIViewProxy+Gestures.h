
#import "TiViewProxy.h"

@interface UIGestureRecognizer (Gesture_name)
-(void)setName:(NSString *)myString;
-(NSString *)name;
@end

@interface TiViewProxy (TiUIViewProxy_Gestures)<UIGestureRecognizerDelegate>

- (void)addGesture:(id)args;
- (void)addFailureDependency:(id)args;
- (void)removeGesture:(id)args;

@end
