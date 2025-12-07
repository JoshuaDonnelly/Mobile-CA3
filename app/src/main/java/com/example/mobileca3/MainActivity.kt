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
import coil.compose.rememberAsyncImagePainter
import com.example.compose.AppTheme
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
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


object FavouriteManager {
    private const val PREFS = "favourites_prefs"
    private const val KEY = "favourite_titles"

    fun saveFavourite(context: Context, title: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY, mutableSetOf()) ?: mutableSetOf()
        prefs.edit().putStringSet(KEY, existing + title).apply()
    }

    fun removeFavourite(context: Context, title: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY, mutableSetOf()) ?: mutableSetOf()
        prefs.edit().putStringSet(KEY, existing - title).apply()
    }

    fun getFavourites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY, emptySet()) ?: emptySet()
    }
}


// PROFILE
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
fun PocketChef(darkTheme: Boolean, onThemeUpdated: () -> Unit, widthClass: WindowWidthSizeClass) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val route = navBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (route != "splash") BottomNavigationBar(navController, route)
        }
    ) { pad ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(pad)
        ) {
            composable("splash") {
                SplashScreen()
                LaunchedEffect(Unit) {
                    delay(700)
                    navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                }
            }

            composable("home") {
                HomeScreen(darkTheme, onThemeUpdated, widthClass)
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
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "Logo",
            modifier = Modifier.size(140.dp)
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

@Composable
fun MealCard(meal: Meal, index: Int, tablet: Boolean) {
    val context = LocalContext.current
    var favourite by remember { mutableStateOf(meal.strMeal in FavouriteManager.getFavourites(context)) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * 100).toLong())
        visible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 60f,
        animationSpec = spring(dampingRatio = 0.65f),
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
            Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(meal.strMealThumb),
                contentDescription = null,
                modifier = Modifier
                    .size(imgSize)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
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
                                    FavouriteManager.saveFavourite(context, meal.strMeal)
                                else
                                    FavouriteManager.removeFavourite(context, meal.strMeal)
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
fun FavouritesScreen(widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)
    val context = LocalContext.current

    val favourites = FavouriteManager.getFavourites(context).toList()

    Column(Modifier.fillMaxSize().padding(if (tablet) 32.dp else 16.dp)) {
        Text(
            "Favourite Recipes",
            style = if (tablet) MaterialTheme.typography.headlineLarge
            else MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        if (favourites.isEmpty()) {
            Text("No favourites yet!")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(favourites) { _, title ->
                    Card(Modifier.fillMaxWidth()) {
                        Text(title, Modifier.padding(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(widthClass: WindowWidthSizeClass) {
    val tablet = isTablet(widthClass)
    val pad = if (tablet) 32.dp else 16.dp
    val titleStyle = if (tablet) MaterialTheme.typography.headlineLarge
    else MaterialTheme.typography.headlineMedium

    val context = LocalContext.current

    var username by remember { mutableStateOf(ProfileManager.getUsername(context)) }
    var fullName by remember { mutableStateOf(ProfileManager.getFullName(context)) }
    var saved by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(pad),
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

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                ProfileManager.saveProfile(context, username.trim(), fullName.trim())
                saved = true
            }) { Text("Save") }

            OutlinedButton(onClick = {
                username = ""
                fullName = ""
                ProfileManager.saveProfile(context, "", "")
                saved = false
            }) { Text("Clear") }
        }

        if (saved) Text("Profile saved.", color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.weight(1f))

        Divider()

        Text("Saved profile:")
        Text("Username: ${ProfileManager.getUsername(context)}")
        Text("Full name: ${ProfileManager.getFullName(context)}")
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, route: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = route == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Filled.Home, null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = route == "favourites",
            onClick = { navController.navigate("favourites") },
            icon = { Icon(Icons.Filled.Favorite, null) },
            label = { Text("Favourites") }
        )

        NavigationBarItem(
            selected = route == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Filled.Person, null) },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun AnimatedText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val yOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1800),
        label = ""
    )

    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier.graphicsLayer(
            translationY = yOffset,
            alpha = alpha
        )
    )
}

@Composable
fun isTablet(widthClass: WindowWidthSizeClass): Boolean {
    return widthClass != WindowWidthSizeClass.Compact
}
