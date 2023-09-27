# Mensa
![API version](https://img.shields.io/badge/API-21-green.svg)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE) 

ETHZ & UZH mensas in zurich.

The app is published in the [play store](https://play.google.com/store/apps/details?id=ch.famoser.mensa&hl=de_CH) and in the [f-droid store](https://f-droid.org/en/packages/ch.famoser.mensa/).

<table>
    <tbody>
        <tr>
            <td>Overview</td>
            <td>Mensa View</td>
        </tr>
        <tr>
            <td><img src="assets/screenshot_phone_overview_white.png?raw=true" alt="Screenshot Main"></td>
            <td><img src="assets/screenshot_phone_detail_white.png?raw=true" alt="Screenshot Mensa"></td>
        </tr>
        <tr>
            <td><img src="assets/screenshot_phone_overview_black.png?raw=true" alt="Screenshot Main"></td>
            <td><img src="assets/screenshot_phone_detail_black.png?raw=true" alt="Screenshot Mensa"></td>
        </tr>
        <tr>
            <td colspan="2">Tablet view</td>
        </tr>
        <tr>
            <td colspan="2"><img src="assets/screenshot_tablet_white.jpg?raw=true" alt="Screenshot Tablet"></td>
        </tr>
        <tr>
            <td colspan="2"><img src="assets/screenshot_tablet_black.jpg?raw=true" alt="Screenshot Tablet"></td>
        </tr>
    </tbody>
</table>

## Update mensa details

Has a new Mensa opened, or are the opening times no longer accurate? Feel free to directly submit a PR!

For UZH, look in [uzh/locations_rss.json](./app/src/main/assets/uzh/locations_rss.json).The `infoUrlSlug` must match the homepage slug (e.g. `raemi59` for `https://www.mensa.uzh.ch/en/standorte/raemi59.html`). The `idSlug` must be the id of the menu plan (e.g. for Rämi 59, the menu plan linked [here](https://www.mensa.uzh.ch/en/menueplaene/raemi59/montag.html) has the URL `https://api.mensaoffice.de/api/PDF/get/509`).

For ETH, look in [eth/locations.json](./app/src/main/assets/eth/locations.json). The `infoUrlSlug` must match the hompage slug (e.g. `zentrum/clausiusbar` for `https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/zentrum/clausiusbar.html`). The `idSlug` must be the id of the menu plan (e.g. for Clasiusbar, the menu plan linked [here](https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/menueplaene.html) has the URL `https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/menueplaene/offerDay.html?language=de&date=2022-08-22&id=4`).

For implementation details how the links are constructed (to try it out yourself), check out [ETHMensaProvider.kt](./app/src/main/java/ch/famoser/mensa/services/providers/ETHMensaProvider.kt) and [UZHMensaProvider.kt](./app/src/main/java/ch/famoser/mensa/services/providers/UZHMensaProvider.kt).

## Release Checklist

Release checklist:

- [ ] increase the `versionCode` and adapt the `versionName` in `app/build.gradle`.
- [ ] upload the signed abb to the play store (use `assets/keystore.jks` with strong PW)
- [ ] generate a signed apk
- [ ] write a changelog in fastlane metadata
- [ ] commit
- [ ] create a new release on github with the `versionName` and attach the signed apk

The key store can be found in `assets/keystore.jks`, the name of the key is `upload`.  
The password is the personal strong passord of the author.
