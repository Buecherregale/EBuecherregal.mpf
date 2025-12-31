package dev.buecherregale.ebook_reader

import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import dev.buecherregale.ebook_reader.core.service.LibraryService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.ebook_reader.ui.navigation.Screen
import dev.buecherregale.ebook_reader.ui.screens.LibraryOverviewScreen
import dev.buecherregale.ebook_reader.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

expect fun platformModule(): Module

@OptIn(KoinExperimentalAPI::class)
val commonModule: Module = module {
    viewModelOf(::LibraryViewModel)

    navigation<Screen.LibraryOverview> { route ->
        LibraryOverviewScreen(viewModel = koinViewModel())
    }

    singleOf(::JsonUtil)

    singleOf(::SettingsManager)
    singleOf(::BookService)
    singleOf(::DictionaryService)
    singleOf(::LibraryService)
}