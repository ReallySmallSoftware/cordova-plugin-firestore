#import "FirestorePlugin.h"
#import "FirestorePluginJSONHelper.h"

#import <Cordova/CDVAvailability.h>

@implementation FirestorePlugin

- (void)pluginInitialize {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
}

- (void)collectionOnSnapshot:(CDVInvokedUrlCommand *)command {
    NSString *collection =[command argumentAtIndex:0 withDefault:@"/" andClass:[NSString class]];
    NSArray *queries = [command argumentAtIndex:1 withDefault:@[] andClass:[NSArray class]];
    NSDictionary *options = [command argumentAtIndex:2 withDefault:@{} andClass:[NSDictionary class]];
    NSString *callbackId = [command argumentAtIndex:3 withDefault:@"" andClass:[NSString class]];
    
    FIRCollectionReference *collectionReference = [self.firestore collectionWithPath:collection];
    
    // query listen options
    FIRQuery *query = [self processQueries:queries ForQuery:collectionReference];
    
    [query addSnapshotListener:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (snapshot == nil) {
            NSLog(@"Collection snapshot listener error %@", error);
            return;
        }
        
        CDVPluginResult *pluginResult = [FirestorePluginResultHelper createQueryPluginResult:snapshot :YES];
        
        NSLog(@"Got collection snapshot data");

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
    
//    [self.observeRemovers setObject:[ObjectRemover observerRemoverWithQuery:query andHandle: handle] forKey:key];
}

- (FIRQuery *)processQueries:(NSArray *)queries ForQuery:(FIRQuery *)query {
    for (NSObject *query in queries) {
        NSLog(@"Query type %@", query);
    }
    
    return query;
}

- (void)collectionUnsubscribe:(CDVInvokedUrlCommand *)command {
}
- (void)collectionAdd:(CDVInvokedUrlCommand *)command {
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
}

- (void)docUpdate:(CDVInvokedUrlCommand *)command {
}

- (void)docOnSnapshot:(CDVInvokedUrlCommand *)command {
}

- (void)docUnsubscribe:(CDVInvokedUrlCommand *)command {
}

- (void)docGet:(CDVInvokedUrlCommand *)command {
}

- (NSString *)getDatePrefix {
    return self.datePrefix;
}

- (void)getDate:(CDVInvokedUrlCommand *)command {
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  NSLocale *enUSPOSIXLocale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
  [dateFormatter setLocale:enUSPOSIXLocale];
  [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZZZZZ"];

  NSDate *now = [NSDate date];
  NSString *iso8601String = [dateFormatter stringFromDate:now];

  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:iso8601String];
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
