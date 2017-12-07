//
//  FirestorePluginResultHelper.m
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#import "FirestorePluginResultHelper.h"
#import "FirestorePluginJSONHelper.h"

@implementation FirestorePluginResultHelper;

+ (CDVPluginResult *)createDocumentPluginResult:(FIRDocumentSnapshot *)doc :(BOOL )reusable {
    NSDictionary *result = [FirestorePluginResultHelper createDocumentSnapshot:doc];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [pluginResult setKeepCallbackAsBool:reusable];
    
    return pluginResult;
}

+ (CDVPluginResult *)createQueryPluginResult:(FIRQuerySnapshot *)query :(BOOL )reusable {
    NSDictionary *querySnapshot = @{};
    NSMutableArray *result = [[NSMutableArray alloc] init];
    
    NSLog(@"Creating query snapshot result");
    
    if (query.documents != nil) {
    for (FIRDocumentSnapshot *doc in query.documents) {
        NSDictionary *document = [FirestorePluginResultHelper createDocumentSnapshot:doc];
        [result addObject:document];
    }
    }
    
    querySnapshot = @{ @"docs" : result};
    
    NSLog(@"%@", querySnapshot);
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:querySnapshot];
    [pluginResult setKeepCallbackAsBool:reusable];
    
    return pluginResult;
}

+ (NSDictionary *)createDocumentSnapshot:(FIRDocumentSnapshot *)doc {
    
    NSString *exists = @"N";
    NSDictionary *data = nil;
    
    NSLog(@"Creating document snapshot result");
    
    if (doc.exists) {
        exists = @"Y";
        data = [FirestorePluginJSONHelper toJSON:doc.data];
    }
    
    NSDictionary *documentSnapshot = @{ @"id" : doc.documentID,
                                        @"exists" : exists,
                                        @"ref" : doc.reference.documentID,
                                        @"_data" : data
                                        };
    
    return documentSnapshot;
}

@end
