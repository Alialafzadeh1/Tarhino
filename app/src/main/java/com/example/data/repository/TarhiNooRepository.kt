package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CommunityPostEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.PromptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TarhiNooRepository(private val db: AppDatabase) {

    val allPrompts: Flow<List<PromptEntity>> = db.promptDao().getAllPrompts()
    val favoritePrompts: Flow<List<PromptEntity>> = db.promptDao().getFavoritePrompts()
    val savedPrompts: Flow<List<PromptEntity>> = db.promptDao().getSavedPrompts()
    val allProjects: Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()
    val allConversations: Flow<List<ConversationEntity>> = db.chatDao().getAllConversations()
    val allAssets: Flow<List<AssetEntity>> = db.assetDao().getAllAssets()
    val allHistory: Flow<List<HistoryEntity>> = db.historyDao().getAllHistory()
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    val communityPosts: Flow<List<CommunityPostEntity>> = db.communityDao().getAllPosts()

    fun getPromptsByType(type: String): Flow<List<PromptEntity>> = db.promptDao().getPromptsByType(type)
    fun searchPrompts(query: String): Flow<List<PromptEntity>> = db.promptDao().searchPrompts(query)
    fun getMessagesForConversation(convId: Long): Flow<List<MessageEntity>> = db.chatDao().getMessagesForConversation(convId)

    suspend fun insertPrompt(prompt: PromptEntity): Long = withContext(Dispatchers.IO) {
        db.promptDao().insertPrompt(prompt)
    }

    suspend fun updatePrompt(prompt: PromptEntity) = withContext(Dispatchers.IO) {
        db.promptDao().updatePrompt(prompt)
    }

    suspend fun deletePrompt(id: Long) = withContext(Dispatchers.IO) {
        db.promptDao().deletePromptById(id)
    }

    suspend fun toggleFavorite(prompt: PromptEntity) = withContext(Dispatchers.IO) {
        val updated = prompt.copy(
            isFavorite = !prompt.isFavorite,
            favoriteCount = if (!prompt.isFavorite) prompt.favoriteCount + 1 else (prompt.favoriteCount - 1).coerceAtLeast(0)
        )
        db.promptDao().updatePrompt(updated)
    }

    suspend fun toggleSaved(prompt: PromptEntity) = withContext(Dispatchers.IO) {
        val updated = prompt.copy(isSaved = !prompt.isSaved)
        db.promptDao().updatePrompt(updated)
    }

    suspend fun incrementCopyCount(prompt: PromptEntity) = withContext(Dispatchers.IO) {
        val updated = prompt.copy(copyCount = prompt.copyCount + 1)
        db.promptDao().updatePrompt(updated)
        // Record in history
        db.historyDao().insertHistory(
            HistoryEntity(
                title = prompt.title,
                type = "PROMPT",
                detail = "کپی پرامپت / Prompt copied"
            )
        )
    }

    suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        val id = db.projectDao().insertProject(project)
        db.historyDao().insertHistory(
            HistoryEntity(
                title = project.title,
                type = "PROJECT",
                detail = "ساخت پروژه جدید / Created new project"
            )
        )
        id
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        db.projectDao().updateProject(project)
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        db.projectDao().deleteProjectById(id)
    }

    suspend fun createConversation(title: String): Long = withContext(Dispatchers.IO) {
        val id = db.chatDao().insertConversation(
            ConversationEntity(title = title)
        )
        // Initial intro message from Tarhi Noo AI
        db.chatDao().insertMessage(
            MessageEntity(
                conversationId = id,
                role = "model",
                content = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nامروز روی چه پروژه خلاقانه‌ای کار می‌کنیم؟ ساخت پرامپت، ایده تبلیغاتی، تحلیل هویت بصری، یا بازطراحی استودیو؟"
            )
        )
        id
    }

    suspend fun insertMessage(convId: Long, role: String, content: String, attachmentUri: String = "", isAgent: Boolean = false): Long = withContext(Dispatchers.IO) {
        val msgId = db.chatDao().insertMessage(
            MessageEntity(
                conversationId = convId,
                role = role,
                content = content,
                attachmentUri = attachmentUri,
                isAgent = isAgent
            )
        )
        // Update conversation timestamp
        val conv = db.chatDao().getConversationById(convId)
        if (conv != null) {
            db.chatDao().updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
        msgId
    }

    suspend fun deleteConversation(id: Long) = withContext(Dispatchers.IO) {
        db.chatDao().clearMessagesForConversation(id)
        db.chatDao().deleteConversation(id)
    }

    suspend fun insertAsset(asset: AssetEntity): Long = withContext(Dispatchers.IO) {
        db.assetDao().insertAsset(asset)
    }

    suspend fun deleteAsset(id: Long) = withContext(Dispatchers.IO) {
        db.assetDao().deleteAsset(id)
    }

    suspend fun addHistory(title: String, type: String, detail: String) = withContext(Dispatchers.IO) {
        db.historyDao().insertHistory(HistoryEntity(title = title, type = type, detail = detail))
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        db.historyDao().clearAllHistory()
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        db.historyDao().deleteHistory(id)
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        db.notificationDao().markAllAsRead()
    }

    suspend fun togglePostLike(post: CommunityPostEntity) = withContext(Dispatchers.IO) {
        val newLiked = !post.isLiked
        val newCount = if (newLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        db.communityDao().updateLike(post.id, newLiked, newCount)
    }

    suspend fun insertCommunityPost(content: String, promptText: String) = withContext(Dispatchers.IO) {
        db.communityDao().insertPost(
            CommunityPostEntity(
                authorName = "کاربر طرحی نو",
                authorHandle = "@creative_user",
                content = content,
                promptText = promptText
            )
        )
    }

    suspend fun initSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        if (db.promptDao().getCount() > 0) return@withContext

        // Seed rich Prompt Library
        val initialPrompts = listOf(
            PromptEntity(
                type = "IMAGE",
                title = "عطر لوکس سلطنتی با شیشه طلایی و کهربا",
                description = "رندر محصول لوکس با بافت شیشه تراش‌خورده، بازتاب‌های طلایی و قطرات شبنم",
                prompt = "A luxury perfume bottle with intricate Persian filigree geometric gold patterns, glowing amber liquid inside, faceted crystal cap, resting on dark polished black marble, dramatic rim lighting, golden hour caustic reflections, macro photography, 8k resolution, editorial fashion campaign.",
                negativePrompt = "blurry, low quality, cheap glass, distorted, scratches, plastic, cartoon",
                language = "fa",
                categoryId = "commercial",
                tags = "عطر, لوکس, طلا, رندر محصول, شیشه, استودیو",
                style = "Luxury",
                lighting = "Rim",
                camera = "Macro",
                lens = "85mm",
                composition = "Portrait",
                environment = "Luxury Interior",
                material = "Glass, Gold",
                color = "Gold, Deep Amber, Black",
                mood = "Sophisticated, Opulent",
                quality = "Ultra 8K, Masterpiece",
                author = "طرحینه مدیا / Tarhineh Media",
                isFeatured = true,
                viewCount = 1420,
                copyCount = 312,
                favoriteCount = 289
            ),
            PromptEntity(
                type = "IMAGE",
                title = "تهران آینده‌نگر سایبرپانک ۲۰۸۸ با برج میلاد نئونی",
                description = "چشم‌انداز بارانی شب با تابلوهای نستعلیق هولوگرافیک و ماشین‌های پرنده",
                prompt = "Futuristic Cyberpunk Tehran in year 2088, towering neon Milad Tower in the misty background, holographic Persian calligraphy billboards hovering over wet asphalt streets, flying retro-futuristic cars with light trails, dense volumetric fog, cinematic lighting, photorealistic 8k, Unreal Engine 5 render.",
                negativePrompt = "blurry, lowres, flat lighting, daytime, cartoon, low polygon",
                language = "fa",
                categoryId = "cyberpunk",
                tags = "تهران, سایبرپانک, آینده, برج میلاد, نئون, نستعلیق",
                style = "Cyberpunk",
                lighting = "Neon",
                camera = "Wide Shot",
                lens = "24mm",
                composition = "Dutch Angle",
                environment = "Cyberpunk City",
                material = "Chrome, Neon, Wet Asphalt",
                color = "Cyan, Deep Magenta, Emerald",
                mood = "Moody, Technological",
                quality = "Cinematic 8K",
                author = "علی رضایی / Ali Re",
                isFeatured = true,
                viewCount = 980,
                copyCount = 210,
                favoriteCount = 195
            ),
            PromptEntity(
                type = "VIDEO",
                title = "حرکت اوربیتال سینمایی در کاخ صفوی عالی‌قاپو",
                description = "چرخش نرم دوربین حول مقرنس‌های طلایی و لاجوردی با نور خورشید زاویه‌دار",
                prompt = "Slow cinematic orbital dolly camera movement inside the historic Ali Qapu palace in Isfahan, looking upward at ornate acoustic muqarnas ceiling, golden dust motes floating in diagonal sunbeams, intricate lapis lazuli and gold leaf tilework, temporal consistency, 60fps, 4k ultra hd.",
                negativePrompt = "shaky cam, jitter, sudden jump cuts, modern artifacts, blur",
                language = "fa",
                categoryId = "cinematic",
                tags = "اصفهان, عالی‌قاپو, مقرنس, ویدیو سینمایی, میراث فرهنگی, نور خورشید",
                style = "Cinematic",
                lighting = "Golden Hour",
                camera = "Orbit",
                lens = "35mm",
                composition = "Overhead",
                environment = "Architecture",
                material = "Gold, Ceramic Tile, Wood",
                color = "Persian Blue, Gold, Terracotta",
                mood = "Transcendent, Majestic",
                quality = "4K 60fps",
                author = "رسانه هنری طرحینه",
                isFeatured = true,
                viewCount = 1650,
                copyCount = 420,
                favoriteCount = 380
            ),
            PromptEntity(
                type = "IMAGE",
                title = "پوستر مینیمال مفهومی عاشورا با خوشنویسی سرخ",
                description = "طراحی مدرن با کادربندی خلوت، خط کوفی زاویه‌دار و پس‌زمینه زغال خاموش",
                prompt = "Minimalist contemporary conceptual Ashura poster design, dramatic bold red stylized Kufic Arabic calligraphy forming an abstract crimson horizon, deep textured charcoal black paper background, negative space composition, fine art gallery print aesthetic, award winning graphic design.",
                negativePrompt = "cluttered, noisy, realistic faces, tacky 3d effects, neon",
                language = "fa",
                categoryId = "artistic",
                tags = "پوستر, عاشورا, خوشنویسی, مینیمال, سرخ, مفهومی",
                style = "Minimal",
                lighting = "Studio",
                camera = "Medium Shot",
                lens = "50mm",
                composition = "Close Up",
                environment = "Studio",
                material = "Paper, Ink",
                color = "Crimson Red, Charcoal, Bone White",
                mood = "Solemn, Spiritual",
                quality = "Masterpiece Graphic Design",
                author = "استودیو هنری صریر",
                isFeatured = false,
                viewCount = 740,
                copyCount = 148,
                favoriteCount = 162
            ),
            PromptEntity(
                type = "TEXT",
                title = "سناریوی معرفی استارتاپ هوش مصنوعی در ۴۰ ثانیه",
                description = "متن گویندگی انگیزشی با ساختار هوک، چالش و راه‌حل خلاقانه",
                prompt = "Write a compelling 40-second Persian narrative voiceover script for an avant-garde AI design platform startup launch, hook the audience in first 3 seconds with creative empowerment theme, energetic cadence, concluding with punchy brand slogan.",
                negativePrompt = "",
                language = "fa",
                categoryId = "commercial",
                tags = "سناریو, تبلیغات, گویندگی, استارتاپ, خلاقیت",
                style = "Commercial",
                lighting = "Studio",
                camera = "Portrait",
                lens = "50mm",
                composition = "Medium Shot",
                environment = "Studio",
                material = "Digital",
                color = "Emerald, Gold",
                mood = "Inspiring, Confident",
                quality = "Professional Scriptwriting",
                author = "طرحی نو AI",
                isFeatured = false,
                viewCount = 530,
                copyCount = 95,
                favoriteCount = 88
            )
        )
        db.promptDao().insertAll(initialPrompts)

        // Seed initial project
        db.projectDao().insertProject(
            ProjectEntity(
                title = "کمپین تبلیغاتی عطر ماهور",
                description = "هویت بصری، پرامپت‌های استودیویی، تیزر و سناریوی اینستاگرامی عطر لوکس",
                category = "Commercial Brand",
                tags = "عطر, لوکس, تیزر, رندرینگ",
                status = "Active",
                assetCount = 8,
                promptCount = 4
            )
        )

        // Seed initial notifications
        val initialNotifications = listOf(
            NotificationEntity(
                title = "هوش مصنوعی طرحی نو آماده است",
                message = "موتور بهینه‌سازی پرامپت و ناو استودیو با موفقیت بارگذاری شد.",
                type = "SYSTEM"
            ),
            NotificationEntity(
                title = "به جامعه خلاق طرحی نو خوش آمدید",
                message = "هزاران پرامپت حرفه‌ای سینمایی و تبلیغاتی در دسترس شماست.",
                type = "AI_COMPLETED"
            )
        )
        initialNotifications.forEach { db.notificationDao().insertNotification(it) }

        // Seed community posts
        val initialPosts = listOf(
            CommunityPostEntity(
                authorName = "امیرحسین کیانی",
                authorHandle = "@amir_design",
                content = "نتیجه پرامپت معماری پارامتریک در ترکیب با خطوط کوفی. نورپردازی ساعت طلایی معجزه کرد!",
                promptText = "Parametric architectural pavilion with parametric timber slats, filtered sunlight rays, photorealistic 8k.",
                likesCount = 84,
                commentsCount = 12
            ),
            CommunityPostEntity(
                authorName = "مریم طاهری",
                authorHandle = "@maryam_studio",
                content = "طراحی تیزر ۱۲ ثانیه‌ای برای برند جواهرات با ترکیب پرامپت معمار و ناو استودیو.",
                promptText = "Macro slow motion shot of emerald diamond ring submerged in sparkling mineral water, caustic light refraction.",
                likesCount = 142,
                commentsCount = 27
            )
        )
        initialPosts.forEach { db.communityDao().insertPost(it) }
    }
}
