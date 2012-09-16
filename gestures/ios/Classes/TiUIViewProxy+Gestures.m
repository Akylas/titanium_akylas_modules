
#import "TiUtils.h"
#import "TiUIViewProxy+Gestures.h"

#include <objc/runtime.h> //objc runtime api’s

#define RECOGNIZE_SIMULTANEOUSLY (1 << 16)
static NSString *GESTURE_NAME_KEY = @"gestureName_";
static NSString *VIEW_GESTURE_TYPES_KEY = @"gestureTypes_";
static NSString *VIEW_GESTURES_KEY = @"gestures_";

@implementation UIGestureRecognizer (Gesture_name)

-(void)setName: (NSString *)myString
{
    objc_setAssociatedObject(self, &GESTURE_NAME_KEY, myString, OBJC_ASSOCIATION_RETAIN);
}

-(NSString *)name
{
return (NSString *)objc_getAssociatedObject(self, &GESTURE_NAME_KEY);
}


@end

@implementation TiViewProxy (TiUIViewProxy_Gestures)

-(void) _initTiUIViewProxy_Gestures
{
    NSArray* arr = objc_getAssociatedObject(self, &VIEW_GESTURE_TYPES_KEY); 
    if (!arr) 
    { 
        arr = [NSArray arrayWithObjects:@"pinch", @"pan", @"tap", @"swipe", @"longpress", @"rotate", nil];
        objc_setAssociatedObject(self, &VIEW_GESTURE_TYPES_KEY, arr, OBJC_ASSOCIATION_RETAIN);
    }
    
    NSMutableDictionary* dic = objc_getAssociatedObject(self, &VIEW_GESTURES_KEY); 
    if (!dic) 
    { 
        dic = [NSMutableDictionary dictionary];
        objc_setAssociatedObject(self, &VIEW_GESTURES_KEY, dic, OBJC_ASSOCIATION_RETAIN);
    }
}

///type: pinch, pan, tap, swipe, longpress, rotate
- (void) removeGesture:(id)args
{
    ENSURE_SINGLE_ARG(args, NSDictionary);
    
    if (![args objectForKey:@"name"] )
    {
        NSLog (@"[ERROR] [removeGesture] You must give a name to the gesture recognizer");
        return;
    }
    NSMutableDictionary* gestures = objc_getAssociatedObject(self, &VIEW_GESTURES_KEY); 
    NSString* name = [TiUtils stringValue:[args objectForKey:@"name"]];
    
    UIGestureRecognizer* gesture = [gestures objectForKey:[TiUtils stringValue:[args objectForKey:@"name"]]];
    if (gesture ==  nil)
    {
        NSLog (@"[ERROR] [removeGesture] gesture doesnt exist");
        return;
    }
    [self removeGesture:gesture];
    [gestures removeObjectForKey:name];
}

- (void) addFailureDependency:(id) args
{
    ENSURE_SINGLE_ARG(args, NSDictionary);
    if (![args objectForKey:@"name"] )
    {
        NSLog (@"[ERROR] [addGesture] you must give of the gesture asking for dependency");
        return;
    }
    if (![args objectForKey:@"failer"] )
    {
        NSLog (@"[ERROR] [addGesture] You must give the name of gesture required to fail");
        return;
    }
    
    NSMutableDictionary* gestures = objc_getAssociatedObject(self, &VIEW_GESTURES_KEY); 
    NSString* gestureName = [TiUtils stringValue:[args objectForKey:@"name"]];
    
    if (![gestures objectForKey:gestureName])
    {
        NSLog (@"[ERROR] [addFailureDependency] no gesture named %@", gestureName);
        return;
    }
    
    
    NSString* failerName = [TiUtils stringValue:[args objectForKey:@"failer"]];
    
    if (![gestures objectForKey:failerName])
    {
        NSLog (@"[ERROR] [addFailureDependency] no gesture named %@", failerName);
        return;
    }
    UIGestureRecognizer* gesture1 = [gestures objectForKey:gestureName];
    UIGestureRecognizer* gesture2 = [gestures objectForKey:failerName];
    if (gesture1 && gesture2)
    {
        [gesture1 requireGestureRecognizerToFail: gesture2];
    }
    else
    {
        NSLog (@"[ERROR] [addFailureDependency] something went wrong");
    }

}

