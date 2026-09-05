# Design System: The Clubhouse Editorial

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Speakeasy Stadium."** 

We are moving away from the sterile, neon-lit "gaming app" aesthetic. Instead, we are leaning into the tactile, moody, and exclusive atmosphere of a high-end office lounge at dusk. Think exposed brick, the smell of dark wood, and the focused intensity of a championship match under warm overhead lamps.

This system breaks the standard "SaaS template" by utilizing **intentional asymmetry** and **high-contrast typography scales**. We treat the screen as an editorial layout—where statistics are not just data, but a narrative of competition. Overlapping elements, deep tonal layering, and "glass" surfaces create a UI that feels built into the physical environment of the clubhouse rather than floating on top of it.

---

## 2. Colors & Materiality

The palette is rooted in the rich, organic tones of the game table and its surroundings.

*   **Primary (#a1d494 / #2d5a27):** Inspired by high-grade tournament felt. Use the container variant for deep, immersive backgrounds.
*   **Secondary & Tertiary (#ffb59f / #efc209):** Warm wood and brass tones that provide the "glow" of ambient lighting.
*   **The Player Accents:** Vibrant Red (#e74c3c) and Yellow (#f1c40f) are reserved strictly for team-specific data, acting as "players" on the field.

### The "No-Line" Rule
**Explicit Instruction:** Do not use 1px solid borders to section off content. Traditional borders create a "boxed-in" feeling that kills the moody atmosphere. Boundaries must be defined solely through:
1.  **Background Color Shifts:** A `surface-container-low` card sitting on a `surface` background.
2.  **Tonal Transitions:** Using the `surface-container` tiers to imply hierarchy.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. 
*   **The Base:** `surface` (#171211) is your dark "brick/wood" floor.
*   **The Section:** `surface-container-low` (#1f1b19) creates a large area for content.
*   **The Focus:** `surface-container-highest` (#393431) is used for active cards or scoreboards.
By nesting these, you create depth that feels architectural, not digital.

### The "Glass & Gradient" Rule
To achieve a premium polish, use **Glassmorphism** for floating elements (like the navigation bar or top-tier stats). Use semi-transparent versions of `surface-container` with a `20px` backdrop-blur. For primary CTAs, apply a subtle linear gradient from `primary` (#a1d494) to `primary-container` (#2d5a27) at a 135-degree angle to mimic the way light hits a felt table.

---

## 3. Typography

The typography strategy balances the precision of modern data with the "sporty" heritage of the game.

*   **Display & Headlines (Space Grotesk):** This is our "Sporty Editorial" voice. Use `display-lg` for massive, asymmetric score counts. The wide, technical feel of Space Grotesk mimics the mechanical nature of the foosball rods and players.
*   **Title & Body (Manrope):** A clean, humanist sans-serif that ensures high readability for match history and player names. Manrope feels premium and sophisticated, preventing the "sporty" headers from feeling too aggressive.
*   **Hierarchy as Identity:** Use extreme scale differences. A `display-lg` score paired with a `label-md` "GOALS" tag creates an authoritative, high-end look found in luxury sports magazines.

---

## 4. Elevation & Depth

We convey importance through **Tonal Layering** rather than structural scaffolding.

*   **The Layering Principle:** Stacking is our primary tool. A `surface-container-lowest` card placed on a `surface-container-low` section creates a "recessed" or "carved" look, reminiscent of a wooden table inlay.
*   **Ambient Shadows:** If a "floating" effect is required (e.g., a Winner's Modal), use an extra-diffused shadow. 
    *   *Blur:* 32px to 64px.
    *   *Opacity:* 6% - 10%.
    *   *Color:* Use a tinted version of `surface-container-lowest` to simulate ambient light absorption.
*   **The "Ghost Border" Fallback:** If accessibility requires a border, use `outline-variant` at 15% opacity. This creates a "glint" of light on an edge rather than a hard line.
*   **Glassmorphism:** Use for persistent headers. This allows the "brick red" or "felt green" backgrounds to bleed through softly as the user scrolls, maintaining the "warm and moody" environment.

---

## 5. Components

### Buttons
*   **Primary:** Gradient from `primary` to `primary-container`. Corner radius: `md` (0.75rem).
*   **Secondary:** `surface-container-high` with a `primary` text label. No border.
*   **Tertiary:** Ghost style; `on-surface` text with no background. Use for "Cancel" or "Back."

### Cards & Statistics
*   **Forbid Dividers:** Do not use lines to separate match history. Use `spacing-6` (1.5rem) of vertical white space or a subtle shift from `surface-container-low` to `surface-container-lowest` for alternating rows.
*   **Corner Radius:** Cards should use `lg` (1rem) for a friendly, tactile feel.

### Input Fields
*   **Style:** Minimalist. `surface-container-highest` background with a `sm` (0.25rem) bottom-only accent in `primary` when focused. No full-box borders.

### Signature Component: The "Rod" Scoreboard
A custom component for displaying live scores. Inspired by the foosball rod, it uses a horizontal `primary-fixed` line with `tertiary` (yellow) and `secondary` (red) player icons "sliding" along it to represent score progress.

---

## 6. Do's and Don'ts

### Do:
*   **Use Asymmetry:** Place the main "Match Score" off-center or overlapping a "Match Details" container to create an editorial feel.
*   **Embrace the Dark:** Keep the `background` (#171211) dominant. The UI should feel like a cozy, dimly lit room.
*   **Use Material Icons:** Customize icons to have rounded heads and rectangular bodies, mimicking the iconic foosball man shape.

### Don't:
*   **Don't use "Game UI" Tropes:** Avoid bright neon glows, heavy metal textures, or comic-book fonts. This is a clubhouse, not an arcade.
*   **Don't use pure white:** All "white" text should be `on-surface` (#ebe0dd), which is a soft, warm cream. Pure white (#FFFFFF) will break the moody lighting.
*   **Don't over-shadow:** If everything floats, nothing is important. Use background color shifts 90% of the time; save shadows for the top 10% of interactive elements.

### Accessibility Note
Ensure that text on `primary-container` (Deep Green) uses `on-primary-container` (#9dd090) to maintain high contrast. Despite the "moody" lighting, the statistics must remain legible for quick glances during an intense match.
