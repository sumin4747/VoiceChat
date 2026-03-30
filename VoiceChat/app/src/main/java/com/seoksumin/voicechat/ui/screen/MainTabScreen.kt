package com.seoksumin.voicechat.ui.screen

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import androidx.compose.foundation.layout.padding

import com.seoksumin.voicechat.ui.navigation.Routes

@Composable
fun MainTabScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            // ✅ 업로드 화면에서는 하단 탭 숨기고 싶으면 여기 조건 걸면 됨
            val route = currentRoute?.destination?.route
            if (route != Routes.UploadVoice) {
                BottomTabs(
                    currentRoute = route,
                    onClickRecords = { navigateSingleTop(navController, Routes.TabRecords) },
                    onClickAnniversary = { navigateSingleTop(navController, Routes.TabAnniversary) },
                    onClickProfile = { navigateSingleTop(navController, Routes.TabProfile) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TabRecords,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.TabRecords) {
                ChatScreen(
                    onGoUpload = { navController.navigate(Routes.UploadVoice) },
                    onClickRecord = {voiced ->

                    }
                )
            }
            composable(Routes.TabAnniversary) {
                AnniversaryScreen()
            }
            composable(Routes.TabProfile) {
                ProfileScreen(
                    onLogout = onLogout
                )
            }

            // ✅ 업로드 화면
            composable(Routes.UploadVoice) {
                UploadVoiceScreen(
                    onBack = { navController.popBackStack() },
                    onUploadSuccess = { navController.popBackStack()}
                )
            }
        }
    }
}

@Composable
private fun BottomTabs(
    currentRoute: String?,
    onClickRecords: () -> Unit,
    onClickAnniversary: () -> Unit,
    onClickProfile: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.TabRecords,
            onClick = onClickRecords,
            icon = { Text("💬") },
            label = { Text("나의 기록") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.TabAnniversary,
            onClick = onClickAnniversary,
            icon = { Text("📅") },
            label = { Text("기념일 관리") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.TabProfile,
            onClick = onClickProfile,
            icon = { Text("👤") },
            label = { Text("내 정보") }
        )
    }
}

private fun navigateSingleTop(navController: NavHostController, route: String) {
    navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
    }
}