package dev.buecherregale.ebook_reader.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import dev.buecherregale.ebook_reader.ui.screens.LibraryDetailScreen
import dev.buecherregale.ebook_reader.ui.screens.LibraryScreen
import dev.buecherregale.ebook_reader.ui.viewmodel.LibraryDetailViewModel
import dev.buecherregale.ebook_reader.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

class Navigator(
    start: Screen
) {
    private val _backStack = mutableStateListOf(start)
    val backStack: List<Screen> get() = _backStack

    fun push(screen: Screen) {
        _backStack.add(screen)
    }

    fun pop() {
        if (_backStack.size > 1) {
            _backStack.removeLast()
        }
    }
}

@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
    single { Navigator(Screen.LibraryOverview) }

    viewModelOf(::LibraryViewModel)
    viewModel { params -> LibraryDetailViewModel(library = params.get(), get(), get()) }

    navigation<Screen.LibraryOverview> { _ ->
        LibraryScreen(viewModel = koinViewModel())
    }
    navigation<Screen.LibraryDetail> { route ->
        LibraryDetailScreen(
            library = route.library,
            viewModel = koinViewModel(
                key = route.library.name // name is unique
            ) {
                parametersOf(route.library)
            }
        )
    }
}