#import "FirestorePlugin.h"
#import "FirestoreTransaction.h"
#import "FirestorePluginJSONHelper.h"

#import <Cordova/CDVAvailability.h>
#include <asl.h>
#include <pthread.h>

@implementation FirestorePlugin

static int logcount = 0;

- (void)pluginInitialize {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
    
    asl_add_log_file(NULL, STDERR_FILENO);
    
    self.listeners = [NSMutableDictionary new];
    self.transactions = [NSMutableDictionary new];
}

- (void)collectionOnSnapshot:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSArray *queries = [command argumentAtIndex:1 withDefault:@[] andClass:[NSArray class]];
    NSDictionary *options = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];
    NSString *callbackId = [command argumentAtIndex:3 withDefault:@"" andClass:[NSString class]];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Listening to collection");
    
    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];

    BOOL includeMetadataChanges = [self getIncludeMetadataChanges:options];

    FIRQuery *query = [self processQueries:queries ForQuery:collectionReference];

    FIRQuerySnapshotBlock snapshotBlock =^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Collection snapshot listener error %s", [self localError:error]);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createQueryPluginResult:snapshot :YES];

        asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Got collection snapshot data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    id<FIRListenerRegistration> listener;

    listener = [query addSnapshotListenerWithIncludeMetadataChanges:includeMetadataChanges listener:snapshotBlock];

    [self.listeners setObject:listener forKey:callbackId];
}

- (const char * _Nullable)localError:(NSError *)error {
    return [self convertString:[error localizedDescription]];
}
- (const char * _Nullable)convertString:(NSString *)input {
    return [input UTF8String];
}

- (BOOL)getIncludeMetadataChanges:(NSDictionary *)options {

    BOOL queryIncludeMetadataChanges = NO;

    if (options != nil) {

        bool includeDocumentMetadataChanges = [options valueForKey:@"includeDocumentMetadataChanges"];

        if (includeDocumentMetadataChanges) {
            queryIncludeMetadataChanges = YES;
        }

        bool includeQueryMetadataChanges = [options valueForKey:@"includeQueryMetadataChanges"];

        if (includeQueryMetadataChanges) {
            queryIncludeMetadataChanges = YES;
        }
        
        bool includeMetadataChanges = [options valueForKey:@"includeMetadataChanges"];
        
        if (includeMetadataChanges) {
            queryIncludeMetadataChanges = YES;
        }
    }

    return queryIncludeMetadataChanges;
}

