#import "FirestorePlugin.h"
#import "FirestoreTransaction.h"
#import "FirestorePluginJSONHelper.h"

#import <Cordova/CDVAvailability.h>

@implementation FirestorePlugin

- (void)pluginInitialize {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
    
    self.listeners = [NSMutableDictionary new];
    self.firestoreTransaction = [FirestoreTransaction new];
}

- (void)collectionOnSnapshot:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSArray *queries = [command argumentAtIndex:1 withDefault:@[] andClass:[NSArray class]];
    NSDictionary *options = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];
    NSString *callbackId = [command argumentAtIndex:3 withDefault:@"" andClass:[NSString class]];

    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];

    FIRQueryListenOptions *queryListenOptions = [self getQueryListenOptions:options];

    FIRQuery *query = [self processQueries:queries ForQuery:collectionReference];

    FIRQuerySnapshotBlock snapshotBlock =^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Collection snapshot listener error %@", error);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createQueryPluginResult:snapshot :YES];

        NSLog(@"Got collection snapshot data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    id<FIRListenerRegistration> listener;

    if (queryListenOptions == nil) {
        listener = [query addSnapshotListener:snapshotBlock];
    } else {
        listener = [query addSnapshotListenerWithOptions:queryListenOptions listener:snapshotBlock];
    }

    [self.listeners setObject:listener forKey:callbackId];
}

- (FIRQueryListenOptions *)getQueryListenOptions:(NSDictionary *)options {

    FIRQueryListenOptions *queryListenOptions = nil;

    if (options != nil) {
        queryListenOptions = [FIRQueryListenOptions alloc];

        bool includeDocumentMetadataChanges = [options valueForKey:@"includeDocumentMetadataChanges"];

        if (includeDocumentMetadataChanges) {
            [queryListenOptions includeQueryMetadataChanges:true];
        }

        bool includeQueryMetadataChanges = [options valueForKey:@"includeQueryMetadataChanges"];

        if (includeQueryMetadataChanges) {
            [queryListenOptions includeQueryMetadataChanges:true];
        }
    }

    return queryListenOptions;
}

- (FIRQuery *)processQueries:(NSArray *)queries ForQuery:(FIRQuery *)query {
    for (NSObject *queryItem in queries) {
        NSLog(@"Query type %@", query);

        NSString *queryType = [queryItem valueForKey:@"queryType"];
        NSObject *value = [queryItem valueForKey:@"value"];

        if ([queryType isEqualToString:@"limit"]) {
            query = [self processQueryLimit:query ForValue:value];
        } else if ([queryType isEqualToString:@"where"]) {
            query = [self processQueryWhere:query ForValue:value];
        } else if ([queryType isEqualToString:@"orderBy"]) {
            query = [self processQueryOrderBy:query ForValue:value];
        } else if ([queryType isEqualToString:@"startAfter"]) {
            query = [self processQueryStartAfter:query ForValue:value];
        } else if ([queryType isEqualToString:@"startAt"]) {
            query = [self processQueryStartAt:query ForValue:value];
        } else if ([queryType isEqualToString:@"endAt"]) {
            query = [self processQueryEndAt:query ForValue:value];
        } else if ([queryType isEqualToString:@"endBefore"]) {
            query = [self processQueryEndBefore:query ForValue:value];
        } else {
            NSLog(@"Unknown query type %@", queryType);
        }
    }

    return query;
}

- (FIRQuery *)processQueryLimit:(FIRQuery *)query ForValue:(NSObject *)value {
    NSNumber *integer = (NSNumber *)value;
    return [query queryLimitedTo:[integer integerValue]];
}

