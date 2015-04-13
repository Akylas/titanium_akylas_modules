//
//  ReusableView.h
//  akylas.map
//
//  Created by Martin Guillon on 05/09/2014.
//
//

#import <Foundation/Foundation.h>

@protocol ReusableViewProtocol <NSObject>

-(NSString*) reuseIdentifier;
-(void) prepareForReuse;
-(void)configurationStart;
-(void)configurationSet;

- (BOOL)canApplyDataItem:(NSDictionary *)otherItem;
@property (nonatomic, readwrite, retain) NSDictionary *dataItem;

@end
