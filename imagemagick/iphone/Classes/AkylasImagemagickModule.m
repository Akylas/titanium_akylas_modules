/**
 * akylas.imagemagick
 *
 * Created by Your Name
 * Copyright (c) 2017 Your Company. All rights reserved.
 */

#import "AkylasImagemagickModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation AkylasImagemagickModule

#pragma mark Internal

// This is generated for your module, please do not change it
- (id)moduleGUID
{
	return @"dacc5179-0cdd-4a66-9cf1-bab009bceb95";
}

// This is generated for your module, please do not change it
- (NSString *)moduleId
{
	return @"akylas.imagemagick";
}

#pragma mark Lifecycle

- (void)startup
{
	// This method is called when the module is first loaded
	// You *must* call the superclass
	[super startup];
	NSLog(@"[DEBUG] %@ loaded",self);
}

#pragma Public APIs

- (id)example:(id)args
{
	// Example method. 
	// Call with "MyModule.example(args)"
	return @"hello world";
}

- (id)exampleProp
{
	// Example property getter. 
	// Call with "MyModule.exampleProp" or "MyModule.getExampleProp()"
	return @"Titanium rocks!";
}

- (id)posterize:(id)value
{
}

@end
