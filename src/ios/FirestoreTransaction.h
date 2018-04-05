#import <Cordova/CDVPlugin.h>

typedef NS_ENUM(NSInteger, FirestoreTransactionOperationType) {
    NONE,
    SET,
    UPDATE,
    DELETE,
    GET,
    RESOLVE
};

@interface FirestoreTransaction : NSObject

@property(strong) NSString *collectionPath;
@property(strong) NSString *docId;
@property(strong) NSDictionary *data;
@property(strong) NSDictionary *options;
@property(assign) FirestoreTransactionOperationType transactionOperationType;

@end

@interface FirestoreTransactionQueue : NSObject

@property(strong) NSString *result;
@property(strong) NSMutableArray *queue;
@property(strong) CDVPluginResult *pluginResult;

@end
