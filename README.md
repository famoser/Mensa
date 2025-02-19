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

For UZH, look in [uzh/locations_zfv.json](./app/src/main/assets/uzh/locations_zfv.json).The `infoUrlSlug` must match the homepage slug (e.g. `raemi59` for `https://www.mensa.uzh.ch/en/standorte/raemi59.html`). The `slug` must match the slug used in the GraphQL endpoint of [ZFV](https://api.zfv.ch/graphql) (do a query on location and kitchen using these values). Note that this API needs an API key. There a multiple other APIs (e.g. [food2020](https://api.app.food2050.ch/), [mensaoffice](`https://api.mensaoffice.de/api/PDF/get/509`)); the chosen ZFV API needs just a single request, which is why it was chosen.

For ETH, look in [eth/locations.json](./app/src/main/assets/eth/locations.json). The `infoUrlSlug` must match the hompage slug (e.g. `zentrum/clausiusbar` for `https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/zentrum/clausiusbar.html`). The `idSlug` must be the id of the menu plan (e.g. for Clasiusbar, the menu plan linked [here](https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/menueplaene.html) has the URL `https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/menueplaene/offerDay.html?language=de&date=2022-08-22&id=4`).

For implementation details how the links are constructed (to try it out yourself), check out [ETHMensaProvider2.kt](./app/src/main/java/ch/famoser/mensa/services/providers/ETHMensaProvider2.kt) and [UZHMensaProvider2.kt](./app/src/main/java/ch/famoser/mensa/services/providers/UZHMensaProvider2.kt).


## Release Checklist

Release checklist:

- [ ] ensure there is a `.gradle/gradle.properties` file with content `ZFV_API_KEY=Y2...` (full key in Bitwarden)
- [ ] increase the `versionCode` and adapt the `versionName` in `app/build.gradle`.
- [ ] upload the signed abb to the play store (use `assets/keystore.jks` with strong PW)
- [ ] generate a signed apk
- [ ] write a changelog in fastlane metadata
- [ ] commit
- [ ] create a new release on github with the `versionName` and attach the signed apk

The key store can be found in `assets/keystore.jks`, the name of the key is `upload`.  
The password is the personal strong passord of the author.


## Development Status

This project is now less relevant for me (@famoser), as I neither use Android nor live in Zürich anymore. However, there are around 3k aktive users, so this project will be maintained indefinitely until there are technical reasons that would require a complete rebuilt of the application; or the user count falls substaintially. If someone wants to take over, feel free to reach out.

Technical improvements:
- Update the technical foundations (dependencies, XML -> jetpack compose, resolve deprecation notices)
- Remove remains of various API migrations (which happen every 2 years or so)
- Refactor caches to minimize accesses which will further improve startup time

Functional improvements:
- View menu for the whole week (usecase: plan ahead whether to prepare lunch for the next day)
- Highlight vegetarian and vegan options
