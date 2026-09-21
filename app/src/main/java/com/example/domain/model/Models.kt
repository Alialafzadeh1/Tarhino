package com.example.domain.model

enum class AppLanguage(val code: String, val titleFa: String, val titleEn: String) {
    PERSIAN("fa", "فارسی", "Persian"),
    ENGLISH("en", "English", "English")
}

enum class PromptType(val titleFa: String, val titleEn: String) {
    IMAGE("تصویر", "Image"),
    VIDEO("ویدیو", "Video"),
    TEXT("متن", "Text"),
    CHAT("گفتگو", "Chat")
}

enum class OutputMode(val titleFa: String, val titleEn: String, val descriptionFa: String) {
    BASIC("پایه", "Basic", "خلاصه و تمیز برای تولید سریع"),
    PROFESSIONAL("حرفه‌ای", "Professional", "فرمول‌بندی متوازن برای استودیوهای طراحی"),
    CINEMATIC("سینمایی", "Cinematic", "فوکوس بر نورپردازی، لنز و عمق سینمایی"),
    ULTRA_DETAILED("فوق پرجزئیات", "Ultra Detailed", "بافت‌ها، میکروساختارها و رندرینگ 8K"),
    COMMERCIAL("تبلیغاتی", "Commercial", "مناسب بنرها، محصولات لوکس و کمپین‌ها"),
    CREATIVE("خلاقانه هنری", "Creative", "ایده‌های آوانگارد، سبک‌های تلفیقی و سورئال"),
    MODEL_OPTIMIZED("بهینه‌شده برای مدل", "Model Optimized", "سازگار دقیق با پارامترهای موتور هوش مصنوعی")
}

enum class ModelTarget(val displayName: String, val provider: String) {
    GEMINI("Gemini 3.5 Flash", "Google"),
    IMAGEN("Imagen 3 / Flash Image", "Google DeepMind"),
    VEO("Veo 3.1 Video", "Google DeepMind"),
    FLUX("FLUX.1 Pro", "Black Forest Labs"),
    MIDJOURNEY("Midjourney v6.1", "Midjourney")
}

enum class SubscriptionTier(
    val titleFa: String,
    val titleEn: String,
    val dailyAiLimit: Int,
    val maxProjects: Int,
    val advancedBuilder: Boolean,
    val priorityProcessing: Boolean
) {
    FREE("پایه / رایگان", "Free", 20, 5, false, false),
    PRO("حرفه‌ای", "Pro", 150, 25, true, true),
    VIP("سازمانی / VIP", "VIP Elite", 1000, 100, true, true)
}

enum class AICoreState {
    IDLE,
    THINKING,
    GENERATING,
    SUCCESS,
    ERROR
}

enum class HistoryGroup(val titleFa: String, val titleEn: String) {
    TODAY("امروز", "Today"),
    YESTERDAY("دیروز", "Yesterday"),
    THIS_WEEK("این هفته", "This Week"),
    EARLIER("گذشته", "Earlier")
}

enum class HistoryType(val titleFa: String, val titleEn: String) {
    CHAT("گفتگو", "Chat"),
    PROMPT("پرامپت", "Prompt"),
    IMAGE("تصویرسازی", "Image"),
    VIDEO("ویدیو", "Video"),
    STUDIO_EDIT("ویرایش استودیو", "Studio Edit"),
    PROJECT("پروژه", "Project")
}

enum class AssetCategory(val titleFa: String, val titleEn: String) {
    ALL("همه", "All"),
    IMAGES("تصاویر", "Images"),
    VIDEOS("ویدیوها", "Videos"),
    PROMPTS("پرامپت‌ها", "Prompts"),
    TEMPLATES("قالب‌ها", "Templates"),
    PRESETS("پریست‌ها", "Presets"),
    PROJECTS("پروژه‌ها", "Projects"),
    REFERENCES("منابع", "References")
}

enum class StudioTool(val titleFa: String, val titleEn: String) {
    ENHANCE("بهبود کیفیت", "Enhance"),
    RELIGHT("نورپردازی مجدد", "Relight"),
    RESTYLE("تغییر سبک", "Restyle"),
    UPSCALE("ارتقاء ابعاد", "Upscale"),
    BACKGROUND("حذف / تعویض پس‌زمینه", "Background"),
    TYPOGRAPHY("تایپوگرافی", "Typography"),
    COMPOSITION("ترکیب‌بندی", "Composition")
}

data class CanvasLayer(
    val id: String,
    val name: String,
    val type: String, // "IMAGE", "TEXT", "SHAPE"
    val content: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val isVisible: Boolean = true
)

data class CreativeAgentWorkflowResult(
    val title: String,
    val productAnalysis: String,
    val creativeDirection: String,
    val imagePrompt: String,
    val videoPrompt: String,
    val caption: String,
    val hashtags: List<String>,
    val suggestedAspectRatios: List<String>
)