- (FIRQuery *)processQueryWhere:(FIRQuery *)query ForValue:(NSObject *)whereObject {

    NSString *fieldPath = [whereObject valueForKey:@"fieldPath"];
    NSString *opStr = [whereObject valueForKey:@"opStr"];
    NSObject *value = [self parseWhereValue:[whereObject valueForKey:@"value"]];

    if ([opStr isEqualToString:@"=="]) {
        return [query queryWhereField:fieldPath isEqualTo:value];
    } else if ([opStr isEqualToString:@">"]) {
        return [query queryWhereField:fieldPath isGreaterThan:value];
    } else if ([opStr isEqualToString:@">="]) {
        return [query queryWhereField:fieldPath isGreaterThanOrEqualTo:value];
    } else if ([opStr isEqualToString:@"<"]) {
        return [query queryWhereField:fieldPath isLessThan:value];
    } else if ([opStr isEqualToString:@"<="]) {
        return [query queryWhereField:fieldPath isLessThanOrEqualTo:value];
    } else {
        NSLog(@"Unknown operation %@", opStr);
    }

    return query;
}

- (FIRQuery *)processQueryOrderBy:(FIRQuery *)query ForValue:(NSObject *)orderByObject {

    NSString *direction = [orderByObject valueForKey:@"direction"];
    NSString *field = [orderByObject valueForKey:@"field"];

    BOOL directionBool = false;

    if ([direction isEqualToString:@"desc"]) {
      directionBool = true;
    }

    return [query queryOrderedByField:field descending:directionBool];
}

- (NSObject *)parseWhereValue:(NSObject *)value {
    return [FirestorePluginJSONHelper parseSpecial:value];
}

- (FIRQuery *)processQueryStartAfter:(FIRQuery *)query ForValue:(NSObject *)value {
    NSMutableArray *array = [[NSMutableArray alloc]init];
    [array addObject:[FirestorePluginJSONHelper parseSpecial:value]];
    return [query queryStartingAfterValues:array];
}

- (FIRQuery *)processQueryStartAt:(FIRQuery *)query ForValue:(NSObject *)value {
    NSMutableArray *array = [[NSMutableArray alloc]init];
    [array addObject:[FirestorePluginJSONHelper parseSpecial:value]];
    return [query queryStartingAtValues:array];
}

- (FIRQuery *)processQueryEndAt:(FIRQuery *)query ForValue:(NSObject *)value {
    NSMutableArray *array = [[NSMutableArray alloc]init];
    [array addObject:[FirestorePluginJSONHelper parseSpecial:value]];
    return [query queryEndingAtValues:array];
}

- (FIRQuery *)processQueryEndBefore:(FIRQuery *)query ForValue:(NSObject *)value {
    NSMutableArray *array = [[NSMutableArray alloc]init];
    [array addObject:[FirestorePluginJSONHelper parseSpecial:value]];
    return [query queryEndingBeforeValues:array];
}

- (void)collectionUnsubscribe:(CDVInvokedUrlCommand *)command {
    NSString *callbackId = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    [self.listeners[callbackId] remove];
    [self.listeners removeObjectForKey:callbackId];
}

- (void)collectionAdd:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:1 withDefault:@{} andClass:[NSDictionary class]];
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];

    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];

    __block FIRDocumentReference *ref = [collectionReference addDocumentWithData:parsedData completion:^(NSError * _Nullable error) {

        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                    @"code" : @(error.code),
                    @"message" : error.description}
            ];

            NSLog(@"Error adding collection data");

        } else {
            pluginResult = [FirestorePluginResultHelper createDocumentReferencePluginResult:ref :NO];

            NSLog(@"Added collection data");
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)collectionGet:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSArray *queries = [command argumentAtIndex:1 withDefault:@[] andClass:[NSArray class]];

    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];

    FIRQuery *query = [self processQueries:queries ForQuery:collectionReference];

    [query getDocumentsWithCompletion:^(FIRQuerySnapshot * snapshot, NSError * error) {
        if (error != nil) {
            NSLog(@"Collection get listener error %@", error);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createQueryPluginResult:snapshot :NO];

        NSLog(@"Got collection data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)initialise:(CDVInvokedUrlCommand *)command {
    NSDictionary *options = [command argumentAtIndex:0 withDefault:@{} andClass:[NSDictionary class]];
    self.firestore = [FIRFirestore firestore];

   // Logging *logging = [Logging];

 //   [Logging enableLogging:true];

    FIRFirestoreSettings *settings = self.firestore.settings;

    if (options[@"persist"] != NULL && (int)options[@"persist"] == true) {
        [settings setPersistenceEnabled:true];
    }

    NSString *datePrefix = options[@"datePrefix"];

    if (datePrefix != NULL) {
        [FirestorePluginJSONHelper setDatePrefix:datePrefix];
    }

    [self.firestore setSettings:settings];
}

- (void)docSet:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];
    NSDictionary *options = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];

    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];

    FIRSetOptions *setOptions = [self getSetOptions:options];

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:docId];

    DocumentSetBlock block = ^(NSError * _Nullable error) {

        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];

            NSLog(@"Error setting document data");

        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

            NSLog(@"Set document data");
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    if (setOptions == nil) {
        [documentReference setData:parsedData completion:block];
    } else {
        [documentReference setData:parsedData options:setOptions completion:block];
    }
}