- (FIRQuery *)processQueries:(NSArray *)queries ForQuery:(FIRQuery *)query {
    for (NSObject *queryItem in queries) {

        NSString *queryType = [queryItem valueForKey:@"queryType"];
        NSObject *value = [queryItem valueForKey:@"value"];

        asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Query type %s", [self convertString:queryType]);

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
            NSLog(@"Unknown query type %s", [self convertString:queryType]);
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
        NSLog(@"Unknown operator type %s", [self convertString:opStr]);
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

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Order by %s + (%s)", [self convertString:field], [self convertString:direction]);
    
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
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)collectionAdd:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:1 withDefault:@{} andClass:[NSDictionary class]];
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Writing document to collection");
    
    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];
    
    __block FIRDocumentReference *ref = [collectionReference addDocumentWithData:parsedData completion:^(NSError * _Nullable error) {

        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                    @"code" : @(error.code),
                    @"message" : error.description}
            ];

            NSLog(@"Error writing document to collection %s", [self localError:error]);

        } else {
            pluginResult = [FirestorePluginResultHelper createDocumentReferencePluginResult:ref :NO];

            asl_log(NULL,NULL, ASL_LEVEL_DEBUG, "Successfully written document to collection");
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)collectionGet:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSArray *queries = [command argumentAtIndex:1 withDefault:@[] andClass:[NSArray class]];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Getting document from collection");
    
    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];

    FIRQuery *query = [self processQueries:queries ForQuery:collectionReference];
    
    [query getDocumentsWithCompletion:^(FIRQuerySnapshot * snapshot, NSError * error) {
        if (error != nil) {
            NSLog(@"Error getting collection %s", [self localError:error]);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createQueryPluginResult:snapshot :NO];

        asl_log(NULL, NULL,ASL_LEVEL_DEBUG, "Successfully got collection");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)initialise:(CDVInvokedUrlCommand *)command {
    NSDictionary *options = [command argumentAtIndex:0 withDefault:@{} andClass:[NSDictionary class]];

    NSDictionary *config = options[@"config"];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Initialising Firestore...");
    
    if (config != NULL) {
        FIROptions *customOptions = [[FIROptions alloc] initWithGoogleAppID:config[@"googleAppID"] GCMSenderID:config[@"gcmSenderID"]];
        customOptions.bundleID = config[@"bundleID"];
        customOptions.APIKey = config[@"apiKey"];
        customOptions.clientID = config[@"clientID"];
        customOptions.databaseURL = config[@"databaseURL"];
        customOptions.storageBucket = config[@"storageBucket"];
        customOptions.projectID = config[@"projectID"];

        if ([FIRApp appNamed:config[@"apiKey"]] == nil) {
            [FIRApp configureWithName:config[@"apiKey"] options:customOptions];
        }
        FIRApp *customApp = [FIRApp appNamed:config[@"apiKey"]];
        self.firestore = [FIRFirestore firestoreForApp:customApp];
    } else {
        self.firestore = [FIRFirestore firestore];
    }

    FIRFirestoreSettings *settings = self.firestore.settings;

    if (options[@"persist"] != NULL && (int)options[@"persist"] == true) {
        [settings setPersistenceEnabled:true];
        asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Setting Firestore persistance to true");
    }

    NSString *datePrefix = options[@"datePrefix"];

    if (datePrefix != NULL) {
        [FirestorePluginJSONHelper setDatePrefix:datePrefix];
    }

    NSString *fieldValueDelete = options[@"fieldValueDelete"];
    
    if (fieldValueDelete != NULL) {
        [FirestorePluginJSONHelper setFieldValueDelete:fieldValueDelete];
    }
    
    NSString *fieldValueServerTimestamp = options[@"fieldValueServerTimestamp"];
    
    if (fieldValueServerTimestamp != NULL) {
        [FirestorePluginJSONHelper setFieldValueServerTimestamp:fieldValueServerTimestamp];
    }
    
    [self.firestore setSettings:settings];
}

- (void)docSet:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];
    NSDictionary *options = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];

    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];

    BOOL merge = [self getMerge:options];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Setting document");
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:docId];
    
    DocumentSetBlock block = ^(NSError * _Nullable error) {

        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];

            NSLog(@"Error writing document %s", [self localError:error]);

        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

            asl_log(NULL,NULL, ASL_LEVEL_DEBUG, "Successfully written document");
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    [documentReference setData:parsedData merge:merge completion:block];
}

- (BOOL)getMerge:(NSDictionary *)options {
    BOOL merge = NO;

    if (options[@"merge"]) {
        merge = YES;
    }

    return merge;
}

- (void)docUpdate:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];

    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Updating document");

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:docId];

    [documentReference updateData:parsedData completion:^(NSError * _Nullable error) {

        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];

            NSLog(@"Error updating document %s", [self localError:error]);

        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

            asl_log(NULL,NULL,ASL_LEVEL_DEBUG,"Successfully updated document");
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

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Listening to document");
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:doc];

    BOOL includeMetadataChanges = [self getIncludeMetadataChanges:options];

    FIRDocumentSnapshotBlock snapshotBlock =^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Document snapshot listener error %s", [self localError:error]);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :YES];

        asl_log(NULL,NULL,ASL_LEVEL_DEBUG,"Got document snapshot data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    id<FIRListenerRegistration> listener;

    listener = [documentReference addSnapshotListenerWithIncludeMetadataChanges:includeMetadataChanges listener:snapshotBlock];

    [self.listeners setObject:listener forKey:callbackId];

}

