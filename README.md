Cordova Firestore Plugin
==

A Google Firebase Firestore plugin to enable realtime synchronisation between app and cloud and automatically handle limited connectivity.

What is Firestore?
--

From the Google documentation (https://firebase.google.com/docs/firestore/):

> Cloud Firestore is a flexible, scalable database for mobile, web, and server development from Firebase and Google Cloud Platform. Like Firebase Realtime Database, it keeps your data in sync across client apps through realtime listeners and offers offline support for mobile and web so you can build responsive apps that work regardless of network latency or Internet connectivity. Cloud Firestore also offers seamless integration with other Firebase and Google Cloud Platform products, including Cloud Functions

Supported platforms
--
This plugin supports the following platforms:

- Android
- iOS
- Browser

Status
--
This is very much a work in progress. Although the planned functionality is believed to work on the above platforms there are no guarantees.

This is also the first Cordova plugin that I have written and was probably a fairly ambitious one to start with given not only the relative complexity of the technology being supported but also that I had never touched Objective C prior to this.

What is supported?
--

DocumentSnapshot
- data()
- get(fieldPath)
- exists
- id
- ref

QuerySnapshot
- docs
- empty
- size

DocumentReference
- get()
- onSnapshot(optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError)
- set(data, options)
- update(data)
- id
- parent

Query
- endAt(snapshotOrVarArgs)
- endBefore(snapshotOrVarArgs)
- limit(limit)
- orderBy(field, direction)
- get()
- onSnapshot(callback, options)
- startAfter(snapshotOrVarArgs)
- startAt(snapshotOrVarArgs)
- where(fieldPath, opStr, passedValue)


CollectionReference (inherits from Query)
- add(data)
- id
- doc(id)

Initialisation
--
The plugin can be initialised as follows:

```
      var options = {
        datePrefix: '__DATE:',
        persist: true
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

Dates
--
Because data is transferred to the client as JSON there is extra logic in place to handle the conversion of dates for some operations.

When initialising the plugin you can set up a prefix that is applied to a string value which is used to identify it as a date. The default prefix is `__DATE:`

Therefore, when a date field is retrieved from the database by the native code it will be transferred in the JSON looking similar to the following:

```
{
    "dateField" : "__DATE:123456789"
}
```

The number is seconds since EPOC.

The client will receive the field as a Javascript Date.

This conversion also happens when specify a field in a where condition.

Learnings and notes
--
I have learnt a number of things whilst implementing this:
- The documentation states that the database cannot be initialised in a seperate thread when using persistence. In my experience this should say it cannot be *used* in multiple threads.
- When used on Android ensure that at least `com.google.gms:google-services:3.1.1` is used in build dependencies. Earlier versions did not work for me.
- Yes, I did spell initialise() with an 's' - I am from the UK
