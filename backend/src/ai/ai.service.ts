import { env } from '../config/env';

export interface AIProvider {
  name: string;
  generateResponse(prompt: string, systemInstruction?: string): Promise<string>;
}

export class GeminiProvider implements AIProvider {
  name = 'gemini';
  private apiKey: string;

  constructor(apiKey?: string) {
    this.apiKey = apiKey || env.GEMINI_API_KEY;
  }

  async generateResponse(prompt: string, systemInstruction?: string): Promise<string> {
    if (!this.apiKey) {
      if (env.NODE_ENV === 'production') {
        throw new Error('AI_PROVIDER_UNAVAILABLE: GEMINI_API_KEY is not configured on production backend.');
      }
      return this.generateDevFallbackResponse(prompt);
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
      } else {
        const errText = await res.text();
        console.error(`[GeminiProvider] API Error (${res.status}):`, errText);
        if (env.NODE_ENV === 'production') {
          throw new Error(`AI_PROVIDER_UNAVAILABLE: Gemini API returned status ${res.status}`);
        }
      }
    } catch (e: any) {
      console.error('[GeminiProvider] Network/call failure:', e.message);
      if (env.NODE_ENV === 'production') {
        throw new Error('AI_PROVIDER_UNAVAILABLE: Could not reach Gemini AI API.');
      }
    }

    return this.generateDevFallbackResponse(prompt);
  }

  private generateDevFallbackResponse(prompt: string): string {
    return `«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n[حالت توسعه] برای ایده «${prompt}»، پرامپت هنری و ساختار بصری با نورپردازی متمرکز آماده شد.`;
  }
}

export class OpenAICompatibleProvider implements AIProvider {
  name = 'openai';
  private apiKey: string;
  private endpoint: string;

  constructor(apiKey?: string, endpoint?: string) {
    this.apiKey = apiKey || env.OPENAI_API_KEY || '';
    this.endpoint = endpoint || 'https://api.openai.com/v1/chat/completions';
  }

  async generateResponse(prompt: string, systemInstruction?: string): Promise<string> {
    if (!this.apiKey) {
      if (env.NODE_ENV === 'production') {
        throw new Error('AI_PROVIDER_UNAVAILABLE: OPENAI_API_KEY is not configured on production backend.');
      }
      return `«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n[حالت توسعه] ایده دریافت شد: «${prompt}»`;
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
    } catch (err: any) {
      console.error('[OpenAICompatibleProvider] Call failure:', err.message);
      if (env.NODE_ENV === 'production') {
        throw new Error('AI_PROVIDER_UNAVAILABLE: OpenAI provider failed.');
      }
    }

    return `«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\n[حالت توسعه] درخواست دریافت شد: «${prompt}»`;
  }
}

export class TarhiNooAIGateway {
  private provider: AIProvider;
  private systemInstruction = `You are Tarhi Noo AI (هوش مصنوعی طرحی نو), from Tarhineh Media (رسانه هنری طرحینه مدیا).
Always introduce yourself respectfully with: «من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»
You provide expert creative direction, prompt engineering, cinematic visual concepts, and audio/music direction for Persian and international artists.`;

  constructor() {
    if (env.AI_DEFAULT_PROVIDER === 'openai' && env.OPENAI_API_KEY) {
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
    return !!env.GEMINI_API_KEY || !!env.OPENAI_API_KEY;
  }
}

export const aiGateway = new TarhiNooAIGateway();