- (void)docUnsubscribe:(CDVInvokedUrlCommand *)command {
    NSString *callbackId = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    [self.listeners[callbackId] remove];
    [self.listeners removeObjectForKey:callbackId];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)docGet:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *doc =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Listening to document");

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:doc];

    FIRDocumentSnapshotBlock snapshotBlock =^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Error getting document %s", [self localError:error]);
            return;
        }

        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :YES];

        asl_log(NULL,NULL,ASL_LEVEL_DEBUG,"Successfully got document");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    [documentReference getDocumentWithCompletion:snapshotBlock];
}

- (void)docDelete:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *doc =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Deleting document");

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:collection] documentWithPath:doc];
    
    [documentReference deleteDocumentWithCompletion:^(NSError * _Nullable error) {
        
        CDVPluginResult *pluginResult;
        
        if (error != nil) {
            NSLog(@"Error deleting document %s", [self localError:error]);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        } else {
            asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Successfully deleted document");
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

    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *collectionPath =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];
    NSDictionary *options = [command argumentAtIndex:4 withDefault:@{} andClass:[NSDictionary class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Transaction document set for %s", [self convertString:transactionId]);
    
    FirestoreTransactionQueue *transactionQueue = (FirestoreTransactionQueue *)self.transactions[transactionId];
    FirestoreTransaction *firestoreTransaction = [FirestoreTransaction new];
    
    firestoreTransaction.docId = docId;
    firestoreTransaction.collectionPath = collectionPath;
    firestoreTransaction.data = data;
    firestoreTransaction.options = options;
    firestoreTransaction.transactionOperationType = (FirestoreTransactionOperationType)SET;
    
    @synchronized(self) {
        [transactionQueue.queue addObject:firestoreTransaction];
    }
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)executeTransactionDocSet:(FIRTransaction *)transaction For:(FirestoreTransaction *)firestoreTransaction WithId:(NSString *)transactionId {
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:firestoreTransaction.data];
    
    BOOL merge = [self getMerge:firestoreTransaction.options];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Execute transaction document set");

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:firestoreTransaction.collectionPath] documentWithPath:firestoreTransaction.docId];
    
    [transaction setData:parsedData forDocument:documentReference merge:merge];
}

- (void)transactionDocUpdate:(CDVInvokedUrlCommand *)command {

    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *collectionPath =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:3 withDefault:@{} andClass:[NSDictionary class]];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Transaction document update for %s", [self convertString:transactionId]);
    
    FirestoreTransactionQueue *transactionQueue = (FirestoreTransactionQueue *)self.transactions[transactionId];
    FirestoreTransaction *firestoreTransaction = [FirestoreTransaction new];
    
    firestoreTransaction.docId = docId;
    firestoreTransaction.collectionPath = collectionPath;
    firestoreTransaction.data = data;
    firestoreTransaction.transactionOperationType = (FirestoreTransactionOperationType)UPDATE;
    
    @synchronized(self) {
        [transactionQueue.queue addObject:firestoreTransaction];
    }
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)executeTransactionDocUpdate:(FIRTransaction *)transaction For:(FirestoreTransaction *)firestoreTransaction WithId:(NSString *)transactionId {
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:firestoreTransaction.data];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Execute transaction document update");

    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:firestoreTransaction.collectionPath] documentWithPath:firestoreTransaction.docId];
    
    [transaction updateData:parsedData forDocument:documentReference];
}

