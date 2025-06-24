package org.renxo.deeplinkapplication.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.MyApplication.Companion.preferenceManager
import org.renxo.deeplinkapplication.models.DetailResponse
import org.renxo.deeplinkapplication.screens.DeepLinkScreen
import org.renxo.deeplinkapplication.screens.EditDataScreen
import org.renxo.deeplinkapplication.screens.EditScreen
import org.renxo.deeplinkapplication.screens.OtherUserScreen
import org.renxo.deeplinkapplication.screens.RegisterScreen
import org.renxo.deeplinkapplication.screens.ScanningScreen
import org.renxo.deeplinkapplication.screens.SelectionScreen
import org.renxo.deeplinkapplication.screens.ShowMyVisitingCardScreen
import org.renxo.deeplinkapplication.screens.SplashScreen
import org.renxo.deeplinkapplication.utils.LocalMainViewModelProvider
import org.renxo.deeplinkapplication.utils.MyAnimation
import org.renxo.deeplinkapplication.utils.getRandomSessionId
import org.renxo.deeplinkapplication.utils.json
import org.renxo.deeplinkapplication.viewmodels.EditDataVM
import org.renxo.deeplinkapplication.viewmodels.OtherUserInfoVM


@Composable
fun AppNavGraph(
) {
    val mainVM = LocalMainViewModelProvider.current
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // This ensures proper padding when the keyboard is visible
            .navigationBarsPadding() // Respect bottom nav bar
            .statusBarsPadding()
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            navController = navController,
            startDestination = AppRoutes.SplashPage,
            enterTransition = { MyAnimation.myEnterAnimation() },
            exitTransition = { MyAnimation.myExitAnimation() },
            popEnterTransition = { MyAnimation.myEnterAnimation() },
            popExitTransition = { MyAnimation.myExitAnimation() },
        ) {

            composable<AppRoutes.SplashPage> {
                SplashScreen {
                    navController.navigateTo(AppRoutes.SelectionPage, finish = true)
                }
            }
            composable<AppRoutes.ShowMyVisitingCardPage> {
                ShowMyVisitingCardScreen()
            }
            composable<AppRoutes.EditDataPage> {
                val data =
                    remember { json.decodeFromString<DetailResponse>(it.toRoute<AppRoutes.EditDataPage>().data) }
                val viewModel = hiltViewModel<EditDataVM>()
                LaunchedEffect(viewModel) {
                    viewModel.setData(data.fields)
                }

                EditDataScreen(viewModel) {

                }
            }
            composable<AppRoutes.ScanningPage> {
                ScanningScreen(navigate = { id, templateId ->
                    navController.navigateTo(
                        AppRoutes.OtherUserPage(id.toString(), templateId),
                        finishAll = true
                    )
                }, navigateWithData = {
                    navController.navigateTo(AppRoutes.EditDataPage(json.encodeToString(it)))
                })
            }
            composable<AppRoutes.RegisterPage> {
                val session = it.toRoute<AppRoutes.RegisterPage>().session
                RegisterScreen("http://192.168.29.200:5175/", session) {
                    if (it) {
                        mainVM.checkNeedForFetchingDetails()
                    }
                    navController.finish()
                }
            }
            composable<AppRoutes.EditPage> {
                EditScreen("http://192.168.29.123:5173/api/android") {
                    navController.finish()
                }
            }
            composable<AppRoutes.OtherUserPage> {
//                val vm: WebViewVM = hiltViewModel()
                val viewModel: OtherUserInfoVM = hiltViewModel<OtherUserInfoVM>()


                val data = remember {
                    it.toRoute<AppRoutes.OtherUserPage>().apply {
//                        this.contact_id.toIntOrNull()?.let { it1 ->
////                            repeat(1000) {
////                                vm.getDetail(it1)
////                            }
//                        }
                    }
                }
                LaunchedEffect(viewModel) {
                    viewModel.getContactDetails(data.contact_id, data.templateId.toString())
                }
                OtherUserScreen(
//                    "http://192.168.31.171:5173?contact_id=${data.contact_id}&template_id=${data.templateId ?: ""}",
//                    "http://192.168.29.98:5173/",
                    data.contact_id
                ) {
                    navController.navigateTo(AppRoutes.SelectionPage, finishAll = true)
                }
            }
            composable<AppRoutes.SelectionPage> {
                val scope = rememberCoroutineScope()
                SelectionScreen(onScanClick = {
                    navController.navigateTo(AppRoutes.ScanningPage)
                }, onRegisterClick = {
//                    navController.navigateTo(AppRoutes.WebViewPage("101", 103))
                    scope.launch {
                        checkSessionId {
                            navController.navigateTo(AppRoutes.RegisterPage(it))
                        }
                    }
                }, onShowClick = {
                    navController.navigateTo(AppRoutes.ShowMyVisitingCardPage)
                }, onEditClick = {
                    navController.navigateTo(AppRoutes.EditPage)
                })
            }
            composable<AppRoutes.DeepLinkPage>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern =
                            "https://ronil-renxo.github.io/deep-link?id={id}&template_id={templateId}"
                    },
                    navDeepLink {
                        uriPattern =
                            "http://ronil-renxo.github.io/deep-link?id={id}&template_id={templateId}"
                    },
                    navDeepLink {
                        uriPattern =
                            "ronil-renxo.github.io://deep-link?id={id}&template_id={templateId}"
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.toRoute<AppRoutes.DeepLinkPage>().id
                val templateId = backStackEntry.toRoute<AppRoutes.DeepLinkPage>().templateId
                DeepLinkScreen(navigate = {
                    navController.navigateTo(
                        AppRoutes.OtherUserPage(id, templateId?.toIntOrNull()),
                        finishAll = true
                    )
                }) {
                    navController.navigateTo(AppRoutes.SelectionPage, finishAll = true)
                }
            }
        }


    }
}

private suspend fun checkSessionId(callback: (String) -> Unit) {
    if (preferenceManager.getSessionId().isNullOrEmpty()) {
        preferenceManager.setSessionId(getRandomSessionId())
    }
    preferenceManager.getSessionId()?.let { callback(it) }

}

