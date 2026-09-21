package com.example.ai

import com.example.domain.model.ModelTarget
import com.example.domain.model.OutputMode

data class PromptBuilderState(
    val isVideo: Boolean = false,
    val subject: String = "",
    val style: String = "Cinematic",
    val composition: String = "Medium Shot",
    val camera: String = "Studio DSLR",
    val lens: String = "50mm",
    val lighting: String = "Cinematic",
    val environment: String = "Studio",
    val materials: List<String> = emptyList(),
    val colors: String = "",
    val mood: String = "",
    val details: String = "",
    val quality: String = "8K, Photorealistic",
    val customNegativePrompt: String = "",
    val outputFormat: String = "16:9",
    // Video-specific
    val cameraMovement: String = "Orbit",
    val subjectMovement: String = "Subtle Organic Motion",
    val motionSpeed: String = "Slow Motion (60fps)",
    val durationSeconds: Int = 8,
    val frameRate: String = "60fps",
    val temporalConsistency: Boolean = true,
    val cinematicDirection: String = "Anamorphic bokeh, steadycam glide"
)

object PromptEngine {

    val availableStyles = listOf(
        "Cinematic", "Photorealistic", "Editorial", "Luxury", "Minimal",
        "Cyberpunk", "Fantasy", "Documentary", "Fashion", "Commercial",
        "Architectural", "3D", "Surreal", "Anime", "Artistic"
    )

    val availableLighting = listOf(
        "Cinematic", "Studio", "Rim", "Golden Hour", "Neon", "Volumetric",
        "Low Key", "High Key", "Moody", "Natural", "Soft", "Hard"
    )

    val availableComposition = listOf(
        "Medium Shot", "Close Up", "Wide Shot", "Macro", "Portrait",
        "Top Down", "Low Angle", "High Angle", "Dutch Angle", "Overhead"
    )

    val availableLenses = listOf(
        "24mm", "35mm", "50mm", "85mm", "105mm", "135mm", "Macro", "Wide Angle", "Telephoto"
    )

    val availableEnvironments = listOf(
        "Studio", "Luxury Interior", "Street", "Urban", "Nature",
        "Cyberpunk City", "Architecture", "Desert", "Forest", "Beach", "Abstract"
    )

    val availableMaterials = listOf(
        "Glass", "Metal", "Gold", "Silver", "Chrome", "Silk",
        "Leather", "Wood", "Stone", "Marble", "Liquid", "Crystal"
    )

    val availableCameraMovements = listOf(
        "Static", "Slow Dolly Forward", "Orbit", "Tracking Shot",
        "Crane Up", "Handheld Glide", "Cinematic Pan", "Dutch Roll"
    )

    fun buildPrompt(state: PromptBuilderState): Pair<String, String> {
        val subjectClean = if (state.subject.isBlank()) "An avant-garde luxury creative concept" else state.subject.trim()

        val tokens = mutableListOf<String>()

        // Subject & core
        tokens.add(subjectClean)

        // Style
        if (state.style.isNotBlank()) {
            tokens.add("${state.style.lowercase()} aesthetic")
        }

        // Environment
        if (state.environment.isNotBlank() && state.environment != "Studio") {
            tokens.add("set in an atmospheric ${state.environment.lowercase()}")
        } else if (state.environment == "Studio") {
            tokens.add("in a professional studio setting")
        }

        // Materials
        if (state.materials.isNotEmpty()) {
            val mats = state.materials.joinToString(", ") { it.lowercase() }
            tokens.add("crafted with opulent $mats elements")
        }

        // Composition, Camera & Lens
        val compTokens = mutableListOf<String>()
        if (state.composition.isNotBlank()) compTokens.add(state.composition)
        if (state.lens.isNotBlank()) compTokens.add(state.lens)
        if (state.camera.isNotBlank()) compTokens.add(state.camera)
        if (compTokens.isNotEmpty()) {
            tokens.add(compTokens.joinToString(", "))
        }

        // Lighting
        if (state.lighting.isNotBlank()) {
            tokens.add("${state.lighting.lowercase()} lighting with subtle caustics and highlights")
        }

        // Color & Mood
        if (state.colors.isNotBlank()) {
            tokens.add("color palette of ${state.colors.trim()}")
        }
        if (state.mood.isNotBlank()) {
            tokens.add("${state.mood.trim()} mood")
        }

        // Details
        if (state.details.isNotBlank()) {
            tokens.add(state.details.trim())
        }

        // Quality
        tokens.add(state.quality)

        // Video specific additions
        if (state.isVideo) {
            tokens.add("camera movement: ${state.cameraMovement}")
            tokens.add("motion: ${state.subjectMovement} (${state.motionSpeed})")
            if (state.temporalConsistency) tokens.add("temporal consistency, fluid natural transitions")
            if (state.cinematicDirection.isNotBlank()) tokens.add(state.cinematicDirection)
            tokens.add("${state.frameRate}, ${state.durationSeconds}s duration")
        }

        val finalPrompt = tokens.joinToString(", ")

        // Negative prompt generation
        val baseNegative = mutableListOf(
            "blurry", "low resolution", "low quality", "distorted",
            "oversaturated", "amateur", "compression artifacts"
        )
        if (state.isVideo) {
            baseNegative.addAll(listOf("jitter", "jump cuts", "shaky camera", "frame skipping", "morphing faces"))
        }
        if (state.customNegativePrompt.isNotBlank()) {
            baseNegative.add(state.customNegativePrompt.trim())
        }

        val finalNegative = baseNegative.joinToString(", ")
        return Pair(finalPrompt, finalNegative)
    }

