//
//  AkylasLeveldbIteratorProxy.h
//  akylas.leveldb
//
//  Created by Martin Guillon on 11/05/2017.
//
//
#import "leveldb/db.h"
#import "leveldb/options.h"
#import "TiProxy.h"

@interface AkylasLeveldbIteratorProxy : TiProxy
-(id)_initWithPageContext:(id<TiEvaluator>)context database:(AkylasLeveldbDatabaseProxy*)db args:(NSArray*)args;

@end
