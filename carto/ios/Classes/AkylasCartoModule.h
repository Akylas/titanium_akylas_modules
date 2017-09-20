/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasMapBaseModule.h"

NTMapBounds* boundsFromRegion(AkRegion trapez);
@interface AkylasCartoModule : AkylasMapBaseModule
{
}
+(NTEPSG3857 *)baseProjection;

@end
