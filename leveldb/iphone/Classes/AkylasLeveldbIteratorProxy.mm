//
//  AkylasLeveldbIteratorProxy.m
//  akylas.leveldb
//
//  Created by Martin Guillon on 11/05/2017.
//
//
#include "Common.h"

#import "AkylasLeveldbIteratorProxy.h"
#import "AkylasLeveldbDatabaseProxy.h"

@implementation AkylasLeveldbIteratorProxy
{
    AkylasLeveldbDatabaseProxy* _db;
    leveldb::Iterator* _iter;
}

-(leveldb::Iterator*)getIterator {
    if (_iter == nil) {
        leveldb::ReadOptions readOptions = leveldb::ReadOptions::ReadOptions();
        leveldb::Iterator * iter = [_db getDb]->NewIterator(readOptions);
        
    }
}

-(id)_initWithPageContext:(id<TiEvaluator>)context database:(AkylasLeveldbDatabaseProxy*)db args:(NSArray*)args
{
    _db = [db retain];
}

-(void)seek:(id)args
{
    leveldb::Slice k = KeyFromStringOrData(args);
    _iter->Seek(k);
}

-(void)next:(id)args
{
    leveldb::Slice k = KeyFromStringOrData(args);
    _iter->Next();
}

-(void)dealloc
{
    // release any resources that have been retained by the module
    [super dealloc];
    RELEASE_TO_NIL(_db)
    if (_iter) {
        delete _iter;
        _iter = nil;
    }
    
}

@end
