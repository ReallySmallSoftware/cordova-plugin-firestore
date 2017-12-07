//
//  FirestorePluginJSONHelper.m
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#import "FirestorePluginJSONHelper.h"
#import "FirestorePlugin.h"

@implementation FirestorePluginJSONHelper;

static NSString *datePrefix = @"__DATE:";

+ (NSDictionary *)toJSON:(NSDictionary *)values {
    NSMutableDictionary *result = [[NSMutableDictionary alloc]initWithCapacity:[values count]];
    
    for (id key in values) {
        id value = [values objectForKey:key];
        
        if ([value isKindOfClass:[NSDate class]]) {
            
            NSDate *date = (NSDate *)value;
            
            NSMutableString *dateString = [[NSMutableString alloc] init];
            [dateString appendString:datePrefix];
            [dateString appendString:[NSString stringWithFormat:@"%.0f000",[date timeIntervalSince1970]]];
            value = dateString;
        }
        
        [result setObject:value forKey:key];
    }
    
    return result;
}

+ (void)setDatePrefix:(NSString *)newDatePrefix {
    datePrefix = newDatePrefix;
}

@end
