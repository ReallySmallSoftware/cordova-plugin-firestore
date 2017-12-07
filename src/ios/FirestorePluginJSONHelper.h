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
+ (void)setDatePrefix:(NSString *)datePrefix;

@end

#endif /* FirestorePluginJSONHelper_h */
