package com.nyumbahub.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nyumbahub.feature.auth.ui.LoginScreen
import com.nyumbahub.feature.auth.ui.RegisterScreen
import com.nyumbahub.feature.chat.ui.ChatScreen
import com.nyumbahub.feature.listings.ui.ApartmentsCategoryScreen
import com.nyumbahub.feature.listings.ui.HousesCategoryScreen
import com.nyumbahub.feature.listings.ui.ListingDetailScreen
import com.nyumbahub.feature.listings.ui.ListingsScreen
import com.nyumbahub.feature.listings.ui.OffPlanScreen
import com.nyumbahub.feature.listings.ui.RoomsScreen
import com.nyumbahub.feature.listings.ui.ValuationScreen
import com.nyumbahub.feature.motors.ui.MotorDetailScreen
import com.nyumbahub.feature.motors.ui.MotorsCategoryScreen
import com.nyumbahub.feature.motors.ui.MotorsScreen
import com.nyumbahub.feature.post.ui.PostListingScreen
import com.nyumbahub.feature.profile.ui.AgentScreen
import com.nyumbahub.feature.profile.ui.EditProfileScreen
import com.nyumbahub.feature.profile.ui.MenuScreen
import com.nyumbahub.feature.profile.ui.MyListingsScreen
import com.nyumbahub.feature.profile.ui.NotificationPreferencesScreen
import com.nyumbahub.feature.profile.ui.SavedListingsScreen
import com.nyumbahub.feature.profile.ui.SecurityScreen
import com.nyumbahub.feature.profile.ui.TermsScreen
import com.nyumbahub.feature.search.ui.MapScreen
import com.nyumbahub.feature.search.ui.PropertyFilterScreen
import com.nyumbahub.feature.search.ui.SearchScreen
import com.nyumbahub.feature.subscription.ui.SubscriptionScreen
import java.io.File

object Routes {
    const val SPLASH        = "splash"
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val HOME          = "home"
    const val FAVORITES     = "favorites"
    const val POST          = "post"
    const val CHATS         = "chats"
    const val MENU          = "menu"
    const val DETAIL        = "listing/{listingId}"
    const val CHAT_DETAIL   = "chat/{inquiryId}"
    const val PLANS         = "subscription"
    const val SEARCH        = "search"
    const val MAP           = "map"
    const val MY_LISTINGS   = "my_listings"
    const val SAVED         = "saved"
    const val SECURITY      = "security"
    const val TERMS         = "terms"
    const val PRIVACY       = "privacy"
    const val MOTORS        = "motors"
    const val MOTORS_LIST   = "motors_list/{category}"
    const val MOTOR_DETAIL  = "motor_detail/{motorId}"
    const val FILTER        = "filter"
    const val ROOMS         = "rooms"
    const val OFF_PLAN      = "off_plan"
    const val VALUATION     = "valuation"
    const val AGENTS        = "agents"
    const val EDIT_PROFILE  = "edit_profile"
    const val NOTIFICATIONS = "notifications_screen"
    const val HOUSES        = "houses_category"
    const val APARTMENTS    = "apartments_category"
    const val NOTIF_PREFS   = "notif_prefs_screen"
    const val ADMIN         = "admin_panel"
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME,      "Home",        Icons.Default.Home),
    BottomNavItem(Routes.FAVORITES, "Favorites",   Icons.Default.FavoriteBorder),
    BottomNavItem(Routes.POST,      "Place an Ad", Icons.Default.AddCircle),
    BottomNavItem(Routes.CHATS,     "Chats",       Icons.Default.ChatBubbleOutline),
    BottomNavItem(Routes.MENU,      "Menu",        Icons.Default.Menu)
)

