package com.example.ai

import com.example.domain.model.CreativeAgentWorkflowResult

object CreativeAgentEngine {

    fun executeWorkflow(userBrief: String): CreativeAgentWorkflowResult {
        val lower = userBrief.lowercase()
        return when {
            lower.contains("عطر") || lower.contains("لوکس") || lower.contains("perfume") -> {
                CreativeAgentWorkflowResult(
                    title = "کمپین اختصاصی عطر سلطنتی",
                    productAnalysis = "محصول در رده کالاهای لوکس (Haute Parfumerie). نیازمند تاکید بر خلوص شیشه، جلوه کهربایی و حس شکوه.",
                    creativeDirection = "رویکرد دارک لاکچری با نورهای حاشیه‌ای طلایی، بک‌گراند آبسیدین صیقلی و انعکاس‌های ملایم مایع عطر.",
                    imagePrompt = "A luxury haute-parfumerie perfume bottle with intricate Persian gold filigree accents, glowing amber liquid inside, faceted crystal cap, resting on polished black obsidian, dramatic rim lighting, golden hour caustic reflections, macro 85mm photography, 8k resolution, Vogue editorial standard.",
                    videoPrompt = "Slow orbital dolly shot around a glowing luxury perfume bottle, floating golden dust motes, subtle caustic light patterns moving across black marble floor, 60fps, 4K, cinematic temporal consistency.",
                    caption = "شکوهی پایدار در قطره‌ای از رایحه اصالت. با عطر اختصاصی، هر لحظه جلوه‌ای از تمایز است. ✨ طرحی نو از رسانه هنری طرحینه مدیا.",
                    hashtags = listOf("#عطر_لوکس", "#طرحی_نو", "#طرحینه_مدیا", "#هوش_مصنوعی_خلاق", "#کمپین_تبلیغاتی"),
                    suggestedAspectRatios = listOf("9:16 (Story/Reel)", "1:1 (Square)", "4:5 (Portrait)", "16:9 (Teaser)")
                )
            }
            lower.contains("محرم") || lower.contains("عاشورا") || lower.contains("پوستر") || lower.contains("هیئت") -> {
                CreativeAgentWorkflowResult(
                    title = "پوستر مفهومی عاشورایی",
                    productAnalysis = "طراحی پوستر مذهبی نیازمند حفظ معنویت اصیل، تایپوگرافی قدرتمند و پرهیز از شلوغی بصری کلیشه‌ای است.",
                    creativeDirection = "مینیمالیسم مدرن، کالیگرافی سرخ کوفی در مرکز، بافت کاغذ ذغالی دست‌ساز، فضای منفی باز برای درج متن مراسم.",
                    imagePrompt = "Minimalist contemporary conceptual Ashura exhibition poster, stylized red Kufic calligraphy forming an abstract crimson horizon, deep textured charcoal black paper background, dramatic negative space composition, fine art gallery print aesthetic, award winning graphic design, 8k.",
                    videoPrompt = "Atmospheric slow motion zoom-out from an abstract crimson calligraphy ink stroke blooming on textured black paper, gentle ambient wind effect, 4K resolution.",
                    caption = "باز این چه شورش است که در خلق عالم است... پوستر رسمی مراسم، طراحی‌شده با هوش مصنوعی طرحی نو. التماس دعا.",
                    hashtags = listOf("#پوستر_محرم", "#عاشورا", "#طرحی_نو", "#خوشنویسی", "#طرحینه"),
                    suggestedAspectRatios = listOf("4:5 (Poster Feed)", "9:16 (Story)", "A3 / Print")
                )
            }
            lower.contains("ویدیو") || lower.contains("تیزر") || lower.contains("video") -> {
                CreativeAgentWorkflowResult(
                    title = "تیزر ویدیویی سینمایی استودیو",
                    productAnalysis = "سناریوی پویا با تمرکز بر ریتم تصویری، پایداری زمانی فریم‌ها و افکت‌های نوری دراماتیک.",
                    creativeDirection = "حرکت پیوسته دوربین (Fluid Tracking)، رنگ‌بندی سینمایی Teal & Orange، عمق میدان باریک و بوکه آنامورفیک.",
                    imagePrompt = "Cinematic film still, ultra-realistic visual concept for $userBrief, atmospheric haze, moody studio rim lighting, 35mm anamorphic lens, 8k.",
                    videoPrompt = "Cinematic dynamic 4K video sequence: $userBrief. Smooth forward tracking camera movement at 60fps, natural organic lighting changes, stable temporal consistency, photorealistic render.",
                    caption = "خلق ایده‌های ویدیویی با بالاترین استانداردهای استودیویی در چند ثانیه. با هوش مصنوعی طرحی نو بساز...",
                    hashtags = listOf("#تیزر_سینمایی", "#هوش_مصنوعی", "#Veo", "#طرحی_نو"),
                    suggestedAspectRatios = listOf("16:9 (Landscape)", "9:16 (Vertical Video)")
                )
            }
            else -> {
                CreativeAgentWorkflowResult(
                    title = "کمپین خلاقانه طرحی نو: $userBrief",
                    productAnalysis = "ایده اولیه به دقت ارزیابی شد. المان‌های کلیدی استخراج و با استانداردهای بصری استودیو همگام‌سازی شدند.",
                    creativeDirection = "ترکیب‌بندی مدرن، توازن رنگ‌های طلایی و تیره، فوکوس دقیق بر سوژه اصلی و کیفیت رندرینگ 8K.",
                    imagePrompt = "Masterpiece creative artwork based on $userBrief, luxury cinematic lighting, sophisticated color harmony, intricate details, 8k photorealistic resolution, award winning studio aesthetic.",
                    videoPrompt = "Slow motion smooth camera orbit showcasing $userBrief, volumetric lighting, natural depth of field, 60fps, 4K resolution.",
                    caption = "خلق شده با هوش مصنوعی طرحی نو — رسانه هنری طرحینه مدیا. هر آنچه تصور کنید، در دستان شماست. 💫",
                    hashtags = listOf("#طرحی_نو", "#خلاقیت_دیجیتال", "#طرحینه_مدیا", "#هنر_مدرن"),
                    suggestedAspectRatios = listOf("1:1", "16:9", "9:16")
                )
            }
        }
    }
}
