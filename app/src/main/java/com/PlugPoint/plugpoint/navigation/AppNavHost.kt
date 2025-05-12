import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.DarkModeViewModel
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel
import com.PlugPoint.plugpoint.navigation.ROUTE_COMMODITY_LIST
import com.PlugPoint.plugpoint.navigation.ROUTE_CONSUMER_VIEW
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
import com.PlugPoint.plugpoint.navigation.ROUTE_SUPPLIER_VIEW
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier.SupplierCommodityScreen
import com.PlugPoint.plugpoint.ui.theme.screens.consumer_view.ConsumerView
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
import com.PlugPoint.plugpoint.ui.theme.screens.supplier_view.SupplierView
import com.PlugPoint.plugpoint.utilis.ImgurViewModelFactory
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_view.CommodityView
import com.PlugPoint.plugpoint.ui.theme.screens.chat_screen.ChatScreen2
import com.PlugPoint.plugpoint.data.ChatViewModel
import com.PlugPoint.plugpoint.navigation.ROUTE_CHAT_SCREEN
import com.PlugPoint.plugpoint.navigation.ROUTE_CHAT_SCREEN_2

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_LOGIN,
    imgurViewModel: ImgurViewModel,
    darkModeViewModel: DarkModeViewModel,
    imgurAPI: ImgurAPI,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val chatViewModel = ChatViewModel()

    NavHost(navController = navController, modifier = modifier, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(ROUTE_REGISTRATION_SUPPLIER) {
            RegistrationSupplierScreen(navController, viewModel = AuthViewModel(imgurViewModel, context))
        }
        composable(ROUTE_REGISTRATION_CONSUMER) {
            RegistrationConsumerScreen(navController, viewModel = AuthViewModel(imgurViewModel, context))
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
            SupplierProfileScreen(navController, authViewModel = AuthViewModel(imgurViewModel, context), userId = userId)
        }
        composable("$ROUTE_PROFILE_CONSUMER/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ConsumerProfileScreen(navController, authViewModel = AuthViewModel(imgurViewModel, context), userId = userId)
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(navController, darkModeViewModel = darkModeViewModel)
        }
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }
        composable("$ROUTE_COMMODITY_LIST/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SupplierCommodityScreen(navController, userId = userId, imgurViewModel = imgurViewModel)
        }
        composable("$ROUTE_CONSUMER_VIEW/{userId}/{searcherRole}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val searcherRole = backStackEntry.arguments?.getString("searcherRole") ?: "consumer"
            ConsumerView(navController, userId = userId, searcherRole = searcherRole)
        }
        composable("$ROUTE_SUPPLIER_VIEW/{userId}/{searcherRole}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val searcherRole = backStackEntry.arguments?.getString("searcherRole") ?: "supplier"
            SupplierView(navController, userId = userId, searcherRole = searcherRole)
        }
        composable("commodity_view/{supplierId}/{searcherRole}") { backStackEntry ->
            val supplierId = backStackEntry.arguments?.getString("supplierId") ?: ""
            val searcherRole = backStackEntry.arguments?.getString("searcherRole") ?: "consumer"
            CommodityView(navController, supplierId = supplierId, searcherRole = searcherRole)
        }
        composable(ROUTE_CHAT_SCREEN) {
            ChatScreen(navController = navController, chatViewModel = chatViewModel)
        }
        composable("$ROUTE_CHAT_SCREEN_2/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val currentUserId = chatViewModel.getCurrentUserId()
            if (currentUserId == null) {
                navController.navigate(ROUTE_LOGIN) // Redirect to login if not authenticated
            } else {
                ChatScreen2(navController = navController, chatViewModel = chatViewModel, userId = userId)
            }
        }
    }
}
