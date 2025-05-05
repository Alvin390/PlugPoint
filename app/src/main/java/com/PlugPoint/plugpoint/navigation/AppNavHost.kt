package com.PlugPoint.plugpoint.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.CommodityViewModel
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier.SupplierCommodityScreen
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerProfileScreen
import com.PlugPoint.plugpoint.ui.theme.screens.login.LoginScreen
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierProfileScreen
import com.PlugPoint.plugpoint.ui.theme.screens.notifications_screen.NotificationScreen
import com.PlugPoint.plugpoint.ui.theme.screens.registration_consumer.RegistrationConsumerScreen
import com.PlugPoint.plugpoint.ui.theme.screens.registration_supplier.RegistrationSupplierScreen
import com.PlugPoint.plugpoint.ui.theme.screens.role_screen.RoleSelectionScreen
import com.PlugPoint.plugpoint.ui.theme.screens.search_screen_supplier.Search_supply_screen
import com.PlugPoint.plugpoint.ui.theme.screens.search_screen_consumer.SearchConsumerScreen
import com.PlugPoint.plugpoint.ui.theme.screens.settings_screen.SettingsScreen
import com.PlugPoint.plugpoint.ui.theme.screens.splashscreen.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_LOGIN
) {
    NavHost(navController = navController, modifier = modifier, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            LoginScreen(navController, viewModel = AuthViewModel())
        }
        composable(ROUTE_REGISTRATION_SUPPLIER) {
            RegistrationSupplierScreen(navController, viewModel = AuthViewModel())
        }
        composable(ROUTE_REGISTRATION_CONSUMER) {
            RegistrationConsumerScreen(navController, viewModel = AuthViewModel())
        }
        composable("$ROUTE_PROFILE_SUPPLIER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierProfileScreen(navController, viewModel = AuthViewModel(), userId = userId)
        }
        composable("$ROUTE_PROFILE_CONSUMER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ConsumerProfileScreen(navController, viewModel = AuthViewModel(), userId = userId)
        }
        composable(ROUTE_NOTIFICATION) {
            NotificationScreen(navController)
        }
        composable(ROUTE_ROLES) {
            RoleSelectionScreen(navController)
        }
        composable("$ROUTE_SEARCH_SUPPLIER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            Search_supply_screen(navController, userId,viewModel = SearchSupplierAuthViewModel())
        }
        composable(ROUTE_SEARCH_CONSUMER) {
            SearchConsumerScreen(navController)
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(navController)
        }
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }
        composable("$ROUTE_COMMODITY_LIST/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierCommodityScreen(navController, viewModel = CommodityViewModel(), userId = userId)
        }
//        composable  (ROUTE_EDIT_PROFILE_SUPPLIER) {
//            EditProfileSupplierScreen()
//        }
//        composable  (ROUTE_EDIT_PROFILE_CONSUMER) {
//            EditProfileConsumerScreen()
//        }
    }
}