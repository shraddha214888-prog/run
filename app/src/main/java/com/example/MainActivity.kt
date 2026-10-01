package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EmergencyViewModel
import com.example.ui.screens.AmbulancePilotScreen
import com.example.ui.screens.CivilianAlertScreen
import com.example.ui.screens.HospitalDirectoryScreen
import com.example.ui.screens.MissionHistoryScreen
import com.example.ui.screens.TrafficCommandScreen
import com.example.ui.theme.DarkHighwayBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TrafficGreen

class MainActivity : ComponentActivity() {

    private val viewModel: EmergencyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val selectedTab by viewModel.selectedTab.collectAsState()

                // Request Real Location Permissions
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (isGranted) {
                        viewModel.requestGpsActivation()
                    }
                }

                LaunchedEffect(Unit) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                // Handle system back navigation to return to the Ambulance screen if not on it
                BackHandler(enabled = selectedTab != "AMBULANCE") {
                    viewModel.selectTab("AMBULANCE")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(EmergencyRedBright)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "૧૦૮ રક્ષક - ઇમરજન્સી લાઈફલાઈન",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = DarkHighwayBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurfaceCard,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == "AMBULANCE",
                                onClick = { viewModel.selectTab("AMBULANCE") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "એમ્બ્યુલન્સ"
                                    )
                                },
                                label = { Text("એમ્બ્યુલન્સ", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmergencyRedBright,
                                    selectedTextColor = EmergencyRedBright,
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8),
                                    indicatorColor = EmergencyRed.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_tab_ambulance")
                            )

                            NavigationBarItem(
                                selected = selectedTab == "CIVILIAN",
                                onClick = { viewModel.selectTab("CIVILIAN") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "સ્પેસ એલર્ટ"
                                    )
                                },
                                label = { Text("સ્પેસ એલર્ટ", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmergencyRedBright,
                                    selectedTextColor = EmergencyRedBright,
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8),
                                    indicatorColor = EmergencyRed.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_tab_civilian")
                            )

                            NavigationBarItem(
                                selected = selectedTab == "TRAFFIC_CONTROL",
                                onClick = { viewModel.selectTab("TRAFFIC_CONTROL") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Traffic,
                                        contentDescription = "સિગ્નલ કંટ્રોલ"
                                    )
                                },
                                label = { Text("ગ્રીન વેવ", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TrafficGreen,
                                    selectedTextColor = TrafficGreen,
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8),
                                    indicatorColor = TrafficGreen.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_tab_traffic")
                            )

                            NavigationBarItem(
                                selected = selectedTab == "HOSPITALS",
                                onClick = { viewModel.selectTab("HOSPITALS") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = "હોસ્પિટલો"
                                    )
                                },
                                label = { Text("હોસ્પિટલો", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF38BDF8),
                                    selectedTextColor = Color(0xFF38BDF8),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8),
                                    indicatorColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_tab_hospitals")
                            )

                            NavigationBarItem(
                                selected = selectedTab == "HISTORY",
                                onClick = { viewModel.selectTab("HISTORY") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "ઇતિહાસ"
                                    )
                                },
                                label = { Text("ઇતિહાસ", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFFFBBF24),
                                    selectedTextColor = Color(0xFFFBBF24),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8),
                                    indicatorColor = Color(0xFFFBBF24).copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_tab_history")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_transition"
                        ) { tab ->
                            when (tab) {
                                "AMBULANCE" -> AmbulancePilotScreen(viewModel = viewModel)
                                "CIVILIAN" -> CivilianAlertScreen(viewModel = viewModel)
                                "TRAFFIC_CONTROL" -> TrafficCommandScreen(viewModel = viewModel)
                                "HOSPITALS" -> HospitalDirectoryScreen(viewModel = viewModel)
                                "HISTORY" -> MissionHistoryScreen(viewModel = viewModel)
                                else -> AmbulancePilotScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
