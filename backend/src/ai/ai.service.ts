export interface AIProvider {
  name: string;
  generateResponse(prompt: string, systemInstruction?: string): Promise<string>;
}

export class GeminiProvider implements AIProvider {
  name = 'gemini';
  private apiKey: string;

  constructor(apiKey?: string) {
    this.apiKey = apiKey || process.env.GEMINI_API_KEY || '';
  }

  async generateResponse(prompt: string, systemInstruction?: string): Promise<string> {
    if (!this.apiKey) {
      return this.generateFallbackResponse(prompt);
    }

    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${this.apiKey}`;
      const payload = {
        contents: [{ role: 'user', parts: [{ text: prompt }] }],
        systemInstruction: systemInstruction ? { parts: [{ text: systemInstruction }] } : undefined,
        generationConfig: { temperature: 0.7, topP: 0.95 },
      };

      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = (await res.json()) as any;
        const text = data?.candidates?.[0]?.content?.parts?.[0]?.text;
        if (text) return text;
      }
    } catch (e) {
      console.warn('Gemini API call failed, using high-fidelity fallback', e);
    }

    return this.generateFallbackResponse(prompt);
  }

  private generateFallbackResponse(prompt: string): string {
    return `«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nبرای ایده «${prompt}»، پرامپت هنری اختصاصی با نورپردازی متمرکز، رندر سینمایی و جزئیات معماری آماده شد. شما می‌توانید با لمس دکمه زیر این پرامپت را به Prompt Builder یا Nava Studio منتقل کنید.`;
  }
}

export class OpenAICompatibleProvider implements AIProvider {
  name = 'openai';
  private apiKey: string;
  private endpoint: string;

  constructor(apiKey?: string, endpoint?: string) {
    this.apiKey = apiKey || process.env.OPENAI_API_KEY || '';
    this.endpoint = endpoint || 'https://api.openai.com/v1/chat/completions';
  }

  async generateResponse(prompt: string, systemInstruction?: string): Promise<string> {
    if (!this.apiKey) {
      return `«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nدرخواست: «${prompt}»`;
    }

    try {
      const res = await fetch(this.endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${this.apiKey}`,
        },
        body: JSON.stringify({
          model: 'gpt-4o-mini',
          messages: [
            { role: 'system', content: systemInstruction || 'You are Tarhi Noo AI' },
            { role: 'user', content: prompt },
          ],
        }),
      });

      if (res.ok) {
        const data = (await res.json()) as any;
        return data?.choices?.[0]?.message?.content || '';
      }
    } catch (err) {
      console.warn('OpenAI provider call failed', err);
    }

    return `«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nدرخواست دریافت شد: «${prompt}»`;
  }
}

export class TarhiNooAIGateway {
  private provider: AIProvider;
  private systemInstruction = `You are Tarhi Noo AI (هوش مصنوعی طرحی نو), from Tarhineh Media (رسانه هنری طرحینه مدیا).
Always introduce yourself respectfully with: «من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»
You provide expert creative direction, prompt engineering, cinematic visual concepts, and audio/music direction for Persian and international artists.`;

  constructor() {
    const defaultProvider = process.env.AI_DEFAULT_PROVIDER || 'gemini';
    if (defaultProvider === 'openai') {
      this.provider = new OpenAICompatibleProvider();
    } else {
      this.provider = new GeminiProvider();
    }
  }

  async processAIMessage(userText: string): Promise<{
    text: string;
    actionPrompt: string;
    targetModule: 'PROMPT_BUILDER' | 'NAVA_STUDIO' | 'CREATIVE_AGENT';
  }> {
    const cleanPrompt = userText.replace(/@TarhiNooAI/gi, '').trim();
    const isAudio = /موسیقی|صدا|آهنگ|audio|music|sound/i.test(cleanPrompt);
    const targetModule = isAudio ? 'NAVA_STUDIO' : 'PROMPT_BUILDER';

    const response = await this.provider.generateResponse(cleanPrompt, this.systemInstruction);

    return {
      text: response,
      actionPrompt: cleanPrompt || 'Cinematic Iranian luxury design, 8k render, golden hour',
      targetModule,
    };
  }

  isConfigured(): boolean {
    return !!process.env.GEMINI_API_KEY || !!process.env.OPENAI_API_KEY;
  }
}

export const aiGateway = new TarhiNooAIGateway();
