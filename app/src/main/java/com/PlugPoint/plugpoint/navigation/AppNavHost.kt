package com.PlugPoint.plugpoint.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            LoginScreen(navController)
        }
        composable(ROUTE_REGISTRATION_SUPPLIER) {
            RegistrationSupplierScreen(navController)
        }
        composable(ROUTE_REGISTRATION_CONSUMER) {
            RegistrationConsumerScreen(navController)
        }
        composable(ROUTE_PROFILE_SUPPLIER) {
            SupplierProfileScreen(navController)
        }
        composable(ROUTE_PROFILE_CONSUMER) {
            ConsumerProfileScreen(navController)
        }
        composable(ROUTE_NOTIFICATION) {
            NotificationScreen(navController)
        }
        composable(ROUTE_ROLES) {
            RoleSelectionScreen(navController)
        }
        composable(ROUTE_SEARCH_SUPPLIER) {
            Search_supply_screen(navController)
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
        composable  (ROUTE_COMMODITY_LIST) {
            SupplierCommodityScreen(navController)
        }
//        composable  (ROUTE_EDIT_PROFILE_SUPPLIER) {
//            EditProfileSupplierScreen()
//        }
//        composable  (ROUTE_EDIT_PROFILE_CONSUMER) {
//            EditProfileConsumerScreen()
//        }
    }
}