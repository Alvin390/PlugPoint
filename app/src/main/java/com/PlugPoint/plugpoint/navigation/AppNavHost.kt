package com.PlugPoint.plugpoint.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.ChatViewModel
import com.PlugPoint.plugpoint.data.ChatViewModelFactory
import com.PlugPoint.plugpoint.data.DarkModeViewModel
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.data.UserSearchViewModel // Updated import
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.ui.theme.screens.chat_screen.ChatScreen
import com.PlugPoint.plugpoint.ui.theme.screens.chat_screen.ChatScreen2
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier.SupplierCommodityScreen
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_view.CommodityView
import com.PlugPoint.plugpoint.ui.theme.screens.consumer_view.ConsumerView
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerProfileScreen
import com.PlugPoint.plugpoint.ui.theme.screens.login.LoginScreen
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierProfileScreen
import com.PlugPoint.plugpoint.ui.theme.screens.notifications_screen.NotificationScreen
import com.PlugPoint.plugpoint.ui.theme.screens.registration_consumer.RegistrationConsumerScreen
import com.PlugPoint.plugpoint.ui.theme.screens.registration_supplier.RegistrationSupplierScreen
import com.PlugPoint.plugpoint.ui.theme.screens.role_screen.RoleSelectionScreen
import com.PlugPoint.plugpoint.ui.theme.screens.search_screen_consumer.SearchScreenConsumer
import com.PlugPoint.plugpoint.ui.theme.screens.search_screen_supplier.SearchScreenSupplier
import com.PlugPoint.plugpoint.ui.theme.screens.settings_screen.SettingsScreen
import com.PlugPoint.plugpoint.ui.theme.screens.splashscreen.SplashScreen
import com.PlugPoint.plugpoint.ui.theme.screens.supplier_all_requests.SupplierAllRequestsScreen
import com.PlugPoint.plugpoint.ui.theme.screens.supplier_view.SupplierView

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = ROUTE_LOGIN,
    imgurViewModel: ImgurViewModel,
    darkModeViewModel: DarkModeViewModel,
    imgurAPI: ImgurAPI,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(ROUTE_REGISTRATION_SUPPLIER) {
            RegistrationSupplierScreen(navController, viewModel = authViewModel)
        }
        composable(ROUTE_REGISTRATION_CONSUMER) {
            RegistrationConsumerScreen(navController, viewModel = authViewModel)
        }
        composable("$ROUTE_SEARCH_SUPPLIER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SearchScreenSupplier(
                navController,
                viewModel = viewModel<UserSearchViewModel>(), // Fixed to UserSearchViewModel
                userId = userId
            )
        }
        composable("$ROUTE_SEARCH_CONSUMER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SearchScreenConsumer(
                navController,
                viewModel = viewModel<UserSearchViewModel>(), // Fixed to UserSearchViewModel
                userId = userId
            )
        }
        composable(ROUTE_NOTIFICATION) {
            NotificationScreen(navController)
        }
        composable(ROUTE_ROLES) {
            RoleSelectionScreen(navController)
        }
        composable("$ROUTE_PROFILE_SUPPLIER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierProfileScreen(navController, authViewModel = authViewModel, userId = userId)
        }
        composable("$ROUTE_PROFILE_CONSUMER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ConsumerProfileScreen(navController, authViewModel = authViewModel, userId = userId)
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(navController, darkModeViewModel = darkModeViewModel)
        }
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }
        composable("$ROUTE_COMMODITY_LIST/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierCommodityScreen(
                navController,
                userId = userId,
                imgurViewModel = imgurViewModel
            )
        }
        composable("$ROUTE_CONSUMER_VIEW/{userId}/{searcherRole}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val searcherRole = backStackEntry.arguments?.getString("searcherRole") ?: "consumer"
            ConsumerView(
                navController = navController,
                userId = userId,
                searcherRole = searcherRole,
                authViewModel = authViewModel
            )
        }
        composable("$ROUTE_SUPPLIER_VIEW/{userId}?searcherRole={searcherRole}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val searcherRole = backStackEntry.arguments?.getString("searcherRole") ?: "supplier"
            SupplierView(
                navController = navController,
                userId = userId,
                searcherRole = searcherRole,
                authViewModel = authViewModel
            )
        }
        composable("commodity_view/{supplierId}/{searcherRole}") { backStackEntry ->
            val supplierId = backStackEntry.arguments?.getString("supplierId") ?: ""
            val searcherRole = backStackEntry.arguments?.getString("searcherRole") ?: "consumer"
            CommodityView(navController, supplierId = supplierId, searcherRole = searcherRole)
        }
        composable(ROUTE_CHAT_SCREEN) {
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(authViewModel)
            )
            ChatScreen(
                navController = navController,
                chatViewModel = chatViewModel,
                authViewModel = authViewModel
            )
        }
        composable(
            route = "$ROUTE_CHAT_SCREEN_2/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(authViewModel)
            )
            ChatScreen2(
                navController = navController,
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                userId = userId
            )
        }
        composable("$ROUTE_SUPPLIER_ALL_REQUESTS/{supplierId}") { backStackEntry ->
            val supplierId = backStackEntry.arguments?.getString("supplierId") ?: ""
            SupplierAllRequestsScreen(supplierId = supplierId)
        }
    }
}