//
//  FirestorePluginResultHelper.m
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#import "FirestorePluginResultHelper.h"
#import "FirestorePluginJSONHelper.h"
#include <asl.h>

@implementation FirestorePluginResultHelper;

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
    NSDictionary *querySnapshot = @{};
    NSMutableArray *result = [[NSMutableArray alloc] init];

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Creating query snapshot result");

    if (query.documents != nil) {
        for (FIRDocumentSnapshot *doc in query.documents) {
            NSDictionary *document = [FirestorePluginResultHelper createDocumentSnapshot:doc];
            [result addObject:document];
        }
    }

    querySnapshot = @{ @"docs" : result};
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:querySnapshot];
    [pluginResult setKeepCallbackAsBool:reusable];

    return pluginResult;
}

+ (NSDictionary *)createDocumentSnapshot:(FIRDocumentSnapshot *)doc {

    NSDictionary *documentSnapshot;

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Creating document snapshot result");

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

    asl_log(NULL, NULL, ASL_LEVEL_DEBUG, "Creating document reference result");

    documentReference = @{ @"id" : doc.documentID};

    return documentReference;
}

@end

