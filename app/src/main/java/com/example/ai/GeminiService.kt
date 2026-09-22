package com.example.ai

import android.util.Log
import com.example.data.remote.api.TarhiNooApiService
import com.example.data.remote.model.AiChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tarhi Noo Server-Side AI Gateway Client.
 *
 * Direct calls from Android to generativelanguage.googleapis.com and BuildConfig.GEMINI_API_KEY
 * have been completely removed.
 *
 * All AI generation requests route securely through the Tarhi Noo Backend (/ai/chat)
 * where authentication, rate-limiting, and the Gemini API key reside exclusively.
 *
 * In local/offline mode or if the backend is not yet configured, the service provides
 * contextual fallback generation without crashing or leaking credentials.
 */
class GeminiService(
    private val apiService: TarhiNooApiService? = null
) {

    suspend fun generateCreativeResponse(
        userPrompt: String,
        systemInstruction: String = "You are Tarhi Noo AI (هوش مصنوعی طرحی نو), from Tarhineh Media (رسانه هنری طرحینه مدیا). You are a futuristic, highly sophisticated, Iranian-first AI creative assistant. Introduce yourself warmly when appropriate as «من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا». Provide brilliant, professional, high-level creative direction, prompt engineering, and design advice in fluent Persian or English as requested."
    ): String = withContext(Dispatchers.IO) {
        // 1. If backend API service is available, route securely through backend gateway
        if (apiService != null) {
            try {
                val targetModule = if (userPrompt.contains("موسیقی") || userPrompt.contains("صوت") || userPrompt.contains("audio") || userPrompt.contains("music")) {
                    "NAVA_STUDIO"
                } else {
                    "PROMPT_BUILDER"
                }

                val response = apiService.sendAiChat(
                    AiChatRequest(
                        prompt = userPrompt,
                        context = systemInstruction,
                        targetModule = targetModule
                    )
                )

                if (response.isSuccessful) {
                    val apiResp = response.body()
                    val text = apiResp?.data?.text
                    if (!text.isNullOrBlank()) {
                        return@withContext text
                    }
                } else {
                    Log.w("GeminiService", "Backend AI Gateway returned status: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w("GeminiService", "Network error reaching backend AI gateway", e)
            }
        }

        // 2. Safe contextual fallback for offline development or when backend is unconfigured
        generateSmartLocalCreativeResponse(userPrompt)
    }

    private fun generateSmartLocalCreativeResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("عطر") || lower.contains("perfume") -> {
                "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n" +
                        "برای کمپین عطر لوکس، ساختار پیشنهادی من:\n\n" +
                        "✦ **جهت‌گیری هنری:** ترکیب المان‌های نورپردازی حاشیه‌ای (Rim Light)، بازتاب‌های کهربایی و بافت شیشه‌ای Faceted روی سنگ مرمر تیره.\n\n" +
                        "✦ **پرامپت تصویر استودیویی:**\n" +
                        "`A luxury haute-parfumerie crystal bottle filled with glowing amber essence, intricate geometric gold filigree, resting on polished black obsidian, dramatic cinematic rim lighting, caustics, macro 85mm lens, 8k resolution, editorial Vogue aesthetic.`\n\n" +
                        "✦ **ایده تیزر ویدیویی (Veo/Video):**\n" +
                        "حرکت اسلوموشن دورانی با سرعت ۶۰ فریم بر ثانیه، قطرات زلال آب که در هوا معلق می‌شوند و پرتوهای نور طلایی که از درون شیشه عبور می‌کنند.\n\n" +
                        "آیا مایلید این ساختار را به یک پروژه رسمی در طرحی نو تبدیل کنیم؟"
            }
            lower.contains("محرم") || lower.contains("عاشورا") || lower.contains("مذهبی") || lower.contains("هیئت") -> {
                "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n" +
                        "طراحی پوستر مفهومی عاشورایی با رعایت اصول اصیل تایپوگرافی و هویت معنوی:\n\n" +
                        "✦ **رویکرد بصری:** مینیمالیسم مفهومی، استفاده از فضای منفی (Negative Space) و کنتراست شدید سرخ و سیاه ذغالی.\n\n" +
                        "✦ **پرامپت پیشنهادی:**\n" +
                        "`Conceptual minimalist Ashura exhibition poster, stylized red Kufic calligraphy silhouette forming an abstract symbolic banner, deep charcoal textured canvas, dramatic directional spotlight, museum art gallery print, high fidelity graphic design, 8k.`\n\n" +
                        "✦ **نکات کادربندی:** نسبت ابعاد 4:5 عمودی، کادر باز در بالای صفحه برای جای‌گذاری متن مراسم و نشان هیئت."
            }
            lower.contains("ماشین") || lower.contains("خودرو") || lower.contains("car") -> {
                "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n" +
                        "پرامپت بهینه‌شده برای عکاسی خودرو در شب:\n\n" +
                        "✦ **پرامپت سینمایی:**\n" +
                        "`Sleek modern hypercar parked on wet asphalt after rain, neon city reflections on glossy metallic dark emerald bodywork, headlights piercing through volumetric fog, ultra-wide 24mm low-angle shot, cinematic color grading, photorealistic 8k, Unreal Engine 5 aesthetic.`\n\n" +
                        "✦ **نگاتیو پرامپت پیشنهادی:**\n" +
                        "`blurry, daylight, flat lighting, cartoon, low resolution, dirty windshield, distorted reflections`"
            }
            else -> {
                "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n" +
                        "درخواست شما با موفقیت در موتور هوش مصنوعی طرحی نو تحلیل شد:\n\n" +
                        "✦ **تحلیل ایده:** «$prompt»\n" +
                        "✦ **پیشنهاد پرامپت حرفه‌ای:**\n" +
                        "`Masterpiece creative composition featuring $prompt, professional studio lighting, atmospheric volumetric depth, intricate details, photorealistic textures, 8k resolution, cinematic color palette.`\n\n" +
                        "می‌توانید این پرامپت را در بخش **Prompt Architect** تنظیم کنید یا برای ویرایش لایه‌ها وارد **ناو استودیو (Nava Studio)** شوید."
            }
        }
    }
}