///type: pinch, pan, tap, swipe, longpress, rotate
- (void) addGesture:(id)args
{
    ENSURE_SINGLE_ARG(args, NSDictionary);
        
    if (![args objectForKey:@"name"] )
    {
        NSLog (@"[ERROR] [addGesture] You must give a name to the gesture recognizer");
        return;
    }
    if (![args objectForKey:@"type"] )
    {
        NSLog (@"[ERROR] [addGesture] You must set a type for the gesture recognizer");
        return;
    }
    
    [self _initTiUIViewProxy_Gestures];
    
    NSString* name = [TiUtils stringValue:[args objectForKey:@"name"]];
    
    NSMutableDictionary* gestures = objc_getAssociatedObject(self, &VIEW_GESTURES_KEY); 
    
    NSInteger type = [objc_getAssociatedObject(self, &VIEW_GESTURE_TYPES_KEY) indexOfObject:[TiUtils stringValue:[args objectForKey:@"type"]]];

    if (type == NSNotFound)
    {
        NSLog (@"[ERROR] [addGesture] wrong gesture type");
        return;
    }
    
    if ([gestures objectForKey:[TiUtils stringValue:[args objectForKey:@"name"]]])
    {
        NSLog (@"[ERROR] [addGesture] this gesture already exists");
        return;
    }
    
    switch (type) {
        case 0: // pinch
        {
//            NSLog (@"[addGesture] adding pinch gesture of type %@ with name %@", [TiUtils stringValue:[args objectForKey:@"type"]], name);
            UIPinchGestureRecognizer* gesture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinchGesture:)];
            gesture.name = name;
            [gesture setDelegate:self];
            [gestures setObject:gesture forKey:name];
            TiThreadPerformOnMainThread(^{[self.view addGestureRecognizer:gesture];}, NO);
            break;
        }
        case 1: // pan
        {
//            NSLog (@"[addGesture] adding pan gesture of type %@ with name %@", [TiUtils stringValue:[args objectForKey:@"type"]], name);
            UIPanGestureRecognizer* gesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePanGesture:)];
            gesture.name = name;
            
            if ([args objectForKey:@"maxtouches"])
                [gesture setMaximumNumberOfTouches:[TiUtils intValue:[args objectForKey:@"maxtouches"]]];
            if ([args objectForKey:@"mintouches"])
                [gesture setMinimumNumberOfTouches:[TiUtils intValue:[args objectForKey:@"mintouches"]]];
            [gesture setDelegate:self];
            [gestures setObject:gesture forKey:name];
            TiThreadPerformOnMainThread(^{[self.view addGestureRecognizer:gesture];}, NO);
            break;
        }
        case 2: // tap
        {
//            NSLog (@"[addGesture] adding tap gesture of type %@ with name %@", [TiUtils stringValue:[args objectForKey:@"type"]], name);
            UITapGestureRecognizer* gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
            gesture.name = name;
            
            if ([args objectForKey:@"nbtaps"])
                [gesture setNumberOfTapsRequired:[TiUtils intValue:[args objectForKey:@"nbtaps"]]];
            if ([args objectForKey:@"nbtouches"])
                [gesture setNumberOfTouchesRequired:[TiUtils intValue:[args objectForKey:@"nbtouches"]]];
            
            [gesture setDelegate:self];
            [gestures setObject:gesture forKey:name];
            TiThreadPerformOnMainThread(^{[self.view addGestureRecognizer:gesture];}, NO);
            
//            [gesture release];
            break;
        }
        case 3: // swipe
        {
//            NSLog (@"[addGesture] adding swipe gesture of type %@ with name %@", [TiUtils stringValue:[args objectForKey:@"type"]], name);
            UISwipeGestureRecognizer* gesture = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeGesture:)];
            gesture.name = name;
            
            if ([args objectForKey:@"direction"])
            {
                int direction = UISwipeGestureRecognizerDirectionRight;
                NSString* dirString = [TiUtils stringValue:[args objectForKey:@"direction"]];
                if ([dirString isEqualToString:@"left"])
                    direction = UISwipeGestureRecognizerDirectionLeft;
                else if ([dirString isEqualToString:@"up"])
                    direction = UISwipeGestureRecognizerDirectionUp;
                else if ([dirString isEqualToString:@"down"])
                    direction = UISwipeGestureRecognizerDirectionDown;
                [gesture setDirection:direction];
            }
            if ([args objectForKey:@"nbtouches"])
                [gesture setNumberOfTouchesRequired:[TiUtils intValue:[args objectForKey:@"nbtouches"]]];
            
            [gesture setDelegate:self];
            [gestures setObject:gesture forKey:name];
            TiThreadPerformOnMainThread(^{[self.view addGestureRecognizer:gesture];}, NO);
//            [gesture release];
            break;
        }
        case 4: // longpress
        {
//            NSLog (@"[addGesture] adding longpress gesture of type %@ with name %@", [TiUtils stringValue:[args objectForKey:@"type"]], name);
            UILongPressGestureRecognizer* gesture = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPressGesture:)];
            gesture.name = name;
            
            if ([args objectForKey:@"nbtaps"])
                [gesture setNumberOfTapsRequired:[TiUtils intValue:[args objectForKey:@"nbtaps"]]];
            if ([args objectForKey:@"nbtouches"])
                [gesture setNumberOfTouchesRequired:[TiUtils intValue:[args objectForKey:@"nbtouches"]]];
            if ([args objectForKey:@"minduration"])
                [gesture setMinimumPressDuration:[TiUtils floatValue:[args objectForKey:@"minduration"]]];
            if ([args objectForKey:@"allowablemovement"])
                [gesture setAllowableMovement:[TiUtils floatValue:[args objectForKey:@"allowablemovement"]]];
            
            [gesture setDelegate:self];
            [gestures setObject:gesture forKey:name];
            TiThreadPerformOnMainThread(^{[self.view addGestureRecognizer:gesture];}, NO);
