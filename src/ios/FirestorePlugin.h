#import <Cordova/CDVPlugin.h>
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
- (void)docDelete:(CDVInvokedUrlCommand *)command;

- (FIRQuery *)processQueries:(NSArray *)queries ForQuery:(FIRQuery *)query;
- (FIRQuery *)processQueryLimit:(FIRQuery *)query ForValue:(NSObject *)value;
- (FIRQuery *)processQueryWhere:(FIRQuery *)query ForValue:(NSObject *)value;
- (FIRQuery *)processQueryOrderBy:(FIRQuery *)query ForValue:(NSObject *)value;
- (FIRQuery *)processQueryStartAfter:(FIRQuery *)query ForValue:(NSObject *)value;
- (FIRQuery *)processQueryStartAt:(FIRQuery *)query ForValue:(NSObject *)value;
- (FIRQuery *)processQueryEndAt:(FIRQuery *)query ForValue:(NSObject *)value;
- (FIRQuery *)processQueryEndBefore:(FIRQuery *)query ForValue:(NSObject *)value;

- (FIRQueryListenOptions *)getQueryListenOptions:(NSDictionary *)options;

@property(strong) FIRFirestore *firestore;
@property(strong) NSMutableDictionary *listeners;

@end

typedef void (^DocumentSetBlock)(NSError *_Nullable error);

