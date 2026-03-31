# Specification: Public Leaderboards & Comprehensive Player Statistics

## 1. Overview
This track implements the core data visualization and competition features of the Foosball Statistics Platform. It transforms confirmed match data into actionable insights, providing global rankings to drive office rivalry and personal performance analytics for player mastery.

## 2. Functional Requirements

### 2.1. Global Leaderboards
- **Rankings**: Three distinct leaderboards:
    - **Overall**: Based on total Win Rate across all matches.
    - **Top Attackers**: Based on Win Rate when playing as Attacker.
    - **Top Defenders**: Based on Win Rate when playing as Defender.
- **Pagination**: Leaderboards display all eligible players with pagination. Users can select page sizes of 10, 20, 50, or 100 entries per page.
- **Metrics**: Display Rank, Player Name, Win Rate (%), Total Matches, Wins, and Losses.

### 2.2. Player Statistics (Personal Profile)
- **Position-Specific Stats**: Detailed breakdown for Attacker and Defender roles:
    - Matches Played, Wins, Losses, Win Rate (%).
- **Head-to-Head (H2H) Analytics**:
    - Table showing performance against each unique opponent.
    - Columns: Opponent Name, Matches, Wins, Losses, Win Rate (%).
    - **Positional H2H**: Ability to filter H2H by 4 position combinations (e.g., "Me as Attacker vs Opponent as Defender").

### 2.3. Filtering and Configuration
- **Time Window Switcher**: Filter all statistics by:
    - Weekly, Monthly, Yearly, All-time.
- **Minimum Match Threshold**: A customizable filter (on the frontend) to exclude players with fewer than X matches from the leaderboards (defaulting to 5 or 10 as per user preference).

### 2.4. UI/UX
- **Dedicated Page**: A new "Leaderboards & Stats" page, accessible from the navigation.
- **Interactive Tables**: Sorting by columns (Wins, Win%, etc.) in H2H and Leaderboard tables. Pagination controls (Previous/Next, Page Size selector).
- **Responsive Design**: Ensure complex tables are readable on mobile.

## 3. Non-Functional Requirements
- **Data Integrity**: Only `CONFIRMED` matches are included in calculations.
- **Performance**: Statistics calculation should be optimized (e.g., using efficient SQL queries or caching) to ensure fast page loads as the match history grows. Pagination is required at the database level to maintain performance.
- **Real-time Accuracy**: Leaderboards should update immediately after a match is confirmed.

## 4. Acceptance Criteria
- [ ] Users can view paginated leaderboards for Overall, Attack, and Defense roles with customizable page sizes (10, 20, 50, 100).
- [ ] Users can view their own personal statistics breakdown by position.
- [ ] Users can view a Head-to-Head table against all opponents.
- [ ] Filtering by time period (Weekly/Monthly/etc.) correctly updates all displayed statistics.
- [ ] Minimal match count filter correctly hides/shows players on leaderboards.
- [ ] Statistics only include confirmed matches.

## 5. Out of Scope
- Integration with external social media.
- Automated Slack/Teams notifications for leaderboard changes (future track).
- Graphical charts (e.g., line charts for performance over time) - reserved for a later "Analytics" track.
