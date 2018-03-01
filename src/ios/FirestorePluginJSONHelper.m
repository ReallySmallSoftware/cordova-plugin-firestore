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
        } else if ([value isKindOfClass:[NSDictionary class]]) {
            value = [self toJSON:value];
        }
        
        [result setObject:value forKey:key];
    }
    
    return result;
}

+ (NSDictionary *)fromJSON:(NSDictionary *)values {
    NSMutableDictionary *result = [[NSMutableDictionary alloc]initWithCapacity:[values count]];
    
    for (id key in values) {
        id value = [values objectForKey:key];
        
        if ([value isKindOfClass:[NSDictionary class]]) {
            value = [self fromJSON:value];
        } else {
            value = [self parseSpecial:value];
        }
        
        [result setObject:value forKey:key];
    }
    
    return result;
}

+ (NSObject *)parseSpecial:(NSObject *)value {
    
    if ([value isKindOfClass:[NSString class]]) {
        NSString *stringValue = (NSString *)value;
        
        NSUInteger datePrefixLength = (NSUInteger)[FirestorePluginJSONHelper getDatePrefix].length;
    
        if ([stringValue length] > datePrefixLength && [datePrefix isEqualToString:[stringValue substringToIndex:datePrefixLength]]) {
            NSTimeInterval timestamp = [[stringValue substringFromIndex:datePrefixLength] doubleValue];
            timestamp /= 1000;
            NSDate *date = [[NSDate alloc] initWithTimeIntervalSince1970:timestamp];
            value = date;
        }
    }
    
    return value;
}

+ (void)setDatePrefix:(NSString *)newDatePrefix {
    datePrefix = newDatePrefix;
}

+ (NSString *)getDatePrefix {
    return datePrefix;
}

@end