- (FIRSetOptions *)getSetOptions:(NSDictionary *)options {
    FIRSetOptions *setOptions = nil;

    if (options[@"merge"]) {
        setOptions = [FIRSetOptions merge];
    }

    return setOptions;
}

- (void)documentSetCompletion:(NSError * _Nullable)error Command:(CDVInvokedUrlCommand *)command  {

    CDVPluginResult *pluginResult;

    if (error != nil) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"code" : @(error.code),
            @"message" : error.description}
        ];

        NSLog(@"Error setting document data");

    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

        NSLog(@"Set document data");
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)docUpdate:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];

    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:docId];

    [documentReference updateData:parsedData completion:^(NSError * _Nullable error) {

        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];

            NSLog(@"Error setting document data");

        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

            NSLog(@"Set document data");
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)docOnSnapshot:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *doc =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *callbackId = [command argumentAtIndex:2 withDefault:@"" andClass:[NSString class]];

    NSDictionary *options = nil;

    if (command.arguments.count > 3) {
        options = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];
    }

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:doc];

    FIRDocumentListenOptions *documentListenOptions = [self getDocumentListenOptions:options];

    FIRDocumentSnapshotBlock snapshotBlock =^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Document snapshot listener error %@", error);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :YES];

        NSLog(@"Got document snapshot data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    id<FIRListenerRegistration> listener;

    if (documentListenOptions == nil) {
        listener = [documentReference addSnapshotListener:snapshotBlock];
    } else {
        listener = [documentReference addSnapshotListenerWithOptions:documentListenOptions listener:snapshotBlock];
    }

    [self.listeners setObject:listener forKey:callbackId];

}

- (FIRDocumentListenOptions *)getDocumentListenOptions:(NSDictionary *)options {

    FIRDocumentListenOptions *documentListenOptions = nil;

    if (options != nil) {
        documentListenOptions = [FIRDocumentListenOptions alloc];

        bool includeMetadataChanges = [options valueForKey:@"includeMetadataChanges"];

        if (includeMetadataChanges) {
            [documentListenOptions includeMetadataChanges:true];
        }
    }

    return documentListenOptions;
}

- (void)docUnsubscribe:(CDVInvokedUrlCommand *)command {
    NSString *callbackId = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    [self.listeners[callbackId] remove];
    [self.listeners removeObjectForKey:callbackId];
}

- (void)docGet:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *doc =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:doc];

    FIRDocumentSnapshotBlock snapshotBlock =^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Document snapshot listener error %@", error);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :YES];

        NSLog(@"Got document snapshot data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    [documentReference getDocumentWithCompletion:snapshotBlock];
}

- (void)docDelete:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *doc =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:doc];
    
    [documentReference deleteDocumentWithCompletion:^(NSError * _Nullable error) {
        
        CDVPluginResult *pluginResult;
        
        if (error != nil) {
            NSLog(@"Document delete error %@", error);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (BOOL)timedOut:(time_t)started {
    if (time(nil) - started > 30) {
        return YES;
    }
    
    return NO;
}

- (void)transactionDocSet:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{

    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *collection =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];
    NSDictionary *options = [command argumentAtIndex:4 withDefault:@{} andClass:[NSDictionary class]];
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.transactionId = transactionId;
        self.firestoreTransaction.docId = docId;
        self.firestoreTransaction.collection = collection;
        self.firestoreTransaction.data = data;
        self.firestoreTransaction.options = options;
        self.firestoreTransaction.transactionType = (FirestoreTransactionType)SET;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)PROCESSING;
    }
    
    time_t started = time(nil);
    
    while (self.firestoreTransaction.transactionStatus != (FirestoreTransactionStatus)COMPLETE) {
        if ([self timedOut:started]) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:command.callbackId];
            return;
        }
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
    }
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)READY;
    }
    
    [self.commandDelegate sendPluginResult:self.firestoreTransaction.pluginResult callbackId:command.callbackId];
    }];
}


