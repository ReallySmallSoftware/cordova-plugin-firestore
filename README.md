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

  - **Step1** Install this plugin

  ```bash
  $> cordova plugin add cordova-plugin-firestore --save
  ```

  - **Step2** Download `google-services.json`, then put it at `(your_project_dir)/google-services.json`
    Hint: [Get a config file for your Android App](https://support.google.com/firebase/answer/7015592#android)

    Then add the below three lines into `(your_project_dir)/config.xml` file.

    ```xml
    <platform name="android">
        <resource-file src="google-services.json" target="app/google-services.json" />
    </platform>
    ```

  - **Step3** Download `GoogleService-Info.plist`, then put it at `(your_project_dir)/GoogleService-Info.plist`
    Hint: [Get a config file for your iOS App](https://support.google.com/firebase/answer/7015592#ios)

    Then add the below three lines into `(your_project_dir)/config.xml` file.

    ```xml
    <platform name="ios">
        <resource-file src="GoogleService-Info.plist" />
    </platform>
    ```

  - **Step4** Make sure your Firestore rule

    ```
    service cloud.firestore {
      match /databases/{database}/documents {
        match /{document=**} {
          allow read, write : if request.auth != null;  // This is an example.
        }
      }
    }
    ```

  - **Step5** Write HelloWorld code
    ```js
    var options = {
      "datePrefix": '__DATE:',
      "fieldValueDelete": "__DELETE",
      "fieldValueServerTimestamp" : "__SERVERTIMESTAMP",
      "persist": true,
      "config" : {}
    };

    if (cordova.platformId === "browser") {

      options.config = {
        apiKey: "AIzaSyB1Tqdt15UXdQTuvgD1_KkbSX0xTaUtpcw",
        authDomain: "localhost",
        projectId: "geofire-example-572f9"
      };
    }

    Firestore.initialise(options).then(function(db) {
      // Add a second document with a generated ID.
      db.collection("users").add({
          first: "Alan",
          middle: "Mathison",
          last: "Turing",
          born: 1912
      })
      .then(function(docRef) {
          console.log("Document written with ID: ", docRef.id);
      })
      .catch(function(error) {
          console.error("Error adding document: ", error);
      });
    });
    ```

  - **Step6** Let's run it!
    ```
    $> cordova platform add browser // android, ios

    $> cordova run browser
    ```

## Install optional variables

  - `Android` **ANDROID_FIREBASE_CORE_VERSION = (16.0.3)**<br>
    The `com.google.firebase:firebase-core` version.
    You can find the latest version at [here](https://firebase.google.com/docs/android/setup#available_libraries).

  - `Android` **ANDROID_FIREBASE_FIRESTORE_VERSION = (17.1.0)**<br>
    The `com.google.firebase:firebase-firestore` version.
    You can find the latest version at [here](https://firebase.google.com/docs/android/setup#available_libraries).

#### Keychain Sharing Capability
If using multiple Firebase plugins it may be necessary to enable this.

# What is supported?
## Firestore
- collection()
- runTransaction(updateFunction)

## DocumentSnapshot and QueryDataSnapshot
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
- collection(collectionPath)
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

## options.config
In the above example this is being used for the browser version, but it can also be used for Android and iOS to specify different databases than the default in the `google-services.json` and `GoogleService-Info.plist` files.

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

### timestampsInSnapshots
This option is provided for compatibility with upcoming Firestore changes. By default this option is set to `false` but you
are advised to heed the following if you wish for your app to continue to work in the browser implementation:

```
The behavior for Date objects stored in Firestore is going to change
AND YOUR APP MAY BREAK.
To hide this warning and ensure your app does not break, you need to add the
following code to your app before calling any other Cloud Firestore methods:

  const firestore = firebase.firestore();
  const settings = {/* your settings... */ timestampsInSnapshots: true};
  firestore.settings(settings);

With this change, timestamps stored in Cloud Firestore will be read back as
Firebase Timestamp objects instead of as system Date objects. So you will also
need to update code expecting a Date to instead expect a Timestamp. For example:

  // Old:
  const date = snapshot.get('created_at');
  // New:
  const timestamp = snapshot.get('created_at');
  const date = timestamp.toDate();

Please audit all existing usages of Date when you enable the new behavior. In a
future release, the behavior will change to the new behavior, so if you do not
follow these steps, YOUR APP MAY BREAK.
```

## FieldValue constants
Similar to the situation with dates, there are special values used for `FieldValue` values:

- FieldValue.delete() equates to `__DELETE`
- FieldValue.serverTimestamp() equates to `__SERVERTIMESTAMP`

These values can be changed when initialisation is performed.

## Learnings and notes
I have learnt a number of things whilst implementing this:
- The documentation states that the database cannot be initialised in a seperate thread when using persistence. In my experience this should say it cannot be *used* in multiple threads.
- When used on Android ensure that at least `com.google.gms:google-services:3.1.1` is used in build dependencies. Earlier versions did not work for me.
- Yes, I did spell initialise() with an 's' - The original plugin developer @ReallySmallSoftware is from the UK
