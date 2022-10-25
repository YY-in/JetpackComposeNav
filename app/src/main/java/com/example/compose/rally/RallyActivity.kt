/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.rally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.rally.ui.components.RallyTabRow
import com.example.compose.rally.ui.theme.RallyTheme

/**
 * This Activity recreates part of the Rally Material Study from
 * https://material.io/design/material-studies/rally.html
 */
class RallyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RallyApp()
        }
    }
}

@Composable
fun RallyApp() {
    RallyTheme {
//        不再更新
//        var currentScreen: RallyDestination by remember { mutableStateOf(Overview) }
        /**
         *  NavController 是核心组件。它可跟踪返回堆栈可组合条目、使堆栈向前移动、支持对返回堆栈执行操作，
         *  以及在不同目的地状态之间导航
         */
        val navController = rememberNavController()
        // 返回栈顶
        val currentBackStack by navController.currentBackStackEntryAsState()
        // Fetch your currentDestination
        /**
         * currentBackStack?.destination 会返回 NavDestination.
         * 如需重新正确更新 currentScreen，您需要想方设法将返回值 NavDestination 与 Rally 的三个主要屏幕可组合项之一进行匹配。
         * 您必须确定当前显示的目的地，以便随后将这些信息传递给 RallyTabRow.

         */
        val currentDestination = currentBackStack?.destination

        Scaffold(
            topBar = {
                RallyTabRow(
                    allScreens = rallyTabRowScreens,
                    onTabSelected = { newScreen ->
                        navController.navigateSingleTopTo(newScreen.route) },
                    /**
                     * 如前所述，每个目的地都有一条唯一的路线，因此我们可以使用此 String 路线作为类似的 ID，
                     * 以进行严格的对比并找到唯一匹配。
                     * 如需更新 currentScreen，您需要迭代 rallyTabRowScreens 列表，
                     * 以找到匹配路线，然后返回对应的 RallyDestination。Kotlin 为此提供了一个便捷的 .find() 函数：
                     */
                    currentScreen = rallyTabRowScreens.find{ it.route == currentDestination?.route } ?: Overview
                )
            }
        ) { innerPadding ->
//            其中包含当前屏幕的内容，用于手动切换屏幕
//            Box(Modifier.padding(innerPadding)) {
//                currentScreen.screen()
//            }
            /**
             * NavHost 充当容器，负责显示导航图的当前目的地
             * 当您在可组合项之间进行导航时，NavHost 的内容会自动进行重组。
             * 此外，它还会将 NavController 与导航图 (NavGraph) 相关联，后者用于标出能够在其间进行导航的可组合目的地。
             * 它实际上是一系列可提取的目的地。
             */
            NavHost(
                navController = navController,
                startDestination = Overview.route,
                modifier = Modifier.padding(innerPadding)
            ){
                /**
                 * 最后一个形参 builder: NavGraphBuilder.() -> Unit 负责定义和构建导航图。
                 * 该形参使用的是 Navigation Kotlin DSL 中的 lambda 语法，因此可作为函数正文内部的尾随 lambda 传递并从括号中取出
                 */
                // builder parameter will be defined here as the graph
                /**
                 * builder 形参要求使用函数，因此 Navigation Compose 提供了 NavGraphBuilder.composable 扩展函数，
                 * 以便轻松将各个可组合目的地添加到导航图中，并定义必要的导航信息。
                 *
                 * 第一个目的地是 Overview，因此您需要通过 composable 扩展函数添加它，并为其设置唯一字符串 route。
                 * 此操作只会将目的地添加到导航图中，因此您还需要定义导航到此目的地时要显示的实际界面
                 */
                composable(route = Overview.route) {
                    Overview.screen()
                }
                composable(route = Accounts.route) {
                    Accounts.screen()
                }
                composable(route = Bills.route) {
                    Bills.screen()
                }
            }
        }
    }
}

/**
 * 连续重按同一标签页会启动同一目的地的多个副本,如何启动目的地的单个副本？
 * 确保返回堆栈顶部最多只有给定目的地的一个副本，Compose Navigation API 提供了一个 launchSingleTop 标志，
 * 您可以将其传递给 navController.navigate()
 */
fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ){
            saveState = true
        }
        launchSingleTop = true
        restoreState =true
    }
