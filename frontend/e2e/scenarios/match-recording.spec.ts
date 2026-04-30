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
    const matchSubmit = await interceptNetworkCall({
      page,
      url: '**/api/matches',
      method: 'POST',
      fulfill: { status: 201, json: { id: match.id, status: 'PENDING' } }
    });

    // 3. Navigation
    await page.goto('/');
    
    // GIVEN I am on the match recording page
    await page.getByTestId('nav-record-match').click();

    // WHEN I select two players
    await page.getByTestId('player-search-input-1').fill(players[0].name);
    await page.getByTestId(`player-option-${players[0].id}`).click();
    
    await page.getByTestId('player-search-input-2').fill(players[1].name);
    await page.getByTestId(`player-option-${players[1].id}`).click();

    // AND I enter the score 10 - 8
    await page.getByTestId('score-input-1').fill('10');
    await page.getByTestId('score-input-2').fill('8');

    // AND I submit the match
    await page.getByTestId('submit-match-button').click();

    // THEN the match should be sent to the server
    const { requestBody } = await matchSubmit.waitForCall();
    expect(requestBody.score.team1).toBe(10);
    expect(requestBody.score.team2).toBe(8);

    // AND I should see a success confirmation
    await expect(page.getByTestId('match-recorded-success')).toBeVisible();
  });
});
