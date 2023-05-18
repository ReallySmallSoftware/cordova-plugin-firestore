//
//  FirestorePluginJSONHelper.m
//  leave your Egos at the door
//
//  Created by Richard WIndley on 05/12/2017.
//

#import "FirestorePluginJSONHelper.h"
#import "FirestorePlugin.h"
@import Firebase;

@implementation FirestorePluginJSONHelper;

static NSString *datePrefix = @"__DATE:";
static NSString *timestampPrefix = @"__TIMESTAMP:";
static NSString *geopointPrefix = @"__GEOPOINT:";
static NSString *referencePrefix = @"__REFERENCE:";
static NSString *fieldValueDelete = @"__DELETE";
static NSString *fieldValueServerTimestamp = @"__SERVERTIMESTAMP";
static NSString *fieldValueIncrement = @"__INCREMENT";
static NSString *fieldValueArrayRemove = @"__ARRAYREMOVE";
static NSString *fieldValueArrayUnion = @"__ARRAYUNION";

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
        } else if ([value isKindOfClass:[FIRTimestamp class]]) {

            FIRTimestamp *timestamp = (FIRTimestamp *)value;

            NSMutableString *timestampString = [[NSMutableString alloc] init];
            [timestampString appendString:timestampPrefix];
            [timestampString appendString:[NSString stringWithFormat:@"%lld_%d",[timestamp seconds], [timestamp nanoseconds]]];
            value = timestampString;
        } else if ([value isKindOfClass:[FIRGeoPoint class]]) {
            FIRGeoPoint *point = (FIRGeoPoint *)value;
            
            NSMutableString *geopointString = [[NSMutableString alloc] init];
            [geopointString appendString:geopointPrefix];
            [geopointString appendString:[NSString stringWithFormat:@"%f,%f", point.latitude, point.longitude]];
            value = geopointString;
        } else if ([value isKindOfClass:[FIRDocumentReference class]]) {
            FIRDocumentReference *reference = (FIRDocumentReference *)value;
            
            NSMutableString *referenceString = [[NSMutableString alloc] init];
            [referenceString appendString:referencePrefix];
            [referenceString appendString:[NSString stringWithFormat:@"%s,%s", [reference.path UTF8String], [reference.documentID UTF8String]]];
            value = referenceString;
        } else if ([value isKindOfClass:[NSDictionary class]]) {
            value = [self toJSON:value];
        }

        [result setObject:value forKey:key];
    }

    return result;
}

+ (NSObject *)fromJSON:(NSObject *)value ForPlugin:(FirestorePlugin *)firestorePlugin {

    if ([value isKindOfClass:[NSString class]]) {
        NSString *stringValue = (NSString *)value;

        NSUInteger datePrefixLength = (NSUInteger)datePrefix.length;
        NSUInteger geopointPrefixLength = (NSUInteger)geopointPrefix.length;
        NSUInteger timestampPrefixLength = (NSUInteger)timestampPrefix.length;
        NSUInteger referencePrefixLength = (NSUInteger)referencePrefix.length;

        if ([stringValue length] > datePrefixLength && [datePrefix isEqualToString:[stringValue substringToIndex:datePrefixLength]]) {
            NSTimeInterval timestamp = [[stringValue substringFromIndex:datePrefixLength] doubleValue];
            timestamp /= 1000;
            NSDate *date = [[NSDate alloc] initWithTimeIntervalSince1970:timestamp];
            value = date;
        }

        if ([stringValue length] > timestampPrefixLength && [timestampPrefix isEqualToString:[stringValue substringToIndex:timestampPrefixLength]]) {
            NSArray *tmp = [[stringValue substringFromIndex:timestampPrefixLength] componentsSeparatedByString:@"_"];
            FIRTimestamp *timestamp = [[FIRTimestamp alloc] initWithSeconds:[[tmp objectAtIndex:0] longLongValue] nanoseconds:[[tmp objectAtIndex:1] intValue]];
            value = timestamp;
        }

        if ([stringValue length] > geopointPrefixLength && [geopointPrefix isEqualToString:[stringValue substringToIndex:geopointPrefixLength]]) {
            NSArray *tmp = [[stringValue substringFromIndex:geopointPrefixLength] componentsSeparatedByString:@","];
            FIRGeoPoint *geopoint = [[FIRGeoPoint alloc] initWithLatitude:[[tmp objectAtIndex:0] doubleValue] longitude:[[tmp objectAtIndex:1] doubleValue]];
            value = geopoint;
        }
      
        if ([stringValue length] > referencePrefixLength && [referencePrefix isEqualToString:[stringValue substringToIndex:referencePrefixLength]]) {
            NSArray *tmp = [[stringValue substringFromIndex:referencePrefixLength] componentsSeparatedByString:@","];
            FIRFirestore *firestore = [firestorePlugin getFirestore];
            FIRDocumentReference *reference = [[firestore collectionWithPath:@""] documentWithPath:[tmp objectAtIndex:0]]; //[[FIRDocumentReference alloc] initWithReference:[[tmp objectAtIndex:0] stringValue]];
            value = reference;
        }
        
        if ([self isWrappedFieldValue:stringValue]) {
            value = [self unwrapFieldValue:stringValue];
        }

    } else if ([value isKindOfClass:[NSDictionary class]]) {
        value = [self toSettableDictionaryInternal:(NSDictionary *)value ForPlugin:firestorePlugin];
    } else if ([value isKindOfClass:[NSArray class]]) {
        value = [self toSettableArrayInternal:(NSArray *)value ForPlugin:firestorePlugin];
    }

    return value;
}

