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

---

## 7. Modal Windows, Form Controls & Interactive Patterns

To maintain the Speakeasy Stadium feel across all overlay and form interactions, every modal window and interactive input must adhere to the following strict conventions:

### Modal Window Architecture
1. **Backdrop Overlay:**
   - Classes: `fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm`
   - Dismissal: Clicking the backdrop (`@click.self="handleClose"`) must close the modal.
2. **Scroll Lock:**
   - When the modal opens (`isOpen: true`), lock the page scroll via `document.body.style.overflow = 'hidden'`.
   - Restore on modal close and on component unmount (`document.body.style.overflow = ''`).
3. **Card Shell (No-Line Elevation):**
   - Classes: `bg-surface-container-low rounded-2xl w-full max-w-lg max-h-[90vh] flex flex-col shadow-2xl overflow-hidden border-0` with explicit inline style `style="border-width: 0px;"`.
   - Strict adherence to the 0px border rule: depth is achieved solely via tonal hierarchy and dark elevation.
4. **Header & Footer Framing:**
   - **Header:** `flex items-center justify-between p-4 bg-surface-container` with close button `p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high`.
   - **Footer:** `p-4 bg-surface-container flex gap-3 justify-end` containing secondary and primary action buttons.
   - **Body:** `flex-1 overflow-y-auto p-4 space-y-4 text-start custom-modal-scroll overscroll-contain`.
5. **Section Inlays:**
   - Group related fields into visual inlays using `bg-surface-container-highest/60 p-4 rounded-xl space-y-3`.
   - Section headers must be uppercase bold: `text-xs font-bold uppercase tracking-wider text-primary`.
6. **Custom Scrollbar:**
   - Apply `custom-modal-scroll`: `scrollbar-width: thin; scrollbar-color: #393431 transparent;` with hover thumb `#4b4440` and 0 background.

---

### Keyboard Interaction & Escape Key Handling
1. **Capture-Phase Escape Listener:**
   - Register a capture-phase key listener on `window` and `document`: `addEventListener('keydown', handleKeyDown, true)`.
   - Check `event.key === 'Escape' || event.key === 'Esc' || event.code === 'Escape'`.
   - Always remove listener in `onUnmounted` and when modal closes.
2. **Nested Popover Escape Priority:**
   - Nested dropdowns / popovers (e.g. `CustomSelect`) must intercept Escape first with `@keydown.escape.stop="isOpen = false"`, closing the dropdown on first press, and closing the modal on subsequent press.

---

### Form Controls & Input Mechanics

#### 1. Text Inputs (`<input type="text">`)
- **Styling:** `w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface placeholder:text-on-surface-variant/50 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:bg-surface-container-high transition-colors`.
- **Autofill & Autocomplete:**
  - Always specify `autocomplete="off"` on named inputs to prevent white browser history popups.
  - Global CSS resets `-webkit-autofill` with dark box-shadow insets (`#262220`) and text color `#ebe0dd`.

#### 2. Number Steppers (`NumberInput.vue`)
- **Compact Geometry:**
  - Outer wrapper: `inline-flex items-center w-fit bg-surface-container rounded-lg transition-all focus-within:bg-surface-container-high focus-within:ring-1 focus-within:ring-primary overflow-hidden`.
  - Minus button: Pinned to left edge with `px-2.5 py-1.5`.
  - Input field: Tight `w-11` (maximum 3 digits visible) with `text-center font-semibold text-sm py-1.5 px-0.5`.
  - Plus button: `px-2.5 py-1.5` positioned snug next to the number.
- **Interaction & Normalization:**
  - Allows natural keyboard typing without mid-typing truncation.
  - On `@blur`: automatically validates and clamps the typed value to `min` and `max`, normalizing the DOM value.
  - Mouse wheel support via `@wheel.prevent="handleWheel"`.

#### 3. Custom Dark Dropdowns (`CustomSelect.vue`)
- **Pure Dark Popovers:**
  - Replaces OS native select popups with custom Vue listbox overlays using `bg-surface-container-high` (`#302b29`), `text-on-surface`, `hover:bg-surface-container-highest`, and active item highlights `bg-primary/20 text-primary font-bold` with checkmark.
  - Closes on click outside and on Escape.
  - Keeps a hidden native `<select :data-testid="dataTestid" class="sr-only">` to preserve automated testing and accessibility parity.

#### 4. Tooltips (`BaseTooltip.vue`)
- **Modal Boundary Collision Detection:**
  - Tooltips measure trigger coordinates relative to the active dialog (`.closest('[role="dialog"]')`).
  - Dynamically sets placement (`left`, `center`, `right`) and adjusts arrow offset (`left-2`, `left-1/2`, `right-2`), ensuring tooltips in left or right grid columns never clip outside modal edges.

#### 5. Real-Time Validation Feedback
- **Inline Warnings:**
  - Every numeric field displays an instant red message (`text-error text-xs mt-1 font-medium`) directly beneath the field if the entered value exceeds bounds or violates logical constraints.
  - `handleSave` performs full range validation against internationalized error messages before emitting save payloads.

