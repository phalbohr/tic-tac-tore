import { test, expect } from '../support/fixtures';

test.describe('Match Recording Scenario', () => {
  test('should record a new match with two players', async ({ 
    page, 
    playerFactory, 
    matchFactory,
    interceptNetworkCall 
  }) => {
    // 1. Setup Data
    const players = playerFactory.createMany(2);
    const match = matchFactory.create({
      players: { team1: [players[0]], team2: [players[1]] },
      score: { team1: 10, team2: 8 }
    });

    // 2. Intercept API calls
    // Note: We intercept the seed call if necessary, but here we assume local backend or mock
    const matchSubmit = await interceptNetworkCall({
      page,
      url: '**/api/v1/matches',
      method: 'POST',
      fulfill: { status: 201, json: { id: match.id, status: 'PENDING' } }
    });

    // 3. Navigation & Auth
    await page.goto('/');
    
    // GIVEN I am logged in
    // Click "Pavel" button to login as dev user
    await page.getByRole('button', { name: 'Pavel' }).click();
    await expect(page.getByText('Player')).toBeVisible();

    // AND I am on the match recording page
    await page.getByTestId('nav-record-match').click();

    // WHEN I select two players (teammate and opponent 1)
    // Note: MatchRecordingForm uses <select> with data-testid="player-select-X"
    // We wait for the seed data to load the options
    await page.waitForSelector('option[data-testid^="player-option-"]');

    const teammateOption = page.locator('option[data-testid^="player-option-"]').first();
    const opponent1Option = page.locator('option[data-testid^="player-option-"]').nth(1);
    const opponent2Option = page.locator('option[data-testid^="player-option-"]').nth(2);

    const teammateId = await teammateOption.getAttribute('value');
    const opponent1Id = await opponent1Option.getAttribute('value');
    const opponent2Id = await opponent2Option.getAttribute('value');

    await page.getByTestId('player-select-1').selectOption(teammateId!);
    await page.getByTestId('player-select-2').selectOption(opponent1Id!);
    await page.getByTestId('player-select-3').selectOption(opponent2Id!);

    // AND I continue to scoring
    await page.getByTestId('submit-match-button').click();

    // AND I enter positions for Game 1
    await page.locator('#t1pos-0').selectOption('creator-teammate');
    await page.locator('#t2pos-0').selectOption('opponent1-opponent2');

    // AND I enter the score 10 - 8
    await page.getByTestId('score-input-1').fill('10');
    await page.getByTestId('score-input-2').fill('8');

    // AND I finish the match
    // Handle the alert dialog
    page.on('dialog', async dialog => {
      expect(dialog.message()).toContain('Match recorded successfully');
      await dialog.accept();
    });

    await page.getByTestId('submit-match-button').click();

    // THEN the match should be sent to the server
    const { requestBody } = await matchSubmit.waitForCall();
    expect(requestBody.games[0].teamAScore).toBe(10);
    expect(requestBody.games[0].teamBScore).toBe(8);
  });
});
