//
//  FirestorePluginResultHelper.m
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#import "FirestorePluginResultHelper.h"
#import "FirestorePluginJSONHelper.h"
#import <os/log.h>

@implementation FirestorePluginResultHelper;

NSDictionary *mappedErrors;

+(void)load {
     mappedErrors = @{  @(FIRFirestoreErrorCodeAborted) : @"aborted",\
                        @(FIRFirestoreErrorCodeAlreadyExists): @"already-exists",\
                        @(FIRFirestoreErrorCodeCancelled):@"cancelled",\
                        @(FIRFirestoreErrorCodeDataLoss): @"data-loss",\
                        @(FIRFirestoreErrorCodeDeadlineExceeded): @"deadline-exceeded",\
                        @(FIRFirestoreErrorCodeFailedPrecondition):@"failed-precondition",\
                        @(FIRFirestoreErrorCodeInternal):@"internal", \
                        @(FIRFirestoreErrorCodeInvalidArgument):@"invalid-argument",\
                        @(FIRFirestoreErrorCodeNotFound):@"not-found",\
                        @(FIRFirestoreErrorCodeOK):@"ok",\
                        @(FIRFirestoreErrorCodeOutOfRange):@"out-of-range",\
                        @(FIRFirestoreErrorCodePermissionDenied):@"permission-denied",\
                        @(FIRFirestoreErrorCodeResourceExhausted):@"resource-exhausted",\
                        @(FIRFirestoreErrorCodeUnauthenticated):@"unauthenticated",\
                        @(FIRFirestoreErrorCodeUnavailable):@"unavailable",\
                        @(FIRFirestoreErrorCodeUnimplemented):@"unimplemented",\
                        @(FIRFirestoreErrorCodeUnknown):@"unknown"};
}
+ (CDVPluginResult *)createPluginErrorResult:(NSError *)error :(BOOL )reusable {
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[self createError:error.code :error.localizedDescription]];
    [pluginResult setKeepCallbackAsBool:reusable];

    return pluginResult;
}

+ (CDVPluginResult *)createDocumentPluginResult:(FIRDocumentSnapshot *)doc :(BOOL )reusable {
    NSDictionary *result = [FirestorePluginResultHelper createDocumentSnapshot:doc];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [pluginResult setKeepCallbackAsBool:reusable];

    return pluginResult;
}

+ (CDVPluginResult *)createDocumentReferencePluginResult:(FIRDocumentReference *)doc :(BOOL )reusable {
    NSDictionary *result = [FirestorePluginResultHelper createDocumentReference:doc];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [pluginResult setKeepCallbackAsBool:reusable];

    return pluginResult;
}

+ (CDVPluginResult *)createQueryPluginResult:(FIRQuerySnapshot *)query :(BOOL )reusable {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    NSMutableArray *changes = [[NSMutableArray alloc] init];
  
    os_log_debug(OS_LOG_DEFAULT, "Creating query snapshot result");

    if (query.documents != nil) {
        for (FIRQueryDocumentSnapshot *doc in query.documents) {
            NSDictionary *document = [FirestorePluginResultHelper createDocumentSnapshot:doc];
            [result addObject:document];
        }
    }

    if (query.documentChanges != nil) {
        for (FIRDocumentChange *diff in query.documentChanges) {
            NSDictionary *change = @{
                @"type": [self mapDocumentChangeType:diff.type],
                @"doc": [FirestorePluginResultHelper createDocumentSnapshot:diff.document]
            };
            [changes addObject:change];
        }
    }

    NSDictionary *querySnapshot = @{
        @"docs" : result,
        @"docChanges" : changes
    };

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:querySnapshot];
    [pluginResult setKeepCallbackAsBool:reusable];

    return pluginResult;
}

+ (NSDictionary *)createError:(NSInteger )code :(NSString *)message {

    NSDictionary *error;
    
    os_log_debug(OS_LOG_DEFAULT, "Creating error result");

    error = @{ @"code" : [mappedErrors objectForKey:@(code)],
               @"message" : message
            };

    return error;
}

+ (NSDictionary *)createDocumentSnapshot:(FIRDocumentSnapshot *)doc {

    NSDictionary *documentSnapshot;

    os_log_debug(OS_LOG_DEFAULT, "Creating document snapshot result");

    if (doc.exists) {
        documentSnapshot = @{ @"id" : doc.documentID,
                                            @"exists" : @YES,
                                            @"ref" : doc.reference.documentID,
                                            @"_data" : [FirestorePluginJSONHelper toJSON:doc.data]
                                            };
    } else {
        documentSnapshot = @{ @"id" : doc.documentID,
                                            @"exists" : @NO,
                                            @"ref" : doc.reference.documentID
                                            };
    }

    return documentSnapshot;
}

+ (NSDictionary *)createDocumentReference:(FIRDocumentReference *)doc {

    NSDictionary *documentReference;

    os_log_debug(OS_LOG_DEFAULT, "Creating document reference result");

    documentReference = @{ @"id" : doc.documentID};

    return documentReference;
}

@end

