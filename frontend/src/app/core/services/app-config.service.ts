import { Injectable, signal, computed } from '@angular/core';
import { AppConfig } from '../../shared/models/app-config.model';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private readonly _config = signal<AppConfig | null>(null);

  public readonly config = computed(() => {
    const cfg = this._config();
    if (!cfg) {
      throw new Error('[AppConfigService] AppConfig has not been initialized');
    }
    return cfg;
  });

  public readonly apiUrl = computed(() => {
    const cfg = this.config();
    return cfg.apiUrl;
  });

  public async loadConfig(): Promise<void> {
    const timestamp = Date.now();

    const defaultRes = await fetch(`/config.default.json?v=${timestamp}`);
    if (!defaultRes.ok) {
      throw new Error(`[AppConfigService] Failed to load config.default.json (Status: ${defaultRes.status})`);
    }

    const defaultConfig: Partial<AppConfig> = await defaultRes.json();

    let overrideConfig: Partial<AppConfig> = {};
    try {
      const overrideRes = await fetch(`/config.override.json?v=${timestamp}`);
      if (overrideRes.ok) {
        overrideConfig = await overrideRes.json();
      }
    } catch {
    }

    const apiUrl = overrideConfig.apiUrl || defaultConfig.apiUrl;
    if (!apiUrl) {
      throw new Error('[AppConfigService] Missing required configuration: "apiUrl" must be provided in config files');
    }

    const mergedConfig: AppConfig = {
      apiUrl,
      appTitle: overrideConfig.appTitle || defaultConfig.appTitle,
      featureFlags: {
        ...(defaultConfig.featureFlags || {}),
        ...(overrideConfig.featureFlags || {}),
      },
    };

    this._config.set(mergedConfig);
  }
}
