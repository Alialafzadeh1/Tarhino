package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ai.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.PromptEntity
import com.example.data.repository.TarhiNooRepository
import com.example.domain.model.AICoreState
import com.example.domain.model.AppLanguage
import com.example.ui.components.TarhiNooBottomNav
import com.example.ui.components.TarhiNooTopBar
import com.example.ui.screens.AIChatScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AssetVaultScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.CreativeAgentScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NavaStudioScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.PromptBuilderScreen
import com.example.ui.screens.PromptOptimizerScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.TarhiNooTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarhiNooApp()
        }
    }
}

@Composable
fun TarhiNooApp() {
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getDatabase(context)
        TarhiNooRepository(db)
    }
    val geminiService = remember { GeminiService() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentLanguage by remember { mutableStateOf(AppLanguage.PERSIAN) }
    var currentRoute by remember { mutableStateOf("home") }
    var routeBackStack by remember { mutableStateOf(listOf("home")) }

    var aiCoreState by remember { mutableStateOf(AICoreState.IDLE) }
    var isAiThinking by remember { mutableStateOf(false) }

    // Seed database on launch
    LaunchedEffect(Unit) {
        repository.initSeedDataIfNeeded()
    }

    // Collect data streams
    val prompts by repository.allPrompts.collectAsState(initial = emptyList())
    val projects by repository.allProjects.collectAsState(initial = emptyList())
    val conversations by repository.allConversations.collectAsState(initial = emptyList())
    val assets by repository.allAssets.collectAsState(initial = emptyList())
    val historyList by repository.allHistory.collectAsState(initial = emptyList())
    val notifications by repository.allNotifications.collectAsState(initial = emptyList())
    val communityPosts by repository.communityPosts.collectAsState(initial = emptyList())

    // Active conversation state
    var activeConversationId by remember { mutableLongStateOf(0L) }
    val activeMessages by repository.getMessagesForConversation(activeConversationId).collectAsState(initial = emptyList())

    LaunchedEffect(conversations) {
        if (activeConversationId == 0L && conversations.isNotEmpty()) {
            activeConversationId = conversations.first().id
        }
    }

    // Shared state between screens
    var builderInitialSubject by remember { mutableStateOf("") }
    var optimizerInitialPrompt by remember { mutableStateOf("") }

    fun navigateTo(route: String) {
        if (currentRoute != route) {
            routeBackStack = routeBackStack + route
            currentRoute = route
        }
    }

    fun navigateBack(): Boolean {
        if (routeBackStack.size > 1) {
            val updated = routeBackStack.dropLast(1)
            routeBackStack = updated
            currentRoute = updated.last()
            return true
        }
        return false
    }

    BackHandler(enabled = routeBackStack.size > 1) {
        navigateBack()
    }

    // RTL dynamic support
    val layoutDirection = if (currentLanguage == AppLanguage.PERSIAN) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        TarhiNooTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = BgDark,
                topBar = {
                    TarhiNooTopBar(
                        currentLanguage = currentLanguage,
                        onToggleLanguage = {
                            currentLanguage = if (currentLanguage == AppLanguage.PERSIAN) AppLanguage.ENGLISH else AppLanguage.PERSIAN
                        },
                        aiState = aiCoreState,
                        onNavigate = { dest -> navigateTo(dest) }
                    )
                },
                bottomBar = {
                    TarhiNooBottomNav(
                        currentRoute = currentRoute,
                        currentLanguage = currentLanguage,
                        onNavigate = { dest -> navigateTo(dest) }
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(BgDark)
                ) {
                    Crossfade(targetState = currentRoute, label = "screen_crossfade") { route ->
                        when (route) {
                            "home" -> HomeScreen(
                                currentLanguage = currentLanguage,
                                prompts = prompts,
                                projects = projects,
                                onNavigate = { dest -> navigateTo(dest) },
                                onSelectPrompt = { p ->
                                    builderInitialSubject = p.prompt
                                    navigateTo("builder")
                                },
                                onToggleFavorite = { p -> scope.launch { repository.toggleFavorite(p) } },
                                onToggleSaved = { p -> scope.launch { repository.toggleSaved(p) } },
                                onCopyPrompt = { p -> scope.launch { repository.incrementCopyCount(p) } },
                                snackbarHostState = snackbarHostState
                            )

                            "explore", "my_prompts", "favorites" -> ExploreScreen(
                                currentLanguage = currentLanguage,
                                prompts = when (route) {
                                    "my_prompts" -> prompts.filter { it.isSaved }
                                    "favorites" -> prompts.filter { it.isFavorite }
                                    else -> prompts
                                },
                                onToggleFavorite = { p -> scope.launch { repository.toggleFavorite(p) } },
                                onToggleSaved = { p -> scope.launch { repository.toggleSaved(p) } },
                                onCopyPrompt = { p -> scope.launch { repository.incrementCopyCount(p) } },
                                onUseInBuilder = { p ->
                                    builderInitialSubject = p.prompt
                                    navigateTo("builder")
                                },
                                onOptimizeWithAI = { p ->
                                    optimizerInitialPrompt = p.prompt
                                    navigateTo("optimizer")
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "builder" -> PromptBuilderScreen(
                                currentLanguage = currentLanguage,
                                initialSubject = builderInitialSubject,
                                onSaveToLibrary = { title, promptText, neg, isVideo ->
                                    scope.launch {
                                        repository.insertPrompt(
                                            PromptEntity(
                                                type = if (isVideo) "VIDEO" else "IMAGE",
                                                title = title,
                                                description = "ساخته‌شده در معمار پرامپت طرحی نو",
                                                prompt = promptText,
                                                negativePrompt = neg,
                                                author = "طرحی نو AI"
                                            )
                                        )
                                    }
                                },
                                onCreateProjectWithPrompt = { title, promptText ->
                                    scope.launch {
                                        repository.insertProject(
                                            ProjectEntity(
                                                title = title,
                                                description = promptText.take(60),
                                                promptCount = 1
                                            )
                                        )
                                    }
                                },
                                onNavigateToOptimizer = { promptText ->
                                    optimizerInitialPrompt = promptText
                                    navigateTo("optimizer")
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "optimizer" -> PromptOptimizerScreen(
                                currentLanguage = currentLanguage,
                                initialPrompt = optimizerInitialPrompt,
                                onSaveOptimizedPrompt = { title, promptText ->
                                    scope.launch {
                                        repository.insertPrompt(
                                            PromptEntity(
                                                type = "IMAGE",
                                                title = title,
                                                description = "پرامپت بهینه‌شده طرحی نو",
                                                prompt = promptText,
                                                author = "طرحی نو AI"
                                            )
                                        )
                                    }
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "chat", "ai_chat" -> AIChatScreen(
                                currentLanguage = currentLanguage,
                                conversations = conversations,
                                activeConversationId = activeConversationId,
                                messages = activeMessages,
                                isAiThinking = isAiThinking,
                                onSelectConversation = { id -> activeConversationId = id },
                                onNewConversation = {
                                    scope.launch {
                                        val newId = repository.createConversation("پروژه گفتگوی جدید")
                                        activeConversationId = newId
                                    }
                                },
                                onSendMessage = { text ->
                                    scope.launch {
                                        if (activeConversationId == 0L) {
                                            activeConversationId = repository.createConversation("گفتگو با هوش مصنوعی")
                                        }
                                        val convId = activeConversationId
                                        repository.insertMessage(convId, "user", text)
                                        repository.addHistory("گفتگو با دستیار", "CHAT", text.take(30))

                                        aiCoreState = AICoreState.THINKING
                                        isAiThinking = true

                                        val responseText = geminiService.generateCreativeResponse(text)

                                        aiCoreState = AICoreState.GENERATING
                                        delay(400)
                                        repository.insertMessage(convId, "model", responseText)

                                        aiCoreState = AICoreState.SUCCESS
                                        isAiThinking = false
                                        delay(1500)
                                        aiCoreState = AICoreState.IDLE
                                    }
                                },
                                onCreateProjectFromChat = { title, content ->
                                    scope.launch {
                                        repository.insertProject(
                                            ProjectEntity(
                                                title = title,
                                                description = content.take(60),
                                                status = "Active"
                                            )
                                        )
                                    }
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "agent" -> CreativeAgentScreen(
                                currentLanguage = currentLanguage,
                                onCreateProject = { title, desc, promptText ->
                                    scope.launch {
                                        repository.insertProject(
                                            ProjectEntity(
                                                title = title,
                                                description = desc,
                                                promptCount = 2,
                                                assetCount = 1
                                            )
                                        )
                                        repository.insertPrompt(
                                            PromptEntity(
                                                type = "IMAGE",
                                                title = title,
                                                description = desc,
                                                prompt = promptText,
                                                author = "دستیار خلاق طرحی نو"
                                            )
                                        )
                                    }
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "nava_studio" -> NavaStudioScreen(
                                currentLanguage = currentLanguage,
                                onSaveToProject = { title ->
                                    scope.launch {
                                        repository.insertProject(
                                            ProjectEntity(
                                                title = title,
                                                description = "ویرایش چندلایه‌ای ناو استودیو",
                                                assetCount = 4
                                            )
                                        )
                                    }
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "projects" -> ProjectsScreen(
                                currentLanguage = currentLanguage,
                                projects = projects,
                                onCreateProject = { title, desc, cat ->
                                    scope.launch {
                                        repository.insertProject(
                                            ProjectEntity(
                                                title = title,
                                                description = desc,
                                                category = cat
                                            )
                                        )
                                    }
                                },
                                onDeleteProject = { id -> scope.launch { repository.deleteProject(id) } },
                                snackbarHostState = snackbarHostState
                            )

                            "asset_vault" -> AssetVaultScreen(
                                currentLanguage = currentLanguage,
                                assets = assets,
                                onAddAsset = { title, type ->
                                    scope.launch {
                                        repository.insertAsset(
                                            AssetEntity(
                                                title = title,
                                                type = type,
                                                category = "استودیو",
                                                pathOrUrl = "asset_url"
                                            )
                                        )
                                    }
                                },
                                onDeleteAsset = { id -> scope.launch { repository.deleteAsset(id) } },
                                snackbarHostState = snackbarHostState
                            )

                            "history" -> HistoryScreen(
                                currentLanguage = currentLanguage,
                                historyList = historyList,
                                onClearAll = { scope.launch { repository.clearHistory() } },
                                onDeleteEntry = { id -> scope.launch { repository.deleteHistory(id) } },
                                snackbarHostState = snackbarHostState
                            )

                            "community" -> CommunityScreen(
                                currentLanguage = currentLanguage,
                                posts = communityPosts,
                                onToggleLike = { post -> scope.launch { repository.togglePostLike(post) } },
                                onPublishPost = { content, promptText ->
                                    scope.launch {
                                        repository.insertCommunityPost(content, promptText)
                                    }
                                },
                                snackbarHostState = snackbarHostState
                            )

                            "notifications" -> NotificationsScreen(
                                currentLanguage = currentLanguage,
                                notifications = notifications,
                                onMarkAllAsRead = { scope.launch { repository.markNotificationsRead() } },
                                snackbarHostState = snackbarHostState
                            )

                            "profile", "settings", "subscription", "about" -> ProfileAndSettingsScreen(
                                currentLanguage = currentLanguage,
                                onToggleLanguage = {
                                    currentLanguage = if (currentLanguage == AppLanguage.PERSIAN) AppLanguage.ENGLISH else AppLanguage.PERSIAN
                                },
                                onNavigate = { dest -> navigateTo(dest) },
                                snackbarHostState = snackbarHostState
                            )

                            "admin" -> AdminPanelScreen(
                                currentLanguage = currentLanguage,
                                promptCount = prompts.size,
                                projectCount = projects.size,
                                onAddPrompt = { p -> scope.launch { repository.insertPrompt(p) } },
                                snackbarHostState = snackbarHostState
                            )

                            else -> HomeScreen(
                                currentLanguage = currentLanguage,
                                prompts = prompts,
                                projects = projects,
                                onNavigate = { dest -> navigateTo(dest) },
                                onSelectPrompt = { p ->
                                    builderInitialSubject = p.prompt
                                    navigateTo("builder")
                                },
                                onToggleFavorite = { p -> scope.launch { repository.toggleFavorite(p) } },
                                onToggleSaved = { p -> scope.launch { repository.toggleSaved(p) } },
                                onCopyPrompt = { p -> scope.launch { repository.incrementCopyCount(p) } },
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            }
        }
    }
}
