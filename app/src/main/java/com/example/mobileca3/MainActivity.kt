package com.example.mobileca3

import android.content.Context
import android.os.Bundle
import android.util.Log
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

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import coil.compose.rememberAsyncImagePainter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
// Retrofit recipe grabber
data class MealResponse(
    val meals: List<Meal>?
)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strInstructions: String,
    val strMealThumb: String
)

interface MealApi {
    @GET("search.php?s=")
    suspend fun getAllMeals(): MealResponse
}

object ApiClient {
    val api: MealApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApi::class.java)
    }
}

class MealRepository {
    suspend fun loadMeals(): List<Meal> {
        return ApiClient.api.getAllMeals().meals ?: emptyList()
    }
}

class MealViewModel : ViewModel() {
    private val repo = MealRepository()

    var meals by mutableStateOf<List<Meal>>(emptyList())
        private set

    init {
        fetchMeals()
    }

    private fun fetchMeals() {
        // use viewModelScope (instance property), not the package-qualified name
        viewModelScope.launch {
            meals = repo.loadMeals()
        }
    }
}

// Managers
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
//Recipe data class to be used in the animated cards (MealCard)
data class Recipe(
    val title: String,
    val description: String,
    val imageUrl: String
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

//Here is where we have all our routes, call our nav bar
@Composable
fun PocketChef(darkTheme: Boolean, onThemeUpdated: () -> Unit, widthClass: WindowWidthSizeClass) {
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

            composable("favourites") {
                FavouritesScreen(widthClass)
            }

            composable("profile") {
                ProfileScreen(widthClass)
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
fun HomeScreen(darkTheme: Boolean, onThemeUpdated: () -> Unit, widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)
    val vm: MealViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val context = LocalContext.current
    var username by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        username = ProfileManager.getUsername(context)
    }

    val horizontalPadding = if (tablet) 32.dp else 16.dp
    val titleSize = if (tablet) 42.sp else 30.sp

    Column(
        Modifier.fillMaxSize().padding(horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Switch(checked = darkTheme, onCheckedChange = { onThemeUpdated() })
            Text("Dark Mode", Modifier.padding(start = 8.dp))
        }

        Spacer(Modifier.height(40.dp))

        AnimatedText(
            text = if (username.isEmpty()) "Pocket Chef" else "Hi Again, $username!",
            style = TextStyle(fontSize = titleSize, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        AnimatedText(
            text = "What's on Today's Menu?",
            style = TextStyle(fontSize = titleSize, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        val meals = vm.meals

        if (meals.isEmpty()) {
            CircularProgressIndicator()
        } else {
            if (tablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(meals) { i, meal ->
                        MealCard(meal, i, tablet)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(meals) { i, meal ->
                        MealCard(meal, i, tablet)
                    }
                }
            }
        }
    }
}

//Retrofit recipe card
@Composable
fun MealCard(meal: Meal, index: Int, tablet: Boolean) {
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }
    var favourite by remember { mutableStateOf(meal.strMeal in FavouriteManager.getFavourites(context)) }

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

    val imgSize = if (tablet) 160.dp else 120.dp

    Card(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = offsetY; this.alpha = alpha }
            .padding(6.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = meal.strMealThumb,
                contentDescription = null,
                modifier = Modifier
                    .size(imgSize)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.icon),
                placeholder = painterResource(id = R.drawable.icon)
            )

            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        meal.strMeal,
                        style = if (tablet) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.titleLarge
                    )

                    Icon(
                        imageVector = if (favourite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favourite",
                        tint = if (favourite) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(if (tablet) 34.dp else 28.dp)
                            .clickable {
                                favourite = !favourite
                                if (favourite)
                                    FavouriteManager.saveFavourite(context, Recipe(meal.strMeal, meal.strInstructions, meal.strMealThumb))
                                else
                                    FavouriteManager.removeFavourite(context, Recipe(meal.strMeal, meal.strInstructions, meal.strMealThumb))
                            }
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    meal.strInstructions.take(140) + "...",
                    style = MaterialTheme.typography.bodyMedium
                )
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
//Screen 2 (Favourites)
@Composable
fun FavouritesScreen(widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)

    val context = LocalContext.current
    // Recompose when we return to screen: read latest favourites each composition
    val savedTitles = remember { mutableStateOf(FavouriteManager.getFavourites(context).toList()) }
    Log.d("Pocket Chef (Favs)", "Favourites List: ${savedTitles.value}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (tablet) 32.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Favourite Recipes ⭐",
            style = if (tablet) MaterialTheme.typography.headlineLarge
            else MaterialTheme.typography.headlineMedium,
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

// Screen 3 (Profile)
@Composable
fun ProfileScreen(widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)
    val pad = if (tablet) 32.dp else 16.dp
    val titleStyle = if (tablet) MaterialTheme.typography.headlineLarge
    else MaterialTheme.typography.headlineMedium

    val context = LocalContext.current

    // Initialize state from SharedPreferences
    var username by remember { mutableStateOf(ProfileManager.getUsername(context)) }
    var fullName by remember { mutableStateOf(ProfileManager.getFullName(context)) }
    var savedConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(pad),
        verticalArrangement = Arrangement.spacedBy(if (tablet) 24.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Profile",
            style = titleStyle,
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

@Composable
fun isTablet(widthClass: WindowWidthSizeClass): Boolean {
    return widthClass != WindowWidthSizeClass.Compact
}
