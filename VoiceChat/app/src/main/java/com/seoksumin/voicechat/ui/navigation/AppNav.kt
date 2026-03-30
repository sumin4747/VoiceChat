package com.seoksumin.voicechat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seoksumin.voicechat.ui.screen.SplashScreen
import com.seoksumin.voicechat.ui.screen.OnboardingScreen
import com.seoksumin.voicechat.ui.screen.LoginScreen
import com.seoksumin.voicechat.ui.screen.AuthChoiceScreen
import com.seoksumin.voicechat.ui.screen.RegisterScreen
import androidx.compose.ui.platform.LocalContext
import com.seoksumin.voicechat.data.prefs.AppPrefs
import com.seoksumin.voicechat.ui.screen.MainTabScreen



@Composable
fun AppNav() {
    // 1) navController = "화면 이동을 담당하는 리모컨"
    val navController = rememberNavController()
    val context = LocalContext.current


    // 2) NavHost = "지도(화면 목록)를 담는 상자"
    NavHost(
        navController = navController,
        startDestination = Routes.Splash // 앱 시작 화면
    ) {
        // 3) composable(...) = "이 이름의 화면을 등록"
        composable(Routes.Splash) {
            SplashScreen(
                onFinished = {
                    val next = when {
                        AppPrefs.isLoggedIn(context) -> Routes.MainTabs
                        AppPrefs.isOnboardingDone(context) -> Routes.AuthChoice
                        else -> Routes.Onboarding
                    }

                    navController.navigate(next) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onStartClick = {
                    AppPrefs.setOnboardingDone(context, true)
                    navController.navigate(Routes.AuthChoice) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.AuthChoice) {
            AuthChoiceScreen(
                onGoRegister = {
                    navController.navigate(Routes.Register) {
                        popUpTo(Routes.AuthChoice) { inclusive = true }
                    }
                },
                onGoLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.AuthChoice) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    // 지금은 회원가입 성공 후 로그인으로 보내자 (정석 흐름)
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                },
                onGoLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    AppPrefs.setLoggedIn(context, true) // ✅ 로그인 상태 저장
                    navController.navigate(Routes.MainTabs) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MainTabs) {
            MainTabScreen(
                onLogout = {
                    AppPrefs.logout(context)
                    navController.navigate(Routes.AuthChoice){
                        popUpTo(Routes.MainTabs) {inclusive = true}
                        launchSingleTop = true

                    }

                }
            )
        }




    }

}

