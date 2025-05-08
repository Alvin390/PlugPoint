import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.DarkModeViewModel
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel
import com.PlugPoint.plugpoint.navigation.ROUTE_COMMODITY_LIST
import com.PlugPoint.plugpoint.navigation.ROUTE_LOGIN
import com.PlugPoint.plugpoint.navigation.ROUTE_NOTIFICATION
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_REGISTRATION_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_REGISTRATION_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_ROLES
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_SETTINGS
import com.PlugPoint.plugpoint.navigation.ROUTE_SPLASH
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier.SupplierCommodityScreen
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerProfileScreen
import com.PlugPoint.plugpoint.ui.theme.screens.login.LoginScreen
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierProfileScreen
import com.PlugPoint.plugpoint.ui.theme.screens.notifications_screen.NotificationScreen
import com.PlugPoint.plugpoint.ui.theme.screens.registration_consumer.RegistrationConsumerScreen
import com.PlugPoint.plugpoint.ui.theme.screens.registration_supplier.RegistrationSupplierScreen
import com.PlugPoint.plugpoint.ui.theme.screens.role_screen.RoleSelectionScreen
import com.PlugPoint.plugpoint.ui.theme.screens.search_screen_consumer.SearchConsumerScreen
import com.PlugPoint.plugpoint.ui.theme.screens.settings_screen.SettingsScreen
import com.PlugPoint.plugpoint.ui.theme.screens.splashscreen.SplashScreen
import com.PlugPoint.plugpoint.utilis.ImgurViewModelFactory

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_LOGIN,
    imgurViewModel: ImgurViewModel,
    darkModeViewModel: DarkModeViewModel,
    imgurAPI: ImgurAPI// Add this parameter
) {
    val imgurViewModel: ImgurViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ImgurViewModelFactory(imgurAPI)
    )
    NavHost(navController = navController,modifier = modifier, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            LoginScreen(navController, viewModel = AuthViewModel(imgurViewModel))
        }
        composable(ROUTE_REGISTRATION_SUPPLIER) {
            RegistrationSupplierScreen(navController, viewModel = AuthViewModel(imgurViewModel))
        }
        composable(ROUTE_REGISTRATION_CONSUMER) {
            RegistrationConsumerScreen(navController, viewModel = AuthViewModel(imgurViewModel))
        }
        composable("$ROUTE_SEARCH_SUPPLIER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SearchScreenSupplier(navController, viewModel = SearchSupplierAuthViewModel(), userId = userId)
        }
        composable("$ROUTE_SEARCH_CONSUMER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SearchConsumerScreen(navController, viewModel = SearchSupplierAuthViewModel(), userId = userId)
        }
        composable(ROUTE_NOTIFICATION) {
            NotificationScreen(navController)
        }
        composable(ROUTE_ROLES) {
            RoleSelectionScreen(navController)
        }
        composable("$ROUTE_PROFILE_SUPPLIER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierProfileScreen(navController, authViewModel = AuthViewModel(imgurViewModel), userId = userId)
        }
        composable("$ROUTE_PROFILE_CONSUMER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ConsumerProfileScreen(navController, authViewModel = AuthViewModel(imgurViewModel), userId = userId)
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(navController, darkModeViewModel = darkModeViewModel)
        }
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }
        composable("$ROUTE_COMMODITY_LIST/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierCommodityScreen(navController, viewModel = CommodityViewModel(imgurViewModel), userId = userId)
        }
    }
}