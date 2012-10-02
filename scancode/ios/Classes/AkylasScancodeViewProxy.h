
#import "TiViewProxy.h"

#import "AkylasScancodeViewController.h"
extern NSDictionary *const symbolDict;
@interface AkylasScancodeViewProxy : TiViewProxy<AkylasScancodeViewControllerDelegate>{

    AkylasScancodeViewController* _controller;
    // OK, this is ridiculous.  Sometimes (always?) views which are made invisible and removed are relayed.
	// This means their views are recreated.  For movie players, this means the movie is reloaded and plays.
	// We need some internal way whether or not to check if it's OK to create a view - this is it.
	BOOL reallyAttached;
}
- (void)setTorch:(BOOL)status;
- (void) setCenteredCropRect:(BOOL)value;
- (void) setCropRect:(NSDictionary*)value;
- (void) setReaders:(NSArray*)value;
- (void) setCameraPosition:(id)value;
@end