    fun optimizePrompt(
        rawPrompt: String,
        mode: OutputMode,
        targetModel: ModelTarget
    ): String {
        val clean = rawPrompt.trim()
        if (clean.isBlank()) return "Please provide a base prompt to optimize."

        return when (mode) {
            OutputMode.BASIC -> {
                "Clean and direct: A crisp, focused subject of '$clean', balanced natural lighting, clean composition, high fidelity."
            }
            OutputMode.PROFESSIONAL -> {
                "Editorial masterpiece of $clean, professional studio lighting, 85mm prime lens, shallow depth of field, refined textures, harmonious color grading, publication quality 8k."
            }
            OutputMode.CINEMATIC -> {
                "Cinematic wide-screen still of $clean, dramatic anamorphic lens flare, moody volumetric lighting, deep shadows, atmospheric dust particles, 35mm film grain, directed by a world-class cinematographer, 8k."
            }
            OutputMode.ULTRA_DETAILED -> {
                "Hyper-detailed macro rendering of $clean, intricate surface textures, micro-reflections, subsurface scattering, tactile materials, ray-traced ambient occlusion, Unreal Engine 5 render, 8k resolution."
            }
            OutputMode.COMMERCIAL -> {
                "High-end luxury commercial advertisement for $clean, pristine studio product photography, elegant gold and glass accents, premium rim lighting, clean negative space for typography, Vogue and GQ editorial standard."
            }
            OutputMode.CREATIVE -> {
                "Avant-garde surrealist interpretation of $clean, fusion of contemporary Iranian geometric art and cybernetic futurism, ethereal glow, transcendent color harmony, conceptual art gallery award winner."
            }
            OutputMode.MODEL_OPTIMIZED -> {
                when (targetModel) {
                    ModelTarget.GEMINI -> {
                        "High fidelity visual description for Gemini: Detailed scene containing $clean, precise spatial arrangement, natural lighting contrast, clear material properties, highly coherent composition."
                    }
                    ModelTarget.IMAGEN -> {
                        "Photorealistic digital image, award winning photography, $clean, natural ray-traced shadows, 8k, sharp focus on subject, perfectly calibrated exposure."
                    }
                    ModelTarget.VEO -> {
                        "Cinematic 4K video sequence: $clean. Camera executes a smooth tracking motion at 60fps, natural temporal consistency, steady camera glide, fluid organic movements, photorealistic."
                    }
                    ModelTarget.FLUX -> {
                        "photo of $clean, raw aesthetic, hyper-realistic skin and surface textures, candid lighting, natural imperfections, shot on Hasselblad H6D-100c."
                    }
                    ModelTarget.MIDJOURNEY -> {
                        "$clean, cinematic lighting, editorial photography, highly detailed, shot on 35mm lens, f/1.4, subtle grain --ar 16:9 --v 6.1 --style raw"
                    }
                }
            }
        }
    }
}
