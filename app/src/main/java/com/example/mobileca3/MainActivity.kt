package com.example.mobileca3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.material3.AnimatedText
import com.example.compose.AppTheme
import com.example.mobileca3.ui.theme.nunitoFont
import kotlinx.coroutines.delay




data class Recipe(
    val title: String,
    val description: String
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val systemIsDark = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemIsDark) }

            AppTheme(darkTheme = darkTheme) {
                PocketChef(
                    darkTheme = darkTheme,
                    onThemeUpdated = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

@Composable
fun PocketChef(darkTheme: Boolean, onThemeUpdated: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "home") {
                BottomNavigationBar()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("splash") {
                SplashScreen()
                LaunchedEffect(Unit) {
                    delay(500)
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            // Here we have our routes to the other screens
            composable("home") {
                homeScreen(
                    darkTheme = darkTheme,
                    onThemeUpdated = onThemeUpdated
                )
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pocket Chef",
            style = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = nunitoFont
            )
        )
    }
}

@Composable
fun homeScreen(darkTheme: Boolean, onThemeUpdated: () -> Unit) {

    //The recipes are just placeholder for now, will probably change to something else
    val sampleRecipes = listOf(
        Recipe("Spaghetti Bolognese", "Rich tomato sauce with minced beef and herbs."),
        Recipe("Chicken Stir Fry", "Quick, colorful vegetables with sticky soy glaze."),
        Recipe("Beef Tacos", "Seasoned beef with lettuce, cheese & salsa."),
        Recipe("Garlic Butter Salmon", "Creamy, flaky salmon with herbs & lemon."),
        Recipe("Pancakes & Syrup", "Fluffy stack with maple drizzle.")
    )
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer (modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = darkTheme,
                onCheckedChange = { onThemeUpdated() }
            )

            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.padding(40.dp))

        //Main content
        AnimatedText(
            text ="What's on Today's Menu?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

    Spacer(modifier = Modifier.padding(12.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(sampleRecipes) { index, recipe ->
            AnimatedRecipeCard(recipe, index)
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true,
            onClick = { /*TODO*/ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Search") },
            selected = false,
            onClick = { /*TODO*/ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { /*TODO*/ }
        )

    }
}
//https://developer.android.com/develop/ui/views/animations/transitions
@Composable
fun AnimatedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier
) {

    //Trigger animation when shown
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }
    val yOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
    )

    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier.graphicsLayer(
            translationY = yOffset,
            scaleX = scale,
            scaleY = scale,
            alpha = alpha
        )
    )

}
@Composable
fun AnimatedRecipeCard(recipe: Recipe, index: Int) {
    val animationDelay = 100 * index

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }
    //https://developer.android.com/develop/ui/views/animations/spring-animation
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 60f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300), label = ""
    )


    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
            }
            .padding(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = recipe.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.padding(4.dp))
            Text(text = recipe.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

