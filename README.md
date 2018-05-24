# Cordova Firestore Plugin
A Google Firebase Firestore plugin to enable realtime synchronisation between app and cloud and automatically handle limited connectivity.

# What is Firestore?
From the Google documentation (https://firebase.google.com/docs/firestore/):

> Cloud Firestore is a flexible, scalable database for mobile, web, and server development from Firebase and Google Cloud Platform. Like Firebase Realtime Database, it keeps your data in sync across client apps through realtime listeners and offers offline support for mobile and web so you can build responsive apps that work regardless of network latency or Internet connectivity. Cloud Firestore also offers seamless integration with other Firebase and Google Cloud Platform products, including Cloud Functions

# Supported platforms
This plugin supports the following platforms:

- Android
- iOS
- Browser

# Installation
`cordova plugin add cordova-plugin-firestore --variable ANDROID_FIREBASE_CORE_VERSION=16.0.0 --variable ANDROID_FIREBASE_FIRESTORE_VERSION=17.0.1 --save`

or

`phonegap plugin add cordova-plugin-firestore --variable ANDROID_FIREBASE_CORE_VERSION=16.0.0 --variable ANDROID_FIREBASE_FIRESTORE_VERSION=17.0.1`

Omitting `FIREBASE_VERSION` will use a default value.

## Dependencies
### Promises
This plugin uses Promises. If you want to use this with Android 4.4 then you will need to include a `Promise` polyfill.

## Firebase configuration
### Android
You must ensure that `google-services.json` is put in the correct location. This can be achieved using the following in your `config.xml`:

```
<platform name="android">
    <resource-file src="google-services.json" target="google-services.json" />
</platform>
```

#### Dependencies
##### cordova-support-google-services

In order to ensure Firebase initialises correctly on Android this plugin can be used. This is not automatically added as a dependency to allow for the configuration it performs to be done manually if desired.

### iOS
iOS requires `GoogleService-Info.plist` is put in the correct location. Similarly this can be done as follows:
```
<platform name="ios">
    <resource-file src="GoogleService-Info.plist" />
</platform>
```
#### Keychain Sharing Capability
If using multiple Firebase plugins it may be necessary to enable this.

# What is supported?
## Firestore
- collection()
- runTransaction(updateFunction)

## DocumentSnapshot
- data()
- get(fieldPath)
- exists
- id
- ref

## QuerySnapshot
- docs
- empty
- size

## DocumentReference
- delete()
- get()
- onSnapshot(optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError)
- set(data, options)
- update(data)
- id
- parent

## Query
- endAt(snapshotOrVarArgs)
- endBefore(snapshotOrVarArgs)
- limit(limit)
- orderBy(field, direction)
- get()
- onSnapshot(callback, options)
- startAfter(snapshotOrVarArgs)
- startAt(snapshotOrVarArgs)
- where(fieldPath, opStr, passedValue)

## CollectionReference (inherits from Query)
- add(data)
- id
- doc(id)

## Transaction
- get()
- delete()
- set()
- update()

## FieldValue
- FieldValue.delete()
- FieldValue.serverTimestamp()

# Initialisation
The plugin can be initialised as follows:

```
      var options = {
        datePrefix: '__DATE:',
        "fieldValueDelete": "__DELETE",
        "fieldValueServerTimestamp" : "__SERVERTIMESTAMP",
        "persist": true
      };

      if (cordova.platformId === "browser") {

        options.browser = {
          apiKey: 'my API key',
          authDomain: 'my domain',
          projectId: 'my project id',
        };
      }

      Firestore.initialise(options).then(function(database) {
        myDatabaseReference = database;
      });
    });
```

This is initialised as a promise to allow the Browser implementation to dynamically add a reference to the Firestore Javascript SDK.

## Dates
Because data is transferred to the client as JSON there is extra logic in place to handle the conversion of dates for some operations.

When initialising the plugin you can set up a prefix that is applied to a string value which is used to identify it as a date. The default prefix is `__DATE:`

Therefore, when a date field is retrieved from the database by the native code it will be transferred in the JSON looking similar to the following:

```
{
    "dateField" : "__DATE:123456789"
}
```

The number is seconds since epoch.

The client will receive the field as a Javascript Date.

This conversion also happens when specify a field in a where condition.

## FieldValue constants
Similar to the situation with dates, there are special values used for `FieldValue` values:

- FieldValue.delete() equates to `__DELETE`
- FieldValue.serverTimestamp() equates to `__SERVERTIMESTAMP`

These values can be changed when initialisation is performed.

## Learnings and notes
I have learnt a number of things whilst implementing this:
- The documentation states that the database cannot be initialised in a seperate thread when using persistence. In my experience this should say it cannot be *used* in multiple threads.
- When used on Android ensure that at least `com.google.gms:google-services:3.1.1` is used in build dependencies. Earlier versions did not work for me.
- Yes, I did spell initialise() with an 's' - I am from the UK

# History
## 1.2.0
- Update Android dependency versions
- Update iOS dependency versions
- WARNING: The Android update may require you to update com.google.gms:google-services to 4.0.0, com.android.tools.build:gradle to 3.1.2 and gradle to 4.4.4 (look in platforms/android/cordova/lib/builders/GradleBuilder.js)

## 1.1.0
- Add support for FieldValue
- Add experimental support for Transactions. _Please note this is **experimental**!_
- Add startswith polyfill

## 1.0.10
- Correct log level when creating results

## 1.0.9
- Updated Dependencies
- Remove incorrect Java 7 dependency

## 1.0.8
- Ensure dates work for queries and nested data
- Implement delete()
- Update README

## 1.0.7
- Remove dependency on cordova-plugin-firebase-hooks

## 1.0.6
- Correct README History
- Make browser firebase dependency loading smarter

## 1.0.5
## 1.0.4
## 1.0.3
- Address plugin dependency issues

## 1.0.2
- Updated version
- Added firebase hooks dependency
- Corrected iOS source/header-file config

## 1.0.0
Initial release
