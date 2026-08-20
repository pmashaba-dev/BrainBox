package com.example.brainbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brainbox.navigation.AppRoutes
import com.example.brainbox.ui.screens.*
import com.example.brainbox.ui.theme.BrainBoxTheme
import com.example.brainbox.ui.screens.EditProfileScreen
import com.example.brainbox.ui.screens.ChangePasswordScreen

// NOTE: You will need to create placeholder Composable functions for these new screens
// in your com.example.brainbox.ui.screens package (e.g., AdminDashboardScreen.kt)
// for this code to compile successfully.
// import com.example.brainbox.ui.screens.AdminDashboardScreen
// import com.example.brainbox.ui.screens.QuizManagementScreen
// import com.example.brainbox.ui.screens.PdfManagementScreen

// The main activity of your application.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrainBoxTheme {
                // The root of your app's UI, using a Navigation Controller to manage screen transitions.
                AppNavigation()
            }
        }
    }
}

// The main navigation composable.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Splash.route
    ) {
        // Defines the route for the splash screen.
        composable(AppRoutes.Splash.route) {
            SplashScreen(navController = navController)
        }
        // Defines the route for the new welcome screen.
        composable(AppRoutes.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        // Defines the route for the login screen.
        composable(AppRoutes.Login.route) {
            LoginScreen(navController = navController)
        }
        // Defines the route for the signup screen.
        composable(AppRoutes.Signup.route) {
            SignupScreen(navController = navController)
        }
        // Defines the route for the forgot password screen.
        composable(AppRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        // Defines the new route for the profile creation screen.
        composable(AppRoutes.ProfileCreation.route) {
            ProfileCreationScreen(navController = navController)
        }
        // Defines the new route for the Dashboard screen.
        composable(AppRoutes.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        // Defines the new route for the Settings screen.
        composable(AppRoutes.Settings.route) {
            SettingsScreen(navController = navController)
        }
        // Defines the new route for the Edit Profile screen.
        composable(AppRoutes.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        // Defines the new route for the Change Password screen.
        composable(AppRoutes.ChangePassword.route) {
            ChangePasswordScreen(navController = navController)
        }

        // 👇 UPDATED ADMIN ROUTES ADDED HERE 👇
        // 👇 NEW: Defines the route for the About screen. 👇
        composable(AppRoutes.About.route) {
            AboutScreen(navController = navController)
        }
        // Defines the route for the main Admin Dashboard.
        composable(AppRoutes.AdminDashboard.route) {
            // Placeholder: Replace with the actual AdminDashboardScreen composable
            AdminDashboardScreen(navController = navController)
        }

        // 1. Quiz Management Listing Screen
        composable(AppRoutes.QuizManagement.route) {
            // This now uses the actual QuizManagementScreen from the previous step
            QuizManagementScreen(navController = navController)
        }

        // 2. Quiz Creation/Editing Screen
        composable(
            route = AppRoutes.CreateEditQuiz.route,
            arguments = listOf(navArgument("topicTitle") {
                type = NavType.StringType
                defaultValue = "null" // The default value set in AppRoutes.kt for optional argument
                nullable = true
            })
        ) { backStackEntry ->
            // Use the helper function from AppRoutes to extract and decode the title
            val topicTitle = AppRoutes.CreateEditQuiz.getTopicTitle(
                backStackEntry.arguments?.getString("topicTitle")
            )

            // Defines the new route for Create/Edit Quiz screen
            CreateEditQuizScreen(
                navController = navController,
                initialTopicTitle = topicTitle // Parameter name updated for consistency
            )
        }

        composable(AppRoutes.UserManagement.route) {
            // The screen you provided
            UserManagementScreen(navController = navController)
        }

        // 👆 END UPDATED ADMIN ROUTES 👆

        // Defines the new route for the Mathematics Topics screen.
        composable(AppRoutes.MathematicsTopics.route) {
            MathematicsTopicsScreen(navController = navController)
        }
        // Defines the new route for the Physical Sciences Topics screen.
        composable(AppRoutes.PhysicalSciencesTopics.route) {
            PhysicalSciencesTopicsScreen(navController = navController)
        }
        // Defines the new route for the Mathematics Study Hub screen.
        composable(AppRoutes.MathsStudyHub.route) {
            MathsStudyHubScreen(navController = navController)
        }
        // Defines the new route for the Science Study Hub screen.
        composable(AppRoutes.ScienceStudyHub.route) {
            ScienceStudyHubScreen(navController = navController)
        }
        // Defines the new route for the Game Modes screen.
        composable(
            route = AppRoutes.GameModes.route,
            arguments = listOf(
                navArgument("topicName") { type = NavType.StringType },
                navArgument("parentRoute") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicName = backStackEntry.arguments?.getString("topicName") ?: "Unknown"
            val parentRoute = backStackEntry.arguments?.getString("parentRoute") ?: "dashboard_screen" // A fallback route
            GameModesScreen(navController = navController, topicName = topicName, parentRoute = parentRoute)
        }
        // Defines the new route for the Quiz Game screen.
        composable(
            route = AppRoutes.QuizGame.route,
            arguments = listOf(
                navArgument("topicName") { type = NavType.StringType },
                navArgument("parentRoute") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicTitle = backStackEntry.arguments?.getString("topicName") ?: "Unknown"
            val parentRoute = backStackEntry.arguments?.getString("parentRoute") ?: AppRoutes.MathematicsTopics.route

            QuizGameScreen(
                topicTitle = topicTitle,
                // Pass the onGoBack lambda to navigate back to the correct parent screen
                onGoBack = {
                    navController.popBackStack(
                        route = parentRoute,
                        inclusive = false
                    )
                }
            )
        }

        composable(
            route = AppRoutes.PDF_LIST.route,
            arguments = listOf(
                navArgument("subject") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            PdfListScreen(
                navController = navController,
                subject = backStackEntry.arguments?.getString("subject") ?: "Unknown",
                category = backStackEntry.arguments?.getString("category") ?: "Unknown"
            )
        }
        // Defines the new route for the PDF Viewer screen
        composable(
            route = AppRoutes.PDF_VIEWER.route,
            arguments = listOf(navArgument("pdfUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val pdfUrl = backStackEntry.arguments?.getString("pdfUrl")
            if (pdfUrl != null) {
                // The URL is encoded in the route, but Compose Navigation automatically decodes it
                PdfViewerScreen(navController = navController, pdfUrl = pdfUrl)
            }
        }

        // Defines the new route for the Leaderboard screen.
        composable(AppRoutes.Leaderboard.route) {
            LeaderboardScreen(
                onGoBack = { navController.popBackStack() }
            )
        }
        // Defines the new route for the Progress screen.
        composable(AppRoutes.Progress.route) {
            ProgressScreen(navController = navController)
        }
    }
}