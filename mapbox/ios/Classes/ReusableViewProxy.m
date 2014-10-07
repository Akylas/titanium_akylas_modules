#import "ReusableViewProxy.h"
#import "ReusableViewProtocol.h"

@implementation ReusableViewProxy {
	NSDictionary *_bindings;
	NSDictionary *_templateProperties;
    NSMutableDictionary *_initialValues;
	NSMutableDictionary *_currentValues;
	NSMutableSet *_resetKeys;
    BOOL unarchived;
    BOOL enumeratingResetKeys;
}


- (id)initInContext:(id<TiEvaluator>)context
{
    self = [self _initWithPageContext:context];
    if (self) {
        unarchived = NO;
        enumeratingResetKeys = NO;
        _initialValues = [[NSMutableDictionary alloc] initWithCapacity:10];
		_currentValues = [[NSMutableDictionary alloc] initWithCapacity:10];
		_resetKeys = [[NSMutableSet alloc] initWithCapacity:10];
        eventOverrideDelegate = self; // to make sure we also override events
		[context.krollContext invokeBlockOnThread:^{
			[context registerProxy:self];
			//Reusable cell will keep native proxy alive.
			//This proxy will keep its JS object alive.
			[self rememberSelf];
		}];
    }
    return self;
}


-(void)dealloc
{
    RELEASE_TO_NIL(_initialValues)
    RELEASE_TO_NIL(_currentValues)
    RELEASE_TO_NIL(_resetKeys)
    RELEASE_TO_NIL(_bindings)
    RELEASE_TO_NIL(_templateProperties)
	[super dealloc];
}

-(void)deregisterProxy:(id<TiEvaluator>)context
{
    //Aggressive removal of children on deallocation of cell
    [self removeAllChildren:nil];
    [self windowDidClose];
    //Go ahead and unprotect JS object and mark context closed
    //(Since cell no longer exists, the proxy is inaccessible)
    [context.krollContext invokeBlockOnThread:^{
        [self forgetSelf];
        [self contextShutdown:context];
    }];
}


- (void)unarchiveFromTemplate:(id)viewTemplate withEvents:(BOOL)withEvents
{
	[super unarchiveFromTemplate:viewTemplate withEvents:withEvents];
    //lets store the default template props
    _templateProperties = [[NSDictionary dictionaryWithDictionary:[self allProperties]] retain];
	if (withEvents) SetEventOverrideDelegateRecursive(self.children, self);
    unarchived = YES;
    [self.bindings enumerateKeysAndObjectsUsingBlock:^(id binding, id bindObject, BOOL *stop) {
        [[bindObject allProperties] enumerateKeysAndObjectsUsingBlock:^(id key, id prop, BOOL *stop) {
            [_initialValues setValue:prop forKey:[NSString stringWithFormat:@"%@.%@",binding, key]];
        }];
    }];
    [_initialValues addEntriesFromDictionary:[self allProperties]];
    
}

- (NSDictionary *)bindings
{
	if (_bindings == nil &&  unarchived) {
		NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:10];
		[self buildBindingsForViewProxy:self intoDictionary:dict];
		_bindings = [dict copy];
		[dict release];
	}
	return _bindings;
}

-(void)setValue:(id)value forKey:(NSString *)key
{
    if ([self shouldUpdateValue:value forKeyPath:key]) {
        [self recordChangeValue:value forKeyPath:key withBlock:^{
            [super setValue:value forKey:key];
        }];
    }
}

-(void)setValue:(id)value forKeyPath:(NSString *)keyPath
{
    if([keyPath isEqualToString:@"properties"])
    {
        [self setValuesForKeysWithDictionary:value];
    }
    else if ([value isKindOfClass:[NSDictionary class]]) {
        id bindObject = [self.bindings objectForKey:keyPath];
        if (bindObject != nil) {
            NSArray * keySequence = [bindObject keySequence];
            for (NSString * key in keySequence)
            {
                if ([value objectForKey:key]) {
                    id value2 = [value objectForKey:key];
                    NSString *newKeyPath = [NSString stringWithFormat:@"%@.%@", keyPath, key];
                    if ([self shouldUpdateValue:value2 forKeyPath:newKeyPath]) {
                        [self recordChangeValue:value2 forKeyPath:newKeyPath withBlock:^{
                            [bindObject setValue:value2 forKey:key];
                        }];
                    }
                }
            }
            [(NSDictionary *)value enumerateKeysAndObjectsUsingBlock:^(NSString *key, id value2, BOOL *stop) {
                if (![keySequence containsObject:key])
                {
                    NSString *newKeyPath = [NSString stringWithFormat:@"%@.%@", keyPath, key];
                    if ([self shouldUpdateValue:value2 forKeyPath:newKeyPath]) {
                        [self recordChangeValue:value2 forKeyPath:newKeyPath withBlock:^{
                            id obj = [bindObject valueForUndefinedKey:key];
                            if ([obj isKindOfClass:[TiProxy class]] && [value2 isKindOfClass:[NSDictionary class]]) {
                                [obj setValuesForKeysWithDictionary:value2];
                            }
                            else {
                                [bindObject setValue:value2 forKey:key];
                            }
                        }];
                    }
                }
                
            }];
        }
    }
    else [super setValue:value forKeyPath:keyPath];
}


-(id<ReusableViewProtocol>)reusableView
{
    return (id<ReusableViewProtocol>)view;
}

-(void)configurationStart:(BOOL)recursive
{
    [[self reusableView] configurationStart];
    [super configurationStart:recursive];
}

-(void)configurationSet:(BOOL)recursive
{
    [super configurationSet:recursive];
    [[self reusableView] configurationSet];
}

