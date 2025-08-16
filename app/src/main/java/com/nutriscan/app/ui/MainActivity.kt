package com.nutriscan.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriscan.app.R
import com.nutriscan.app.ui.scanner.BarcodeScannerActivity
import com.nutriscan.app.ui.ar.ARActivity
import com.nutriscan.app.ui.theme.NutriScanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(ComposeView(this).apply {
            setContent {
                NutriScanTheme {
                    MainScreen(
                        onScanClick = { openBarcodeScanner() },
                        onARClick = { /* Handle AR navigation */ }
                    )
                }
            }
        })
    }
    
    private fun openBarcodeScanner() {
        val intent = Intent(this, BarcodeScannerActivity::class.java)
        startActivity(intent)
    }
    
    private fun openARView() {
        val intent = Intent(this, ARActivity::class.java)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onScanClick: () -> Unit,
    onARClick: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem("home", "Home", Icons.Default.Home),
                    BottomNavItem("scan", "Scan", Icons.Default.QrCodeScanner),
                    BottomNavItem("nutrition", "Nutrition", Icons.Default.Analytics),
                    BottomNavItem("profile", "Profile", Icons.Default.Person)
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = false, // TODO: Implement proper selection state
                        onClick = {
                            when (item.route) {
                                "scan" -> onScanClick()
                                else -> navController.navigate(item.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(onScanClick, onARClick) }
            composable("nutrition") { NutritionScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onARClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Welcome to NutriScan",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Scan foods for instant nutrition insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f),
                onClick = onScanClick
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scan Barcode")
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                onClick = onARClick
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ViewInAr,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AR View")
                }
            }
        }
        
        // Recent scans section
        Text(
            text = "Recent Scans",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        RecentScansSection()
    }
}

@Composable
fun RecentScansSection() {
    // Placeholder for recent scans
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(3) { index ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sample Food Item $index",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "250 calories • Grade: B+",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nutrition Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Daily goals progress
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Daily Goals",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Calories progress
                NutritionProgressItem("Calories", 1400, 2000, "kcal")
                Spacer(modifier = Modifier.height(8.dp))
                NutritionProgressItem("Protein", 65, 150, "g")
                Spacer(modifier = Modifier.height(8.dp))
                NutritionProgressItem("Carbs", 120, 250, "g")
            }
        }
        
        // Weekly trends (placeholder)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Trends",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Charts and analytics coming soon!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun NutritionProgressItem(
    label: String,
    current: Int,
    goal: Int,
    unit: String
) {
    val progress = (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label)
            Text(text = "$current / $goal $unit")
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Profile info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "John Doe", // Placeholder
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Age: 30 • Height: 175cm • Weight: 70kg")
                Text("Goal: Weight Loss")
            }
        }
        
        // Settings items
        val settingsItems = listOf(
            "Health Goals",
            "Dietary Restrictions",
            "Fitness Integration",
            "Notifications",
            "Privacy Settings"
        )
        
        settingsItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Handle setting click */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item)
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
