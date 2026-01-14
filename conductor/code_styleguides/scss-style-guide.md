# SCSS Style Guide

## 1. Syntax and Formatting

### Syntax
- Use the SCSS syntax (`.scss`), not the indented Sass syntax.
- Use 2 spaces for indentation.

### Nesting
- Avoid excessive nesting. Limit nesting to 3 levels deep to prevent specificity issues and maintain readability.

**✅ Correct:**
```scss
.card {
  border: 1px solid #ddd;

  &__header {
    background: #f5f5f5;
  }
}
```

**❌ Incorrect:**
```scss
.card {
  .card-body {
    .content {
      .text {
        color: red;
      }
    }
  }
}
```

## 2. Naming Conventions

### BEM (Block Element Modifier)
- Adopt the BEM naming convention for classes to ensure modularity and low specificity.
- **Block:** `.block`
- **Element:** `.block__element`
- **Modifier:** `.block--modifier`

```scss
.btn {
  &__icon { ... }
  &--primary { ... }
}
```

## 3. Variables and Mixins

### Variables
- Use SCSS variables (or CSS custom properties) for colors, fonts, and spacing to maintain consistency.

```scss
$primary-color: #3498db;
$spacing-unit: 8px;

.button {
  color: $primary-color;
  padding: $spacing-unit;
}
```

### Mixins
- Use mixins for reusable patterns (e.g., media queries, flexbox layouts).

```scss
@mixin flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.container {
  @include flex-center;
}
```

## 4. Scoped Styles in Vue
- Always use the `scoped` attribute in Vue SFC `<style>` tags to prevent style leakage.

```vue
<style scoped lang="scss">
.component-specific {
  color: red;
}
</style>
```

## 5. Global Styles
- Global styles (reset, typography, variables) should be imported in the main entry point (e.g., `main.ts` or `App.vue`) or typically in a `styles/main.scss` file.
