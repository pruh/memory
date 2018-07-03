# memory
Simple memory game for android. The app is built using MVP architecture, Koin for DI and RxJava for async calls. The purpose of the app is to show how to use MVP pattern to separete views from business logic. Using MVP we can test most non-view classes on the host machine with no need to start the app on the device or emulator. For reference, take a look at [GamePresenterImplTest](https://github.com/pruh/memory/blob/master/app/src/test/java/space/naboo/memory/game/GamePresenterImplTest.kt) on how to test non-view classes.

## Set up
Flickr API key needs to be generated in order ti use the app. After you obtain the key put it in your local.properties and rebuild the app:

```
flickr_api_key=YOUR_KEY_GOES_HERE
```
