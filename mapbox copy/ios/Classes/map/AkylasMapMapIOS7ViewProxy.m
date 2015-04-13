/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapMapIOS7ViewProxy.h"
#import "AkylasMapMapIOS7View.h"

@implementation AkylasMapMapIOS7ViewProxy

-(AkylasMapCameraProxy*)camera
{
    return [(AkylasMapMapIOS7View *)[self view] camera];
}

-(void)animateCamera:(id)args
{
    [(AkylasMapMapIOS7View *)[self view] animateCamera:args];
}

//-(void)showAnnotations:(id)args
//{
//    [(AkylasMapMapIOS7View *)[self view] showAnnotations:args];
//}

@end
