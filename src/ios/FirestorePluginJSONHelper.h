//
//  FirestorePluginJSONHelper.h
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#ifndef FirestorePluginJSONHelper_h
#define FirestorePluginJSONHelper_h

@interface FirestorePluginJSONHelper : NSObject

+ (NSDictionary *)toJSON:(NSDictionary *)values;
+ (NSDictionary *)fromJSON:(NSDictionary *)values;
+ (void)setReferencePrefix:(NSString *)referencePrefix;
+ (void)setGeopointPrefix:(NSString *)geopointPrefix;
+ (void)setDatePrefix:(NSString *)datePrefix;
+ (void)setTimestampPrefix:(NSString *)timestampPrefix;
+ (void)setFieldValueDelete:(NSString *)fieldValueDelete;
+ (void)setFieldValueServerTimestamp:(NSString *)fieldValueServerTimestamp;
+ (NSObject *)parseSpecial:(NSObject *)value;

@end

#endif /* FirestorePluginJSONHelper_h */