- (void)executeTransactionDocSet:(FIRTransaction *)transaction {
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:self.firestoreTransaction.data];
    
    FIRSetOptions *setOptions = [self getSetOptions:self.firestoreTransaction.options];
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:self.firestoreTransaction.collection] documentWithPath:self.firestoreTransaction.docId];
    
    if (setOptions == nil) {
        [transaction setData:parsedData forDocument:documentReference];
    } else {
        [transaction setData:parsedData forDocument:documentReference options:setOptions];
    }
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.pluginResult = pluginResult;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)COMPLETE;
    }
}

- (void)transactionDocUpdate:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{

    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *collection =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.transactionId = transactionId;
        self.firestoreTransaction.docId = docId;
        self.firestoreTransaction.collection = collection;
        self.firestoreTransaction.data = data;
        self.firestoreTransaction.options = nil;
        self.firestoreTransaction.transactionType = (FirestoreTransactionType)UPDATE;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)PROCESSING;
    }
    
    time_t started = time(nil);
    
    while (self.firestoreTransaction.transactionStatus != (FirestoreTransactionStatus)COMPLETE) {
        if ([self timedOut:started]) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:command.callbackId];
            return;
        }
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
    }
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)READY;
    }
    
    [self.commandDelegate sendPluginResult:self.firestoreTransaction.pluginResult callbackId:command.callbackId];
    }];
}

- (void)executeTransactionDocUpdate:(FIRTransaction *)transaction {
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:self.firestoreTransaction.data];
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:self.firestoreTransaction.collection] documentWithPath:self.firestoreTransaction.docId];
    
    [transaction updateData:parsedData forDocument:documentReference];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.pluginResult = pluginResult;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)COMPLETE;
    }
}

- (void)transactionDocDelete:(CDVInvokedUrlCommand *)command {
    
    [self.commandDelegate runInBackground:^{

    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *collection =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.transactionId = transactionId;
        self.firestoreTransaction.docId = docId;
        self.firestoreTransaction.collection = collection;
        self.firestoreTransaction.data = nil;
        self.firestoreTransaction.options = nil;
        self.firestoreTransaction.transactionType = (FirestoreTransactionType)DELETE;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)PROCESSING;
    }
    
    time_t started = time(nil);
    
    while (self.firestoreTransaction.transactionStatus != (FirestoreTransactionStatus)COMPLETE) {
        if ([self timedOut:started]) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:command.callbackId];
            return;
        }
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
    }
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)READY;
    }
    
    [self.commandDelegate sendPluginResult:self.firestoreTransaction.pluginResult callbackId:command.callbackId];
    }];
}

- (void)executeTransactionDocDelete:(FIRTransaction *)transaction {
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:self.firestoreTransaction.collection] documentWithPath:self.firestoreTransaction.docId];
    
    [transaction deleteDocument:documentReference];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.pluginResult = pluginResult;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)COMPLETE;
    }
}