val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun NyumbaHubNavGraph() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val backstackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backstackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavRoutes

    fun shareApp() {
        try {
            val apkFile = File(context.applicationInfo.sourceDir)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", apkFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Elmaz App via"))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out Elmaz!")
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(containerColor = Color.White, tonalElevation = androidx.compose.ui.unit.Dp(8f)) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (item.route == Routes.POST) { nav.navigate(item.route) }
                                else { nav.navigate(item.route) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFE53935), selectedTextColor = Color(0xFFE53935),
                                unselectedIconColor = Color(0xFF666666), unselectedTextColor = Color(0xFF666666),
                                indicatorColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Routes.SPLASH, modifier = Modifier.padding(padding)) {
            composable(Routes.SPLASH) { SplashScreen(onFinished = { nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } }) }
            composable(Routes.HOME) {
                ListingsScreen(
                    onListingClick = { id -> nav.navigate("listing/$id") },
                    onLoginRequired = { nav.navigate(Routes.LOGIN) },
                    onMotorsClick = { nav.navigate(Routes.MOTORS) },
                    onRoomsClick = { nav.navigate(Routes.ROOMS) },
                    onOffPlanClick = { nav.navigate(Routes.OFF_PLAN) },
                    onFilterClick = { nav.navigate(Routes.FILTER) },
                    onNotificationsClick = { nav.navigate(Routes.NOTIFICATIONS) },
                    onHousesClick = { nav.navigate(Routes.HOUSES) },
                    onApartmentsClick = { nav.navigate(Routes.APARTMENTS) },
                    onSearchHistoryClick = { nav.navigate(Routes.FILTER) }
                )
            }
            composable(Routes.FAVORITES) { FavoritesScreen(onListingClick = { id -> nav.navigate("listing/$id") }, onLoginRequired = { nav.navigate(Routes.LOGIN) }) }
            composable(Routes.POST) {
                PostListingScreen(onBack = { nav.popBackStack() }, onSuccess = { nav.navigate(Routes.HOME) { popUpTo(Routes.POST) { inclusive = true } } }, onLoginRequired = { nav.navigate(Routes.LOGIN) })
            }
            composable(Routes.CHATS) {
                ChatsScreen(onChatClick = { id -> nav.navigate("chat/$id") }, onLoginRequired = { nav.navigate(Routes.LOGIN) }, onExplore = { nav.navigate(Routes.HOME) }, onPostAd = { nav.navigate(Routes.POST) })
            }
            composable(Routes.MENU) {
                MenuScreen(
                    onAdminClick = { nav.navigate(Routes.ADMIN) },
                    onLogin = { nav.navigate(Routes.LOGIN) },
                    onProfile = { nav.navigate(Routes.PLANS) },
                    onSubscription = { nav.navigate(Routes.PLANS) },
                    onMyListings = { nav.navigate(Routes.MY_LISTINGS) },
                    onSaved = { nav.navigate(Routes.SAVED) },
                    onSecurity = { nav.navigate(Routes.SECURITY) },
                    onNotifications = { nav.navigate(Routes.NOTIF_PREFS) },
                    onContactUs = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/250798028184"))) },
                    onTerms = { nav.navigate(Routes.TERMS) },
                    onPrivacy = { nav.navigate(Routes.PRIVACY) },
                    onSignOut = { nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } },
                    onShareApp = { shareApp() },
                    onValuation = { nav.navigate(Routes.VALUATION) },
                    onAgents = { nav.navigate(Routes.AGENTS) },
                    onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) }
                )
            }
            composable(Routes.LOGIN) { LoginScreen(onNavigateToHome = { nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }, onNavigateToRegister = { nav.navigate(Routes.REGISTER) }) }
            composable(Routes.REGISTER) { RegisterScreen(onNavigateToHome = { nav.navigate(Routes.HOME) { popUpTo(Routes.REGISTER) { inclusive = true } } }, onNavigateToLogin = { nav.navigate(Routes.LOGIN) }) }
            composable(Routes.DETAIL) { backStack ->
                val listingId = backStack.arguments?.getString("listingId") ?: return@composable
                ListingDetailScreen(listingId = listingId, onBack = { nav.popBackStack() }, onInquiry = { id ->
                    nav.navigate("chat/$id")
                    nav.navigate(Routes.CHATS) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(Routes.CHAT_DETAIL) { backStack ->
                val inquiryId = backStack.arguments?.getString("inquiryId") ?: return@composable
                ChatScreen(inquiryId = inquiryId, currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "", onBack = { nav.popBackStack() })
            }
            composable(Routes.PLANS) { SubscriptionScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.FILTER) { PropertyFilterScreen(onBack = { nav.popBackStack() }, onApply = { nav.navigate(Routes.SEARCH) }) }
            composable(Routes.MAP) { MapScreen(onBack = { nav.popBackStack() }, onListingClick = { id -> nav.navigate("listing/$id") }) }
            composable(Routes.SECURITY) { SecurityScreen(email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "", onBack = { nav.popBackStack() }) }
            composable(Routes.ADMIN) { AdminPanelScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.NOTIF_PREFS) { NotificationPreferencesScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.TERMS) { TermsScreen(isPrivacyPolicy = false, onBack = { nav.popBackStack() }) }
            composable(Routes.PRIVACY) { TermsScreen(isPrivacyPolicy = true, onBack = { nav.popBackStack() }) }
            composable(Routes.SEARCH) { SearchScreen(onListingClick = { id -> nav.navigate("listing/$id") }, onMapClick = { nav.navigate(Routes.MAP) }) }
            composable(Routes.MY_LISTINGS) { MyListingsScreen(onBack = { nav.popBackStack() }, onListingClick = { id -> nav.navigate("listing/$id") }, onPostNew = { nav.navigate(Routes.POST) }) }
            composable(Routes.SAVED) { SavedListingsScreen(onBack = { nav.popBackStack() }, onListingClick = { id -> nav.navigate("listing/$id") }) }
            composable(Routes.MOTORS_LIST) { backStack ->
                val cat = backStack.arguments?.getString("category") ?: ""
                MotorsScreen(onBack = { nav.popBackStack() }, onMotorClick = { id -> nav.navigate("motor_detail/$id") }, onPostMotor = { nav.navigate(Routes.POST) })
            }
            composable(Routes.MOTOR_DETAIL) { backStack ->
                val motorId = backStack.arguments?.getString("motorId") ?: return@composable
                MotorDetailScreen(motorId = motorId, onBack = { nav.popBackStack() }, onInquiry = { id ->
                    nav.navigate("chat/$id")
                    nav.navigate(Routes.CHATS) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(Routes.OFF_PLAN) { OffPlanScreen(onBack = { nav.popBackStack() }, onProjectClick = { nav.navigate(Routes.FILTER) }) }
            composable(Routes.VALUATION) { ValuationScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.EDIT_PROFILE) { EditProfileScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.AGENTS) { AgentScreen(onBack = { nav.popBackStack() }, onAgentClick = { }) }
            composable(Routes.HOUSES) { HousesCategoryScreen(onBack = { nav.popBackStack() }, onCategoryClick = { nav.navigate(Routes.FILTER) }, onPostClick = { nav.navigate(Routes.POST) }) }
            composable(Routes.APARTMENTS) { ApartmentsCategoryScreen(onBack = { nav.popBackStack() }, onCategoryClick = { nav.navigate(Routes.FILTER) }, onPostClick = { nav.navigate(Routes.POST) }) }
            composable(Routes.ROOMS) { RoomsScreen(onBack = { nav.popBackStack() }, onRoomClick = { id -> nav.navigate("listing/$id") }, onPostRoom = { nav.navigate(Routes.POST) }) }
            composable(Routes.MOTORS) { MotorsCategoryScreen(onBack = { nav.popBackStack() }, onCategoryClick = { nav.navigate(Routes.MOTORS_LIST.replace("{category}", it)) }, onSellClick = { nav.navigate(Routes.POST) }) }
        }
    }
}
