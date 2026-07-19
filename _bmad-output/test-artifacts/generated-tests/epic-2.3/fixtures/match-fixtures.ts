import { Page } from '@playwright/test';

export class MatchFixtures {
  constructor(private page: Page) {}

  async mockRulesEndpoint(config: { scoreLimit: number, winsNeeded: number }) {
    await this.page.route('**/api/v1/rules/**', async route => {
      const json = {
        scoreLimit: config.scoreLimit,
        winsNeeded: config.winsNeeded,
        winByTwo: true
      };
      await route.fulfill({ json });
    });
  }

  async mockRulesEndpointError(status: number) {
    await this.page.route('**/api/v1/rules/**', async route => {
      await route.fulfill({ status });
    });
  }

  async navigateAndInitializeMatch() {
    await this.page.goto('/match/new');
    // Assuming a flow where users select 1v1 by default for most tests
    await this.setupOneVsOneMatch(['p1', 'p2']);
  }

  async setupOneVsOneMatch(players: string[]) {
    // Select match type
    await this.page.click('text=1 vs 1');
    // Select players (mocking basic interaction)
    for (const p of players) {
      await this.page.click(`text=Player ${p}`);
    }
  }

  async setupTwoVsTwoMatch(players: string[]) {
    await this.page.goto('/match/new');
    // Select match type
    await this.page.click('text=2 vs 2');
    // Select players
    for (const p of players) {
      await this.page.click(`text=Player ${p}`);
    }
  }

  async proceedToScoreEntry() {
    await this.page.click('button:has-text("Start Match")');
  }
}
