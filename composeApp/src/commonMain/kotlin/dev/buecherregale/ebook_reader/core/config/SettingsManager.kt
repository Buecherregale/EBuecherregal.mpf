package dev.buecherregale.ebook_reader.core.config

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Manager class for the application settings and state. <br></br>
 * Loads and saves settings, creates states from them.
 */
@OptIn(ExperimentalUuidApi::class)
class SettingsManager(
    private val fileService: FileService,
    private val jsonUtil: JsonUtil,
    private val dictionaryService: DictionaryService
) {

    private var settings: ApplicationSettings? = null
    private var state: ApplicationState? = null

    init {

        loadOrCreate()
    }

    /**
     * Loads the config file at [.configFile]. Will fail if the config file is not present. <br></br>
     * Also builds the initial state via [.buildState].
     */
    fun load() {
        val json = fileService.read(configFile())
        settings = jsonUtil.deserialize(json)
        state = buildState()
        Logger.i("loaded application settings: $settings")
    }

    /**
     * Checks if the config file exists. If not creates BUT DOES NOT SAVE a blank config and state.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun loadOrCreate() {
        if (!fileService.exists(configFile())) {
            Logger.i("no existing settings found, creating blank...")
            settings = ApplicationSettings()
            state = ApplicationState()
        } else load()
    }

    /**
     * The reference to the config file.
     * Even if the file does not exist, the valid ref will be returned.
     *
     * @return the (theoretical) location of the config
     */
    fun configFile(): FileRef {
        return fileService.getAppDirectory(AppDirectory.CONFIG).resolve(CONFIG_FILENAME)
    }

    /**
     * Saves the config as json to the file at [.configFile].
     */
    fun save() {
        Logger.d("saving settings at: ${configFile()}")
        val json: String = jsonUtil.serialize(settings!!)
        fileService.write(configFile(), json)
    }

    /**
     * Builds the initial state the config describes, OVERWRITING the existent state. <br></br>
     * Fails if no config is loaded.
     *
     * @return the initial state
     */
    fun buildState(): ApplicationState {
        val state = ApplicationState()
        if (settings!!.activeDictionaryId != null) state.activeDictionary = dictionaryService.open(settings!!.activeDictionaryId!!)

        return state
    }

    /**
     * Set the active dictionary, updating the state as well.
     *
     * @param dictionaryId the name of the new dictionary
     */
    fun activeDictionaryId(dictionaryId: Uuid) {
        settings?.activeDictionaryId = dictionaryId
        state?.activeDictionary = dictionaryService.open(settings!!.activeDictionaryId!!)
    }

    companion object {
        const val CONFIG_FILENAME: String = "settings.json"
    }
}