#import <Cordova/CDVPlugin.h>

typedef NS_ENUM(NSInteger, FirestoreTransactionStatus) {
    READY,
    PROCESSING,
    COMPLETE
};

typedef NS_ENUM(NSInteger, FirestoreTransactionType) {
    UNDEFINED,
    SET,
    UPDATE,
    DELETE,
    GET
};

@interface FirestoreTransaction : NSObject

@property(strong) NSString *transactionId;
@property(assign) FirestoreTransactionType transactionType;
@property(assign) BOOL transactionResolved;
@property(assign) FirestoreTransactionStatus transactionStatus;
@property(strong) NSString *docId;
@property(strong) NSString *collection;
@property(strong) NSDictionary *data;
@property(strong) NSDictionary *options;
@property(strong) CDVPluginResult *pluginResult;
@property(strong) NSString *result;

@end

