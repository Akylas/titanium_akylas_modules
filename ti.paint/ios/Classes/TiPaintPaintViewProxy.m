/**
 * Ti.Paint Module
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiPaintPaintViewProxy.h"
#import "TiPaintPaintView.h"
#import "TiUtils.h"

@implementation TiPaintPaintViewProxy


-(void)clear:(id)args
{
	[[self view] performSelectorOnMainThread:@selector(clear:) withObject:args waitUntilDone:NO];
}
-(id)toBlob:(id)args
{
    if (view) {
        UIImageView* imageView = [(TiPaintPaintView*)view imageView];
        if ([imageView image]) {
            return [[[TiBlob alloc] initWithImage:[imageView image]] autorelease];
        }
    }
    return nil;
    
}

@end
