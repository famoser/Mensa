package ch.famoser.mensa.testServices

import ch.famoser.mensa.services.IAssetService

class InMemoryAssetService(private val filenameContentMap: Map<String, String>) : IAssetService {
    override fun readStringFile(fileName: String): String? {
        return filenameContentMap[fileName]
    }
}