+ (NSObject *)toSettableDictionaryInternal:(NSDictionary *)values ForPlugin:(FirestorePlugin *)firestorePlugin {
    NSMutableDictionary *result = [[NSMutableDictionary alloc]initWithCapacity:[values count]];

    for (id key in values) {
        id value = [values objectForKey:key];
        value = [self fromJSON:value ForPlugin:firestorePlugin];
        [result setObject:value forKey:key];
    }

    return result;
}

+ (NSObject *)toSettableArrayInternal:(NSArray *)values ForPlugin:(FirestorePlugin *)firestorePlugin {
    NSMutableArray *result = [[NSMutableArray alloc]initWithCapacity:[values count]];

    for (id key in values) {
        [result addObject:key];
    }

    return result;
}

+ (void)setDatePrefix:(NSString *)newDatePrefix {
    datePrefix = newDatePrefix;
}

+ (void)setTimestampPrefix:(NSString *)newTimestampPrefix {
    timestampPrefix = newTimestampPrefix;
}

+ (void)setReferencePrefix:(NSString *)newReferencePrefix {
    referencePrefix = newReferencePrefix;
}

+ (void)setGeopointPrefix:(NSString *)newGeopointPrefix {
    geopointPrefix = newGeopointPrefix;
}

+ (void)setFieldValueDelete:(NSString *)newFieldValueDelete {
    fieldValueDelete = newFieldValueDelete;
}

+ (void)setFieldValueServerTimestamp:(NSString *)newFieldValueServerTimestamp {
    fieldValueServerTimestamp = newFieldValueServerTimestamp;
}

+ (void)setFieldValueIncrement:(NSString *)newFieldValueIncrement {
    fieldValueIncrement = newFieldValueIncrement;
}

+ (void)setFieldValueArrayUnion:(NSString *)newFieldValueArrayUnion {
    fieldValueArrayUnion = newFieldValueArrayUnion;
}

+ (void)setFieldValueArrayRemove:(NSString *)newFieldValueArrayRemove {
    fieldValueArrayRemove = newFieldValueArrayRemove;
}

+(Boolean)isWrappedFieldValue:(NSString *)value {
    if ([value isEqualToString:fieldValueDelete]) {
        return true;
    }
    
    if ([value isEqualToString:fieldValueServerTimestamp]) {
        return true;
    }
    
    if ([value length] >= fieldValueIncrement.length && [[value substringToIndex:fieldValueIncrement.length] isEqualToString:fieldValueIncrement]) {
        return true;
    }
    
    if ([value length] >= fieldValueArrayUnion.length && [[value substringToIndex:fieldValueArrayUnion.length] isEqualToString:fieldValueArrayUnion]) {
        return true;
    }
    
    if ([value length] >= fieldValueArrayRemove.length &&  [[value substringToIndex:fieldValueArrayRemove.length] isEqualToString:fieldValueArrayRemove]) {
        return true;
    }
    
    return false;
}

+ (NSObject *)unwrapFieldValue:(NSString *)stringValue {
    if ([fieldValueDelete isEqualToString:stringValue]) {
        return FIRFieldValue.fieldValueForDelete;
    }

    if ([fieldValueServerTimestamp isEqualToString:stringValue]) {
        return FIRFieldValue.fieldValueForServerTimestamp;
    }

    if ([[stringValue substringToIndex:fieldValueIncrement.length] isEqualToString:fieldValueIncrement]) {
        return [FIRFieldValue fieldValueForIntegerIncrement:[[self unwrap:stringValue ForPrefix:fieldValueIncrement] longLongValue]];
    }

    if ([[stringValue substringToIndex:fieldValueArrayUnion.length] isEqualToString:fieldValueArrayUnion]) {
        NSString *unwrapped =[self unwrap:stringValue ForPrefix:fieldValueArrayUnion];
        return [FIRFieldValue fieldValueForArrayUnion:[self JSONArrayToArray:unwrapped]];
    }

    if ([[stringValue substringToIndex:fieldValueArrayRemove.length] isEqualToString:fieldValueArrayRemove]) {
        NSString *unwrapped =[self unwrap:stringValue ForPrefix:fieldValueArrayRemove];
        return [FIRFieldValue fieldValueForArrayRemove:[self JSONArrayToArray:unwrapped]];
    }
    
    return stringValue;
}

+ (NSString *)unwrap:(NSString *)stringValue ForPrefix:(NSString *)prefix {
    return [stringValue substringFromIndex:prefix.length];
}

+ (NSArray *)JSONArrayToArray:(NSString *)array {
    NSData *data = [array dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSArray *result = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
    if (error != nil) {
        NSLog(@"Error parsing JSON string: %@", error);
        return nil;
    }
    return result;
}
@end
