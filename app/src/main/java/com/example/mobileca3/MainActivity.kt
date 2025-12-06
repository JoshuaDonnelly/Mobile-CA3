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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import coil.compose.AsyncImage



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
    val imageUrl: String
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
                    onThemeUpdated = onThemeUpdated
                )
            }

            composable("favourites") {
                FavouritesScreen()
            }

            composable("profile") {
                ProfileScreen()
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
fun HomeScreen(darkTheme: Boolean, onThemeUpdated: () -> Unit) {

    val context = LocalContext.current

    var username by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        username = ProfileManager.getUsername(context)
    }


    val sampleRecipes = listOf(
        Recipe("Spaghetti Bolognese", "Rich tomato sauce with minced beef and herbs.", "https://www.kitchensanctuary.com/wp-content/uploads/2019/09/Spaghetti-Bolognese-square-FS-0204.jpg"),
        Recipe("Chicken Stir Fry", "Quick, colorful vegetables with sticky soy glaze.", "https://thegirlonbloor.com/wp-content/uploads/2019/04/The-best-Beef-stir-fry-3-500x500.jpg"),
        Recipe("Beef Tacos", "Seasoned beef with lettuce, cheese & salsa.", "https://oliviaadriance.com/wp-content/uploads/2023/07/Final_3_Crispy_Baked_Beef_Tacos_grain-free-dairy-free.jpg"),
        Recipe("Garlic Butter Salmon", "Creamy, flaky salmon with herbs & lemon.", "https://www.kitchensanctuary.com/wp-content/uploads/2020/05/Honey-Garlic-Baked-Salmon-square-FS-111.jpg"),
        Recipe("Pancakes & Syrup", "Fluffy stack with maple drizzle.", "https://www.allrecipes.com/thmb/TvmI_Fszqlu7ITqqhtj8l_JWqZo=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/21014-Good-old-Fashioned-Pancakes-primary-4x3-c991bb30cf5a4078b61e3808b7ebcda8.jpg")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(40.dp))
        if (username.isEmpty()) {
            AnimatedText(
                text ="Pocket Chef",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            AnimatedText(
                text = "Hi Again, $username!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        AnimatedText(
            text ="What's on Today's Menu?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

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
fun AnimatedRecipeCard(recipe: Recipe, index: Int) {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = offsetY; this.alpha = alpha }
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {

            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.icon),    // Shows if loading failed
                placeholder = painterResource(id = R.drawable.icon) // Shows during loading
            )

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (favouriteState) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Toggle Favourite",
                        tint = if (favouriteState) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(28.dp)
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
fun FavouritesScreen() {
    val context = LocalContext.current
    // Recompose when we return to screen: read latest favourites each composition
    val savedTitles = remember { mutableStateOf(FavouriteManager.getFavourites(context).toList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Favourite Recipes ⭐",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold,
        )

        if (savedTitles.value.isEmpty()) {
            Text(
                "No favourites yet!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.
                    padding( top = 116.dp),
                fontWeight = FontWeight.Bold,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(savedTitles.value) { _, title ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

// ----------------- PROFILE SCREEN -----------------
@Composable
fun ProfileScreen() {
    val context = LocalContext.current

    // Initialize state from SharedPreferences
    var username by remember { mutableStateOf(ProfileManager.getUsername(context)) }
    var fullName by remember { mutableStateOf(ProfileManager.getFullName(context)) }
    var savedConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold,
        )
        AsyncImage(
            model = "https://cdn-icons-png.flaticon.com/512/5987/5987424.png",
            contentDescription = null,
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                ProfileManager.saveProfile(context, username.trim(), fullName.trim())
                savedConfirmation = true
            }) {
                Text("Save Profile")
            }

            //clear button
            OutlinedButton(onClick = {
                username = ""
                fullName = ""
                ProfileManager.saveProfile(context, "", "")
                savedConfirmation = false
            }) {
                Text("Clear")
            }
        }

        if (savedConfirmation) {
            Text(
                text = "Profile saved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Show saved profile summary
        Divider()
        Text("Saved profile:", style = MaterialTheme.typography.titleSmall)
        Text("Username: ${ProfileManager.getUsername(context)}")
        Text("Full name: ${ProfileManager.getFullName(context)}")
    }
}