- (void)transactionDocDelete:(CDVInvokedUrlCommand *)command {
    
    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *collectionPath =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Transaction document delete for %s", [self convertString:transactionId]);

    FirestoreTransactionQueue *transactionQueue = (FirestoreTransactionQueue *)self.transactions[transactionId];
    FirestoreTransaction *firestoreTransaction = [FirestoreTransaction new];
    
    firestoreTransaction.docId = docId;
    firestoreTransaction.collectionPath = collectionPath;
    firestoreTransaction.transactionOperationType = (FirestoreTransactionOperationType)DELETE;
    
    @synchronized(self) {
        [transactionQueue.queue addObject:firestoreTransaction];
    }
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)executeTransactionDocDelete:(FIRTransaction *)transaction For:(FirestoreTransaction *)firestoreTransaction WithId:(NSString *)transactionId {
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:firestoreTransaction.collectionPath] documentWithPath:firestoreTransaction.docId];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Execute transaction document delete");

    [transaction deleteDocument:documentReference];
}

- (void)transactionDocGet:(CDVInvokedUrlCommand *)command {
    
    [self.commandDelegate runInBackground:^{

        NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
        NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
        NSString *collectionPath =[command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
        
        asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Transaction document get for %s", [self convertString:transactionId]);

        FirestoreTransactionQueue *transactionQueue = (FirestoreTransactionQueue *)self.transactions[transactionId];
        FirestoreTransaction *firestoreTransaction = [FirestoreTransaction new];
        
        firestoreTransaction.docId = docId;
        firestoreTransaction.collectionPath = collectionPath;
        firestoreTransaction.transactionOperationType = (FirestoreTransactionOperationType)GET;
        
        @synchronized(self) {
            [transactionQueue.queue addObject:firestoreTransaction];
            transactionQueue.pluginResult = nil;
        }
        
        time_t started = time(nil);
        BOOL timedOut = NO;
        
        CDVPluginResult *pluginResult;
        
        @synchronized(self) {
            pluginResult = transactionQueue.pluginResult;
        }
        
        while (pluginResult == nil && timedOut == NO) {
            
            timedOut = [self timedOut:started];
            
            @synchronized(self) {
                pluginResult = transactionQueue.pluginResult;
            }
        }
        
        if (timedOut == YES) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:command.callbackId];
        } else {
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (CDVPluginResult *)executeTransactionDocGet:(FIRTransaction *)transaction For:(FirestoreTransaction *)firestoreTransaction WithId:(NSString *)transactionId WithError:(NSError * __autoreleasing *)errorPointer {
    
    FIRDocumentReference *documentReference = [[self.firestore collectionWithPath:firestoreTransaction.collectionPath] documentWithPath:firestoreTransaction.docId];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Execute transaction document get");

    FIRDocumentSnapshot *snapshot = [transaction getDocument:documentReference error:errorPointer];

    CDVPluginResult *pluginResult;

    if (*errorPointer != nil) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    } else {
        pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :NO];
    }

    return pluginResult;
}

- (void)runTransaction:(CDVInvokedUrlCommand *)command {
    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Running transaction");
    
    [self.firestore runTransactionWithBlock:^id _Nullable(FIRTransaction * _Nonnull transaction, NSError *  __autoreleasing * errorPointer) {
        
        asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Applying transaction %s", [self convertString:transactionId]);

        FirestoreTransactionQueue *firestoreTransactionQueue = [FirestoreTransactionQueue new];
        firestoreTransactionQueue.queue = [NSMutableArray new];
        firestoreTransactionQueue.pluginResult = nil;

        @synchronized(self) {
            [self.transactions setObject:firestoreTransactionQueue forKey:transactionId];
        }
        
        NSString *execute = [NSString stringWithFormat:@"Firestore.__executeTransaction('%@');", transactionId];

        [self stringByEvaluatingJavaScriptFromString:execute];
        
        time_t started = time(nil);
        
        BOOL timedOut = NO;
        
        FirestoreTransactionOperationType transactionOperationType = (FirestoreTransactionOperationType)NONE;
        
        while (transactionOperationType != (FirestoreTransactionOperationType)RESOLVE && timedOut == NO)
        {
            timedOut = [self timedOut:started];
            
            int count;
            
            @synchronized(self) {
                count = (int)firestoreTransactionQueue.queue.count;
            }
            
            while (count == 0 && timedOut == NO) {
                timedOut = [self timedOut:started];

                @synchronized(self) {
                    count = (int)firestoreTransactionQueue.queue.count;
                }
            }
            
            FirestoreTransaction *firestoreTransaction;
            
            @synchronized(self) {
                firestoreTransaction = (FirestoreTransaction *)firestoreTransactionQueue.queue[0];
            }
            
            transactionOperationType = firestoreTransaction.transactionOperationType;
            
            CDVPluginResult *pluginResult;
            
            switch (transactionOperationType) {
                case (FirestoreTransactionOperationType)SET:
                    [self executeTransactionDocSet:transaction For:firestoreTransaction WithId:transactionId];
                    break;
                case (FirestoreTransactionOperationType)UPDATE:
                    [self executeTransactionDocUpdate:transaction For:firestoreTransaction WithId:transactionId];
                    break;
                case (FirestoreTransactionOperationType)DELETE:
                    [self executeTransactionDocDelete:transaction For:firestoreTransaction WithId:transactionId];
                    break;
                case (FirestoreTransactionOperationType)GET:
                    pluginResult = [self executeTransactionDocGet:transaction For:firestoreTransaction WithId:transactionId WithError:errorPointer];
                    @synchronized(self) {
                        firestoreTransactionQueue.pluginResult = pluginResult;
                    }
                    break;
                case (FirestoreTransactionOperationType)NONE:
                case (FirestoreTransactionOperationType)RESOLVE:
                    break;
            }
            
            @synchronized(self) {
                [firestoreTransactionQueue.queue removeObjectAtIndex:0];
            }
        }
        
        [self.transactions removeObjectForKey:transactionId];
        
        return firestoreTransactionQueue.result;
        
    } completion:^(id  _Nullable result, NSError * _Nullable error) {
                
        CDVPluginResult *pluginResult;

        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];
            NSLog(@"Transaction failure %s", [self localError:error]);
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:result];
            asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Transaction success");
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)transactionResolve:(CDVInvokedUrlCommand *)command {
    NSString *transactionId =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *result =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Transaction resolve for %s", [self convertString:transactionId]);

    FirestoreTransactionQueue *transactionQueue = (FirestoreTransactionQueue *)self.transactions[transactionId];
    FirestoreTransaction *firestoreTransaction = [FirestoreTransaction new];
    
    firestoreTransaction.transactionOperationType = (FirestoreTransactionOperationType)RESOLVE;
    
    @synchronized(self) {
        [transactionQueue.queue addObject:firestoreTransaction];
        transactionQueue.result = result;
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
                NSLog(@"evaluateJavaScript error : %s", [self localError:error]);
            }
        }];
    });

    
    return resultString;
}

