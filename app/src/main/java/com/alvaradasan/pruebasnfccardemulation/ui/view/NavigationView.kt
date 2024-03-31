package com.alvaradasan.pruebasnfccardemulation.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alvaradasan.pruebasnfccardemulation.ui.view.cardemulation.CardEmulationMainView
import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcFindInfoView
import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcInfoView
import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcMainView
import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcRWView
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation.CardEmulationMainViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcMainViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcRWViewModel

enum class Views {
    MAIN,
    NFC_MAIN,
    NFC_INFO,
    NFC_FIND_INFO,
    NFC_NDEF_READ,
    BEAM_MAIN
}

@Composable
fun NavigationView(
    nfcMainViewModel: NfcMainViewModel,
    nfcRWViewModel: NfcRWViewModel,
    cardEmulationMainViewModel: CardEmulationMainViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "${Views.MAIN}"){
        composable("${Views.MAIN}") { MainView(navController) }

        composable("${Views.NFC_MAIN}") { NfcMainView(nfcMainViewModel, navController) }
        composable("${Views.NFC_INFO}/{info}") { backStackEntry ->
            NfcInfoView(info = backStackEntry.arguments?.getString("info"))
        }
        composable("${Views.NFC_FIND_INFO}") { NfcFindInfoView() }
        composable("${Views.NFC_NDEF_READ}") { NfcRWView(nfcRWViewModel, data = "") }
        composable("${Views.NFC_NDEF_READ}/{data}") { backStackEntry ->
            NfcRWView(nfcRWViewModel, data = backStackEntry.arguments?.getString("data"))
        }

        composable(Views.BEAM_MAIN.toString()) { CardEmulationMainView(cardEmulationMainViewModel) }
    }
}