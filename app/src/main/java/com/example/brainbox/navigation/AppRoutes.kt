package com.example.brainbox.navigation

import android.net.Uri

/**
 * Defines the routes for navigation within the application.
 */
sealed class AppRoutes(val route: String) {
    object Splash : AppRoutes("splash_screen")
    object Welcome : AppRoutes("welcome_screen")
    object Login : AppRoutes("login_screen")
    object Signup : AppRoutes("signup_screen")
    object ForgotPassword : AppRoutes("forgot_password_screen")
    object ProfileCreation : AppRoutes("profile_creation_screen")
    object Dashboard : AppRoutes("dashboard_screen")
    object Settings : AppRoutes("settings_screen")
    object EditProfile : AppRoutes("edit_profile_screen")
    object ChangePassword : AppRoutes("change_password_screen")

    // NEW ADMIN ROUTES
    object AdminDashboard : AppRoutes("admin_dashboard_screen")
    object QuizManagement : AppRoutes("admin_quiz_management_screen")


    // NEW USER MANAGEMENT ROUTE
    object UserManagement : AppRoutes("user_management_screen")

    // NEW QUIZ CREATION/EDIT ROUTE
    // The route includes an optional argument for topicTitle (null if creating new)
    object CreateEditQuiz : AppRoutes("create_edit_quiz_screen?topicTitle={topicTitle}") {
        fun createRoute(topicTitle: String? = null): String {
            // URI-encode the topic title or use "null" for creation
            val encodedTopic = topicTitle?.let { Uri.encode(it) } ?: "null"
            return "create_edit_quiz_screen?topicTitle=$encodedTopic"
        }

        /**
         * Helper to decode the topic title from the route argument.
         * Compose will pass "null" as a string if the argument wasn't provided.
         */
        fun getTopicTitle(encodedTitle: String?): String? {
            // Check if the string is exactly "null" (which is what we encode for a missing title)
            return encodedTitle?.takeIf { it != "null" }?.let { Uri.decode(it) }
        }
    }
    // END NEW ADMIN ROUTES

    object MathematicsTopics : AppRoutes("mathematics_topics_screen")
    object PhysicalSciencesTopics : AppRoutes("physical_sciences_topics_screen")
    object MathsStudyHub : AppRoutes("maths_study_hub_screen")
    object ScienceStudyHub : AppRoutes("science_study_hub_screen")
    object Leaderboard : AppRoutes("leaderboard_screen")
    object Progress : AppRoutes("progress_screen")

    object About : AppRoutes("about_screen")
    object GameModes : AppRoutes("game_modes_screen/{topicName}/{parentRoute}") {
        fun createRoute(topicName: String, parentRoute: String): String {
            return "game_modes_screen/$topicName/$parentRoute"
        }
    }
    object QuizGame : AppRoutes("quiz_game_screen/{topicName}/{parentRoute}") {
        fun createRoute(topicName: String, parentRoute: String): String {
            return "quiz_game_screen/$topicName/$parentRoute"
        }
    }
    object PDF_LIST : AppRoutes("pdfList/{subject}/{category}") {
        fun createRoute(subject: String, category: String): String {
            return "pdfList/$subject/$category"
        }
    }
    // New route for the in-app PDF viewer
    object PDF_VIEWER : AppRoutes("pdfViewer/{pdfUrl}") {
        fun createRoute(pdfUrl: String): String {
            // Encode the URL to be safely passed as a navigation argument
            val encodedUrl = Uri.encode(pdfUrl)
            return "pdfViewer/$encodedUrl"
        }
    }
}