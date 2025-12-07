package com.example.mobileca3

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.compose.AppTheme
import com.example.mobileca3.ui.theme.nunitoFont
import kotlinx.coroutines.delay


// Favourites Storer (titles only)
//Temporary for functionality purposes**

object FavouriteManager {
    private const val PREFS = "favourites_prefs"
    private const val KEY = "favourite_titles"

    fun saveFavourite(context: Context, recipe: Recipe) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY, mutableSetOf()) ?: mutableSetOf()
        val updated = existing + recipe.title
        prefs.edit().putStringSet(KEY, updated).apply()
    }

    fun removeFavourite(context: Context, recipe: Recipe) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY, mutableSetOf()) ?: mutableSetOf()
        val updated = existing - recipe.title
        prefs.edit().putStringSet(KEY, updated).apply()
    }

    fun getFavourites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY, emptySet()) ?: emptySet()
    }

    fun isFavourite(context: Context, recipe: Recipe): Boolean {
        return getFavourites(context).contains(recipe.title)
    }
}

//Temporary for functionality purposes**
// Profile storer(username + fullname)
// Saves two simple strings to SharedPreferences.
// Keys: "profile_username", "profile_fullname"
object ProfileManager {
    private const val PREFS = "profile_prefs"
    private const val KEY_USERNAME = "profile_username"
    private const val KEY_FULLNAME = "profile_fullname"

    fun saveProfile(context: Context, username: String, fullName: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_FULLNAME, fullName)
            .apply()
    }

    fun getUsername(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }

    fun getFullName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FULLNAME, "") ?: ""
    }
}

data class Recipe(
    val title: String,
    val description: String,
    val imageRes: Int
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val widthClass = calculateWindowSizeClass(this).widthSizeClass
            val systemIsDark = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemIsDark) }

            AppTheme(darkTheme = darkTheme) {
                PocketChef(
                    darkTheme = darkTheme,
                    onThemeUpdated = { darkTheme = !darkTheme },
                    widthClass = widthClass
                )
            }
        }
    }
}

@Composable
fun PocketChef(darkTheme: Boolean,
               onThemeUpdated: () -> Unit,
               widthClass: WindowWidthSizeClass) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash") {
                BottomNavigationBar(navController, currentRoute)
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

            composable("home") {
                HomeScreen(
                    darkTheme = darkTheme,
                    onThemeUpdated = onThemeUpdated,
                    widthClass = widthClass
                )
            }

            // Favourites screen already present
            composable("favourites") {
                FavouritesScreen(widthClass = widthClass)
            }

            // NEW: Profile route
            composable("profile") {
                ProfileScreen(widthClass = widthClass)
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
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "Pocket Chef",
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun HomeScreen(darkTheme: Boolean,
               onThemeUpdated: () -> Unit,
               widthClass: WindowWidthSizeClass) {

    val context = LocalContext.current
    val tablet = isTablet(widthClass)

    var username by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        username = ProfileManager.getUsername(context)
    }


    val sampleRecipes = listOf(
        Recipe("Spaghetti Bolognese", "Rich tomato sauce with minced beef and herbs.", R.drawable.spagbol),
        Recipe("Chicken Stir Fry", "Quick, colorful vegetables with sticky soy glaze.", R.drawable.stirfry),
        Recipe("Beef Tacos", "Seasoned beef with lettuce, cheese & salsa.", R.drawable.tacos),
        Recipe("Garlic Butter Salmon", "Creamy, flaky salmon with herbs & lemon.", R.drawable.salmon),
        Recipe("Pancakes & Syrup", "Fluffy stack with maple drizzle.", R.drawable.pancakes)
    )

    val horizontalPadding = if (tablet) 32.dp else 16.dp
    val topSpacing = if (tablet) 24.dp else 8.dp
    val titleSize = if (tablet) 42.sp else 30.sp
    val bodySize = if (tablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(topSpacing))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontalPadding),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = darkTheme,
                onCheckedChange = { onThemeUpdated() }
            )

            Text(
                text = "Dark Mode",
                style = bodySize,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedText(
            text = if (username.isEmpty()) "Pocket Chef" else "Hi Again, $username!",
            style = TextStyle(fontSize = titleSize, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedText(
            text ="What's on Today's Menu?",
            style = TextStyle(fontSize = titleSize, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (tablet) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(sampleRecipes) { index, recipe ->
                    AnimatedRecipeCard(recipe, index, tablet)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(sampleRecipes) { index, recipe ->
                    AnimatedRecipeCard(recipe, index, tablet)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, null) },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, null) },
            label = { Text("Favourites") },
            selected = currentRoute == "favourites",
            onClick = { navController.navigate("favourites") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, null) },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") }
        )
    }
}