- (void)setDataItem:(NSDictionary *)dataItem
{
    [self configurationStart:YES];
	[_resetKeys addObjectsFromArray:[_currentValues allKeys]];

//    NSMutableDictionary* listProps = [NSMutableDictionary dictionaryWithDictionary:[_listViewProxy propertiesForItems]];
//    if (_templateProperties) {
//        [listProps removeObjectsForKeys:[_templateProperties allKeys]];
//    }
//    if ([dataItem objectForKey:@"properties"])
//    {
//        [listProps removeObjectsForKeys:[[dataItem objectForKey:@"properties"] allKeys]];
//    }
    
    [self.bindings enumerateKeysAndObjectsUsingBlock:^(id key, id bindObject, BOOL *stop) {
        if ([bindObject isKindOfClass:[TiProxy class]]) {
            [bindObject setReproxying:YES];
        }
    }];
    
//    [self setValuesForKeysWithDictionary:listProps];
    
    [dataItem enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        [self setValue:obj forKeyPath:key];
    }];
    
    enumeratingResetKeys = YES;
	[_resetKeys enumerateObjectsUsingBlock:^(NSString *keyPath, BOOL *stop) {
		id value = [_initialValues objectForKey:keyPath];
		[super setValue:(value != [NSNull null] ? value : nil) forKeyPath:keyPath];
		[_currentValues removeObjectForKey:keyPath];
	}];
    [_resetKeys removeAllObjects];
    enumeratingResetKeys = NO;
    
    [self.bindings enumerateKeysAndObjectsUsingBlock:^(id key, id bindObject, BOOL *stop) {
        if ([bindObject isKindOfClass:[TiProxy class]]) {
            [bindObject setReproxying:NO];
        }
    }];
    
    [self configurationSet:YES];
}

- (id)valueForUndefinedKey:(NSString *)key
{
    if ([self.bindings objectForKey:key])
        return [self.bindings objectForKey:key];
    return [super valueForUndefinedKey:key];
}


- (void)recordChangeValue:(id)value forKeyPath:(NSString *)keyPath withBlock:(void(^)(void))block
{
    //	if ([_initialValues objectForKey:keyPath] == nil) {
    //		id initialValue = [self valueForKeyPath:keyPath];
    //		[_initialValues setObject:(initialValue != nil ? initialValue : [NSNull null]) forKey:keyPath];
    //	}
	block();
    if (!unarchived) {
        return;
    }
	if (value != nil) {
		[_currentValues setObject:value forKey:keyPath];
	} else {
		[_currentValues removeObjectForKey:keyPath];
	}
	if (!enumeratingResetKeys) [_resetKeys removeObject:keyPath];
}

- (BOOL)shouldUpdateValue:(id)value forKeyPath:(NSString *)keyPath
{
	id current = [_currentValues objectForKey:keyPath];
	BOOL sameValue = ((current == value) || [current isEqual:value]);
	if (sameValue && !enumeratingResetKeys) {
		[_resetKeys removeObject:keyPath];
	}
	return !sameValue;
}


#pragma mark - TiViewEventOverrideDelegate

- (NSDictionary *)overrideEventObject:(NSDictionary *)eventObject forEvent:(NSString *)eventType fromViewProxy:(TiViewProxy *)viewProxy
{
	NSMutableDictionary *updatedEventObject = [eventObject mutableCopy];
	id propertiesValue = [[self reusableView].dataItem objectForKey:@"properties"];
	NSDictionary *properties = ([propertiesValue isKindOfClass:[NSDictionary class]]) ? propertiesValue : nil;
	id itemId = [properties objectForKey:@"itemId"];
	if (itemId != nil) {
		[updatedEventObject setObject:itemId forKey:@"itemId"];
	}
	id bindId = [viewProxy valueForKey:@"bindId"];
	if (bindId != nil) {
		[updatedEventObject setObject:bindId forKey:@"bindId"];
	}
	return [updatedEventObject autorelease];
}

- (void)viewProxy:(TiProxy *)viewProxy updatedValue:(id)value forType:(NSString *)type;
{
    [self.bindings enumerateKeysAndObjectsUsingBlock:^(id binding, id bindObject, BOOL *stop) {
        if (bindObject == viewProxy) {
            [[[self reusableView].dataItem objectForKey:binding] setValue:value forKey:type];
            [_currentValues setValue:value forKey:[NSString stringWithFormat:@"%@.%@", binding, type]];
            return;
        }
    }];
}


#pragma mark - Static

- (void)buildBindingsForViewProxy:(TiProxy *)viewProxy intoDictionary:(NSMutableDictionary *)dict
{
    if ([viewProxy isKindOfClass:[TiParentingProxy class]]) {
        NSArray* myChildren = [(TiParentingProxy*)viewProxy children];
        [myChildren enumerateObjectsUsingBlock:^(TiProxy *childViewProxy, NSUInteger idx, BOOL *stop) {
            [self buildBindingsForViewProxy:childViewProxy intoDictionary:dict];
        }];
    }
    
    if (![viewProxy isKindOfClass:[ReusableViewProxy class]]) {
        id bindId = [viewProxy valueForKey:@"bindId"];
        if (bindId != nil) {
            [dict setObject:viewProxy forKey:bindId];
        }
    }
}

static void SetEventOverrideDelegateRecursive(NSArray *children, id<TiViewEventOverrideDelegate> eventOverrideDelegate)
{
	[children enumerateObjectsUsingBlock:^(TiProxy *child, NSUInteger idx, BOOL *stop) {
		child.eventOverrideDelegate = eventOverrideDelegate;
        if ([child isKindOfClass:[TiParentingProxy class]]) {
            SetEventOverrideDelegateRecursive(((TiParentingProxy*)child).children, eventOverrideDelegate);
        }
	}];
}

@end