//            [gesture release];
            break;
        }
        case 5: // rotate
        {
//            NSLog (@"[addGesture] adding rotate gesture of type %@ with name %@", [TiUtils stringValue:[args objectForKey:@"type"]], name);
            UIRotationGestureRecognizer* gesture = [[UIRotationGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPressGesture:)];
            gesture.name = name;
            
            [gesture setDelegate:self];
            [gestures setObject:gesture forKey:name];
            TiThreadPerformOnMainThread(^{[self.view addGestureRecognizer:gesture];}, NO);
//            [gesture release];
            break;
        }    
        default:
            break;
    }
    objc_setAssociatedObject(self, &VIEW_GESTURES_KEY, gestures, OBJC_ASSOCIATION_RETAIN);
}


- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return (self.view.tag & RECOGNIZE_SIMULTANEOUSLY);
}

- (void)handlePanGesture:(UIPanGestureRecognizer *)sender
{
    
    CGPoint translation = [sender translationInView:self.view.superview];
    CGPoint location = [sender locationInView:self.view.superview];
    CGPoint velocity = [sender velocityInView:self.view.superview];
    
    NSDictionary *args = [NSDictionary dictionaryWithObjectsAndKeys:
                          [NSNumber numberWithFloat:[sender state]], @"state",
                          [NSNumber numberWithFloat:[sender numberOfTouches]], @"nbtouches",
                          [[[TiPoint alloc] initWithPoint:translation] autorelease], @"translation",
                          [[[TiPoint alloc] initWithPoint:location] autorelease], @"location",
                          [[[TiPoint alloc] initWithPoint:velocity] autorelease], @"velocity", nil];
    
    
    if([self _hasListeners:sender.name]){
        [self fireEvent:sender.name withObject:args];
    }
    
}

- (void)handleRotationGesture:(UIRotationGestureRecognizer *)sender
{
    NSDictionary *args = [NSDictionary dictionaryWithObjectsAndKeys:
                          [NSNumber numberWithFloat:[sender state]], @"state",
                          [NSNumber numberWithFloat:[sender numberOfTouches]], @"nbtouches",
               [NSNumber numberWithFloat:[sender rotation]], @"rotation",
               [NSNumber numberWithFloat:[sender velocity]], @"velocity", nil];
    
    if([self _hasListeners:sender.name]){
        [self fireEvent:sender.name withObject:args];
    }
}

- (void)handlePinchGesture:(UIPinchGestureRecognizer *)sender
{
    NSDictionary *args = [NSDictionary dictionaryWithObjectsAndKeys:
                          [NSNumber numberWithFloat:[sender state]], @"state",
                          [NSNumber numberWithFloat:[sender numberOfTouches]], @"nbtouches",
                          [NSNumber numberWithFloat:[sender scale]], @"scale",
                          [NSNumber numberWithFloat:[sender velocity]], @"velocity", nil];
    
    if([self _hasListeners:sender.name]){
        [self fireEvent:sender.name withObject:args];
    }
}

- (void)handleTapGesture:(UITapGestureRecognizer *)sender
{
    if ([sender state] == UIGestureRecognizerStateRecognized)
    {

        CGPoint location = [sender locationInView:self.view.superview];
        NSDictionary *args = [NSDictionary dictionaryWithObjectsAndKeys:
                              [NSNumber numberWithFloat:[sender numberOfTouches]], @"nbtouches",
                              [[[TiPoint alloc] initWithPoint:location] autorelease], @"location",
                              [NSNumber numberWithFloat:[sender state]], @"state", nil];
        
        if([self _hasListeners:sender.name]){
            [self fireEvent:sender.name withObject:args];
        }
    }
}

- (void)handleLongPressGesture:(UILongPressGestureRecognizer *)sender
{
    if ([sender state] == UIGestureRecognizerStateRecognized)
    {
        NSDictionary *args = [NSDictionary dictionaryWithObjectsAndKeys:
                              [NSNumber numberWithFloat:[sender numberOfTouches]], @"nbtouches",
                              [NSNumber numberWithFloat:[sender state]], @"state", nil];
        
        if([self _hasListeners:sender.name]){
            [self fireEvent:sender.name withObject:args];
        }
    }
}

- (void)handleSwipeGesture:(UISwipeGestureRecognizer *)sender
{
    
    NSDictionary *args = [NSDictionary dictionaryWithObjectsAndKeys:
                          [NSNumber numberWithFloat:[sender numberOfTouches]], @"nbtouches",
                          [NSNumber numberWithFloat:[sender state]], @"state", nil];
    
    if([self _hasListeners:sender.name]){
        [self fireEvent:sender.name withObject:args];
    }
}

@end