@Composable
fun AnimatedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val yOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing)
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(2000, easing = LinearOutSlowInEasing)
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
fun AnimatedRecipeCard(recipe: Recipe, index: Int, tablet: Boolean) {
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }

    // read favourite once on composition and keep local state for toggling
    var favouriteState by remember { mutableStateOf(FavouriteManager.isFavourite(context, recipe)) }

    LaunchedEffect(Unit) {
        delay((index * 100).toLong())
        visible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 60f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = ""
    )

    val imageSize = if (tablet) 160.dp else 120.dp
    val padding = if (tablet) 16.dp else 16.dp
    val titleStyle =
        if (tablet) MaterialTheme.typography.headlineSmall
        else MaterialTheme.typography.titleLarge

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = offsetY; this.alpha = alpha }
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            horizontalArrangement = Arrangement.Start
        ) {

            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(imageSize)
                    .padding(padding)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recipe.title,
                        style = titleStyle,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (favouriteState) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Toggle Favourite",
                        tint = if (favouriteState) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(if (tablet) 34.dp else 28.dp)
                            .clickable {
                                favouriteState = !favouriteState
                                if (favouriteState)
                                    FavouriteManager.saveFavourite(context, recipe)
                                else
                                    FavouriteManager.removeFavourite(context, recipe)
                            }
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FavouritesScreen(widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)
    val padding = if (tablet) 32.dp else 16.dp
    val titleStyle =
        if (tablet) MaterialTheme.typography.headlineLarge
        else MaterialTheme.typography.headlineMedium
    val bodyStyle =
        if (tablet) MaterialTheme.typography.bodyLarge
        else MaterialTheme.typography.bodyMedium

    val context = LocalContext.current
    val savedTitles = remember {
        mutableStateOf(FavouriteManager.getFavourites(context).toList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Text("Favourite Recipes", style = titleStyle)

        Spacer(Modifier.height(16.dp))

        if (savedTitles.value.isEmpty()) {
            Text("No favourites yet!")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(savedTitles.value) { _, title ->
                    Card(Modifier.fillMaxWidth()) {
                        Text(title, Modifier.padding(24.dp), style = bodyStyle)
                    }
                }
            }
        }
    }
}


// ----------------- PROFILE SCREEN (Compose) -----------------
@Composable
fun ProfileScreen(widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)
    val padding = if (tablet) 32.dp else 16.dp
    val titleStyle =
        if (tablet) MaterialTheme.typography.headlineLarge
        else MaterialTheme.typography.headlineMedium

    val context = LocalContext.current

    var username by remember { mutableStateOf(ProfileManager.getUsername(context)) }
    var fullName by remember { mutableStateOf(ProfileManager.getFullName(context)) }
    var savedConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(if (tablet) 24.dp else 16.dp)
    ) {
        Text("Profile", style = titleStyle)

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                ProfileManager.saveProfile(context, username.trim(), fullName.trim())
                savedConfirmation = true
            }) { Text("Save") }

            OutlinedButton(onClick = {
                username = ""
                fullName = ""
                ProfileManager.saveProfile(context, "", "")
                savedConfirmation = false
            }) { Text("Clear") }
        }

        if (savedConfirmation) {
            Text("Profile saved.", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.weight(1f))

        Divider()
        Text("Saved profile:")
        Text("Username: ${ProfileManager.getUsername(context)}")
        Text("Full name: ${ProfileManager.getFullName(context)}")
    }
}


@Composable
fun isTablet(widthClass: WindowWidthSizeClass): Boolean {
    return widthClass != WindowWidthSizeClass.Compact
}
