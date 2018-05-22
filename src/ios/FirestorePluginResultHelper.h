//
//  FirestorePluginResultHelper.h
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#ifndef FirestorePluginResultHelper_h
#define FirestorePluginResultHelper_h

#import <Cordova/CDVPlugin.h>

@import Firebase/Firebase;
@import FirebaseFirestore/FirebaseFirestore;

@interface FirestorePluginResultHelper : NSObject

+ (CDVPluginResult *)createDocumentPluginResult:(FIRDocumentSnapshot *)doc :(BOOL )reusable;
+ (CDVPluginResult *)createQueryPluginResult:(FIRQuerySnapshot *)doc :(BOOL )reusable;
+ (CDVPluginResult *)createDocumentReferencePluginResult:(FIRDocumentReference *)doc :(BOOL )reusable;

+ (NSDictionary *)createDocumentSnapshot:(FIRDocumentSnapshot *)doc;
+ (NSDictionary *)createDocumentReference:(FIRDocumentReference *)doc;

@end

#endif /* FirestorePluginResultHelper_h */
