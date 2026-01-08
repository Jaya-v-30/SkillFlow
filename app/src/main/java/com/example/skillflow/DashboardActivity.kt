package com.example.skillflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.skillflow.ui.theme.SkillFlowTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                Toast.makeText(this, "Photo captured ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) cameraLauncher.launch(null)
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkillFlowTheme {
                DashboardScreen(
                    onCameraClick = { handleCameraClick() }
                )
            }
        }
    }

    private fun handleCameraClick() {
        val granted =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED

        if (granted) cameraLauncher.launch(null)
        else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }
}

data class Course(
    val id: String,
    val title: String,
    val category: String,
    val level: String,
    val progress: Int,
    val duration: String
)

data class CategoryChip(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onCameraClick: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName ?: "Learner"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Snackbar for "Course started"
    val snackbarHostState = remember { SnackbarHostState() }

    val categories = listOf(
        CategoryChip("All"),
        CategoryChip("Programming"),
        CategoryChip("Design"),
        CategoryChip("AI / ML"),
        CategoryChip("Business"),
        CategoryChip("Tools")
    )

    // Sample data
    val featuredCourses = listOf(
        Course("1", "Kotlin for Android", "Programming", "Beginner", 0, "1h 45m"),
        Course("2", "Figma UI Design Basics", "Design", "Beginner", 0, "2h 10m"),
        Course("3", "Intro to Machine Learning", "AI / ML", "Intermediate", 0, "3h 00m")
    )

    val continueCourses = listOf(
        Course("4", "Jetpack Compose Essentials", "Programming", "Intermediate", 45, "40m left"),
        Course("5", "Git & GitHub for Beginners", "Tools", "Beginner", 70, "25m left")
    )

    // Combine all courses for searching/filtering
    val allCourses = remember { featuredCourses + continueCourses }

    // Apply filter
    val filteredCourses = remember(searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()

        allCourses.filter { course ->
            val matchCategory =
                selectedCategory == "All" || course.category.equals(selectedCategory, ignoreCase = true)

            val matchSearch =
                q.isBlank() ||
                        course.title.lowercase().contains(q) ||
                        course.category.lowercase().contains(q) ||
                        course.level.lowercase().contains(q)

            matchCategory && matchSearch
        }
    }

    // Split back into featured vs continue (so your UI sections still work)
    val filteredFeatured = filteredCourses.filter { it.id in featuredCourses.map { c -> c.id }.toSet() }
    val filteredContinue = filteredCourses.filter { it.id in continueCourses.map { c -> c.id }.toSet() }

    fun startCourse(course: Course) {
        Toast.makeText(context, "Course started: ${course.title}", Toast.LENGTH_SHORT).show()
        // also show snackbar (looks nicer)
        // no coroutines needed if we use LaunchedEffect-style call:
        // We'll trigger via rememberCoroutineScope
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hello, $displayName 👋", style = MaterialTheme.typography.titleMedium)
                        Text("Ready to learn today?", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E3A8A).copy(alpha = 0.10f),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(padding)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Search
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Search courses...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category chips (functional)
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { chip ->
                            AssistChip(
                                onClick = { selectedCategory = chip.name },
                                label = { Text(chip.name) },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (selectedCategory == chip.name) Color.White else Color(0xFF4C6EF5)
                                ),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor =
                                        if (selectedCategory == chip.name) Color(0xFF4C6EF5) else Color.Transparent,
                                    labelColor =
                                        if (selectedCategory == chip.name) Color.White else Color(0xFF4C6EF5)
                                )
                            )
                        }
                    }
                }

                // Featured
                item { SectionTitle("Featured for you") }

                item {
                    if (filteredFeatured.isEmpty()) {
                        Text("No courses found.", color = Color.White.copy(alpha = 0.8f))
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredFeatured) { course ->
                                FeaturedCourseCard(
                                    course = course,
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Course started: ${course.title}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        scope.launch {
                                            snackbarHostState.showSnackbar("Course started: ${course.title}")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Continue learning
                item { SectionTitle("Continue learning") }

                if (filteredContinue.isEmpty()) {
                    item {
                        Text("No courses found.", color = Color.White.copy(alpha = 0.8f))
                    }
                } else {
                    items(filteredContinue) { course ->
                        ContinueCourseRow(
                            course = course,
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Course started: ${course.title}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                scope.launch {
                                    snackbarHostState.showSnackbar("Course started: ${course.title}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = Color.White
    )
}

@Composable
fun FeaturedCourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4C6EF5))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = course.title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${course.category} • ${course.level}",
                color = Color.White.copy(0.85f),
                fontSize = 12.sp
            )

            Text(
                text = "Duration: ${course.duration}  • Tap to start",
                color = Color.White.copy(0.85f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ContinueCourseRow(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171A1F))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = course.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${course.category} • ${course.level} • ${course.duration}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = course.progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text("${course.progress}%", color = Color.White, fontSize = 12.sp)
        }
    }
}
