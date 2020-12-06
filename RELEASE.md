# Release

The app is published in the [play store](https://play.google.com/store/apps/details?id=ch.famoser.mensa&hl=de_CH) and is waiting to be published in the [f-droid store](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/6927).

Release checklist:

- [ ] increase the `versionCode` and adapt the `versionName` in `app/build.gradle`.
- [ ] upload the signed abb to the play store
- [ ] generate a signed apk
- [ ] write a changelog in fastlane metadata
- [ ] commit
- [ ] create a new release on github with the `versionName` and attach the signed apk

The key store can be found in `assets/keystore.jks`, the name of the key is `upload`.  
The password is the personal strong passord of the author.