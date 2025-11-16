package com.example.skillflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillflow.ui.theme.SkillFlowTheme
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkillFlowTheme {
                DashboardScreen()
            }
        }
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
fun DashboardScreen() {

    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName ?: "Learner"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        CategoryChip("All"),
        CategoryChip("Programming"),
        CategoryChip("Design"),
        CategoryChip("AI / ML"),
        CategoryChip("Business")
    )

    val featuredCourses = listOf(
        Course("1", "Kotlin for Android", "Programming", "Beginner", 0, "1h 45m"),
        Course("2", "Figma UI Design Basics", "Design", "Beginner", 0, "2h 10m"),
        Course("3", "Intro to Machine Learning", "AI/ML", "Intermediate", 0, "3h 00m")
    )

    val continueCourses = listOf(
        Course("4", "Jetpack Compose Essentials", "Programming", "Intermediate", 45, "40m left"),
        Course("5", "Git & GitHub for Beginners", "Tools", "Beginner", 70, "25m left")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, $displayName 👋",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Ready to learn today?",
                            style = MaterialTheme.typography.bodySmall
                        )
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
                            Color(0xFF1E3A8A).copy(alpha = 0.07f),
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

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { chip ->
                            AssistChip(
                                onClick = { selectedCategory = chip.name },
                                label = { Text(chip.name) },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (selectedCategory == chip.name)
                                        Color.White
                                    else
                                        Color(0xFF4C6EF5)
                                ),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor =
                                        if (selectedCategory == chip.name)
                                            Color(0xFF4C6EF5)
                                        else
                                            Color.Transparent,
                                    labelColor =
                                        if (selectedCategory == chip.name)
                                            Color.White
                                        else
                                            Color(0xFF4C6EF5)
                                )
                            )
                        }
                    }
                }

                item { SectionTitle("Featured for you") }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(featuredCourses) { course ->
                            FeaturedCourseCard(course)
                        }
                    }
                }

                item { SectionTitle("Continue learning") }

                items(continueCourses) { course ->
                    ContinueCourseRow(course)
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
fun FeaturedCourseCard(course: Course) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4C6EF5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                text = "${course.level} • ${course.duration}",
                color = Color.White.copy(0.8f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ContinueCourseRow(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = course.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = course.progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                "${course.progress}%",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}
