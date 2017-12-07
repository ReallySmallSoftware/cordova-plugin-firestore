#import <Cordova/CDVPlugin.h>
@import Firebase;
@import FirebaseFirestore;

#import "FirestorePluginResultHelper.h"

@interface FirestorePlugin : CDVPlugin

- (void)collectionOnSnapshot:(CDVInvokedUrlCommand *)command;
- (void)collectionUnsubscribe:(CDVInvokedUrlCommand *)command;
- (void)collectionAdd:(CDVInvokedUrlCommand *)command;
- (void)collectionGet:(CDVInvokedUrlCommand *)command;
- (void)initialise:(CDVInvokedUrlCommand *)command;
- (void)docSet:(CDVInvokedUrlCommand *)command;
- (void)docUpdate:(CDVInvokedUrlCommand *)command;
- (void)docOnSnapshot:(CDVInvokedUrlCommand *)command;
- (void)docUnsubscribe:(CDVInvokedUrlCommand *)command;
- (void)docGet:(CDVInvokedUrlCommand *)command;

- (FIRQuery *)processQueries:(NSArray *)queries ForQuery:(FIRQuery *)query;

- (NSString *)getDatePrefix;

@property(strong) FIRFirestore *firestore;
@property(strong) NSString *datePrefix;
@end