- (void)docOfSubCollectionSet:(CDVInvokedUrlCommand *)command {
    NSString *collection = [command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId = [command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *subCollection = [command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSString *docOfSubCollectionId = [command argumentAtIndex:3 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:4 withDefault:@{} andClass:[NSDictionary class]];
    NSDictionary *options = [command argumentAtIndex:5 withDefault:@{} andClass:[NSDictionary class]];
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];
    
    FIRSetOptions *setOptions = [self getSetOptions:options];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Setting document of sub collection");
    
    FIRDocumentReference *documentOfSubCollectionReference = [[[[self.firestore collectionWithPath:collection] documentWithPath:docId] collectionWithPath:subCollection] documentWithPath:docOfSubCollectionId];
    
    DocumentSetBlock block = ^(NSError * _Nullable error) {
        
        CDVPluginResult *pluginResult;
        
        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];
            
            NSLog(@"Error writing document of sub collection %s", [self localError:error]);
            
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            
            asl_log(NULL,NULL, ASL_LEVEL_DEBUG, "Successfully written document of sub collection");
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };
    
    if (setOptions == nil) {
        [documentOfSubCollectionReference setData:parsedData completion:block];
    } else {
        [documentOfSubCollectionReference setData:parsedData options:setOptions completion:block];
    }
}

- (void)docOfSubCollectionGet:(CDVInvokedUrlCommand *)command {
    NSString *collection = [command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId = [command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *subCollection = [command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSString *docOfSubCollectionId = [command argumentAtIndex:3 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Listening to document of sub collection");
    
    FIRDocumentReference *documentOfSubCollectionReference = [[[[self.firestore collectionWithPath:collection] documentWithPath:docId] collectionWithPath:subCollection] documentWithPath:docOfSubCollectionId];
    
    FIRDocumentSnapshotBlock snapshotBlock =^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Error getting document of sub collection %s", [self localError:error]);
            return;
        }
        
        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createDocumentPluginResult:snapshot :YES];
        
        asl_log(NULL,NULL,ASL_LEVEL_DEBUG,"Successfully got document of sub collection");
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };
    
    [documentOfSubCollectionReference getDocumentWithCompletion:snapshotBlock];
}

- (void)docOfSubCollectionUpdate:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId =[command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *subCollection = [command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSString *docOfSubCollectionId = [command argumentAtIndex:3 withDefault:@"/" andClass:[NSString class]];
    NSDictionary *data = [command argumentAtIndex:4 withDefault:@{} andClass:[NSDictionary class]];
    
    NSDictionary *parsedData = [FirestorePluginJSONHelper fromJSON:data];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Updating document of sub collection");
    
    FIRDocumentReference *documentOfSubCollectionReference = [[[[self.firestore collectionWithPath:collection] documentWithPath:docId] collectionWithPath:subCollection] documentWithPath:docOfSubCollectionId];
    
    [documentOfSubCollectionReference updateData:parsedData completion:^(NSError * _Nullable error) {
        
        CDVPluginResult *pluginResult;
        
        if (error != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                                                                                                          @"code" : @(error.code),
                                                                                                          @"message" : error.description}
                            ];
            
            NSLog(@"Error updating document of sub collection %s", [self localError:error]);
            
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            
            asl_log(NULL,NULL,ASL_LEVEL_DEBUG,"Successfully updated document of sub collection");
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)docOfSubCollectionDelete:(CDVInvokedUrlCommand *)command {
    NSString *collection = [command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId = [command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *subCollection = [command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    NSString *docOfSubCollectionId = [command argumentAtIndex:3 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Deleting document of sub collection");
    
    FIRDocumentReference *documentOfSubCollectionReference = [[[[self.firestore collectionWithPath:collection] documentWithPath:docId] collectionWithPath:subCollection] documentWithPath:docOfSubCollectionId];
    
    [documentOfSubCollectionReference deleteDocumentWithCompletion:^(NSError * _Nullable error) {
        
        CDVPluginResult *pluginResult;
        
        if (error != nil) {
            NSLog(@"Error deleting document of sub collection %s", [self localError:error]);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        } else {
            asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Successfully deleted document of sub collection");
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)subCollectionGet:(CDVInvokedUrlCommand *)command {
    NSString *collection = [command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSString *docId = [command argumentAtIndex:1 withDefault:@"/" andClass:[NSString class]];
    NSString *subCollection = [command argumentAtIndex:2 withDefault:@"/" andClass:[NSString class]];
    
    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Getting document from sub collection");
    
    FIRCollectionReference *collectionReference = [[[self.firestore collectionWithPath:collection] documentWithPath:docId] collectionWithPath:subCollection];
    
    [collectionReference getDocumentsWithCompletion:^(FIRQuerySnapshot * snapshot, NSError * error) {
        if (error != nil) {
            NSLog(@"Error getting sub collection %s", [self localError:error]);
            return;
        }
        
        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createQueryPluginResult:snapshot :NO];
        
        asl_log(NULL, NULL,ASL_LEVEL_DEBUG, "Successfully got sub collection");
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

@end