- (void)transactionDocGet:(CDVInvokedUrlCommand *)command {
    
    [self.commandDelegate runInBackground:^{
        NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
        NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
        NSString *collection =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
        
        @synchronized (self.firestoreTransaction) {
            self.firestoreTransaction.transactionId = transactionId;
            self.firestoreTransaction.docId = docId;
            self.firestoreTransaction.collection = collection;
            self.firestoreTransaction.data = nil;
            self.firestoreTransaction.options = nil;
            self.firestoreTransaction.transactionType = (FirestoreTransactionType)GET;
            self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)PROCESSING;
        }
        
        time_t started = time(nil);
        
        FirestoreTransactionStatus status = (FirestoreTransactionStatus)PROCESSING;
        
        while (status != (FirestoreTransactionStatus)COMPLETE) {
            
            @synchronized(self.firestoreTransaction) {
                status = self.firestoreTransaction.transactionStatus;
            }
            
            if ([self timedOut:started]) {
                [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:command.callbackId];
                return;
            }
            [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
        }
        
        @synchronized (self.firestoreTransaction) {
            self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)READY;
        }
        
        [self.commandDelegate sendPluginResult:self.firestoreTransaction.pluginResult callbackId:command.callbackId];
    }];
}

- (void)executeTransactionDocGet:(FIRTransaction *)transaction WithError:(NSError * __autoreleasing *)errorPointer {

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:self.firestoreTransaction.collection] documentWithPath:self.firestoreTransaction.docId];
    
    FIRDocumentSnapshot *snapshot = [transaction getDocument:documentReference error:errorPointer];
    
    CDVPluginResult *pluginResult;
    
    if (*errorPointer != nil) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    } else {
        pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :NO];
    }
    
    @synchronized (self.firestoreTransaction) {
        self.firestoreTransaction.pluginResult = pluginResult;
        self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)COMPLETE;
    }
}

- (void)runTransaction:(CDVInvokedUrlCommand *)command {
    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    
    self.firestoreTransaction.transactionResolved = [NSNumber numberWithBool:NO];

    [self.firestore runTransactionWithBlock:^id _Nullable(FIRTransaction * _Nonnull transaction, NSError *  __autoreleasing * errorPointer) {
        
        NSString *execute = [NSString stringWithFormat:@"Firestore.__executeTransaction('%@');", transactionId];

        [self stringByEvaluatingJavaScriptFromString:execute];
        
        @synchronized (self.firestoreTransaction) {
            self.firestoreTransaction.transactionStatus = (FirestoreTransactionStatus)READY;
            self.firestoreTransaction.transactionType = (FirestoreTransactionType)UNDEFINED;
            self.firestoreTransaction.transactionResolved = NO;
        }
        
        time_t started = time(nil);
        BOOL resolved = NO;
            
        while (resolved == NO && [self timedOut:started] == NO)
        {
            @synchronized(self.firestoreTransaction) {
                resolved = self.firestoreTransaction.transactionResolved;
                
                if (self.firestoreTransaction.transactionType == (FirestoreTransactionType)SET) {
                    [self executeTransactionDocSet:transaction];
                } else if (self.firestoreTransaction.transactionType == (FirestoreTransactionType)UPDATE) {
                    [self executeTransactionDocUpdate:transaction];
                } else if (self.firestoreTransaction.transactionType == (FirestoreTransactionType)DELETE) {
                    [self executeTransactionDocDelete:transaction];
                } else if (self.firestoreTransaction.transactionType == (FirestoreTransactionType)GET) {
                    [self executeTransactionDocGet:transaction WithError:errorPointer];
                }
                
                self.firestoreTransaction.transactionType = (FirestoreTransactionType)UNDEFINED;
            }
                
            [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
        }
        
        return self.firestoreTransaction.result;
        
    } completion:^(id  _Nullable result, NSError * _Nullable error) {
                
        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:result];
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)transactionResolve:(CDVInvokedUrlCommand *)command {
    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *result =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];

    @synchronized(self.firestoreTransaction) {
        self.firestoreTransaction.transactionResolved = [NSNumber numberWithBool:YES];
        self.firestoreTransaction.result = result;
    }
}

- (NSString *)stringByEvaluatingJavaScriptFromString:(NSString *)script {
    __block NSString *resultString = nil;

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.webViewEngine evaluateJavaScript:script completionHandler:^(id result, NSError *error) {
            if (error == nil) {
                if (result != nil) {
                    resultString = [NSString stringWithFormat:@"%@", result];
                }
            } else {
                NSLog(@"evaluateJavaScript error : %@", error.localizedDescription);
            }
        }];
    });

    
    return resultString;
}

@end

