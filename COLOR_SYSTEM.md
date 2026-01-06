# Material Design Color Scheme - Visual Reference

## 🎨 Main Color Palette

### Primary Blue (Actions & Headers)
- **Blue**: `#1E88E5` ← Main CTA buttons
- **Dark Blue**: `#1565C0` ← Hover states
- **Light Blue**: `#42A5F5` ← Light accents

### Secondary Yellow (Badges & Highlights)
- **Yellow**: `#FFC107` ← Badges, pills
- **Dark Yellow**: `#FFA000` ← Text, dark states
- **Light Yellow**: `#FFD54F` ← Light backgrounds

### Accent Red (Sparingly)
- **Red**: `#FF5252` ← Error, delete, important
- **Light Red**: `#FF6E40` ← Red hover

### Neutrals
- **Text Primary**: `#212121` ← Main text
- **Text Secondary**: `#616161` ← Muted text, hints
- **Background**: `#FAFAFA` ← Page background
- **White**: `#FFFFFF` ← Cards, surfaces
- **Surface**: `#F5F5F5` ← Slightly darker surface
- **Divider**: `#BDBDBD` ← Borders, lines

---

## 📐 Radius Scale

```
--radius-lg:  16px  ← Cards, modals, major containers
--radius-md:  12px  ← Buttons, inputs, components
--radius-sm:   8px  ← Small buttons, chips
```

---

## 🎯 Component Color Usage

### Navbar
```
Background:    WHITE (#FFFFFF)
Text:          TEXT-PRIMARY (#212121)
Button:        PRIMARY-BLUE (#1E88E5)
Border:        DIVIDER (#BDBDBD)
Search Focus:  PRIMARY-BLUE (#1E88E5)
```

### Buttons
```
Primary:       BLUE (#1E88E5) with WHITE text
Secondary:     WHITE (#FFFFFF) with DIVIDER border
Accent:        YELLOW (#FFC107) with BLACK text
Danger:        RED (#FF5252) with WHITE text
Hover:         Darker version of main color
```

### Cards
```
Background:    WHITE (#FFFFFF)
Text:          TEXT-PRIMARY (#212121)
Hint:          TEXT-SECONDARY (#616161)
Border:        DIVIDER (#BDBDBD) or transparent
Hover Border:  PRIMARY-BLUE (#1E88E5)
```

### Badges/Pills
```
Background:    YELLOW (#FFC107)
Text:          TEXT-PRIMARY (#212121)
Border:        YELLOW or TRANSPARENT
```

### Modals
```
Background:    WHITE (#FFFFFF)
Header:        PRIMARY-BLUE (#1E88E5) with WHITE text
Border:        DIVIDER (#BDBDBD)
Close Button:  TEXT-PRIMARY with light hover
```

---

## 🌟 Shadow System

### Elevation 1 (Subtle - Cards, Inputs)
```
box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
```

### Elevation 2 (Light - Hover States)
```
box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
```

### Elevation 3 (Medium - Modals, Dropdowns)
```
box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
```

### Elevation 4 (Strong - Floating, Top-Level)
```
box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
```

---

## ✨ Transitions & Animations

### Standard Transition
```css
transition: all 200ms ease-out;
```

### Hover Scale
```css
transform: scale(1.05);  /* For subtle hover effect */
```

### Color Change
```css
transition: color 200ms ease-out;
transition: background 200ms ease-out;
```

### No Complex Transforms on Hover!
```css
/* ✅ DO THIS */
.button:hover {
  background: darker-color;
  box-shadow: larger-shadow;
}

/* ❌ DON'T DO THIS */
.button:hover {
  transform: translateY(-4px) scale(1.1);  /* Material Design doesn't use this */
  box-shadow: huge-complex-shadow;
}
```

---

## 🎭 Brand & Typography

### Font
- **Family**: `'Roboto', system-ui, -apple-system, sans-serif`
- **Weight**: 400 (normal), 500 (medium), 600 (semibold)
- **Size**: 14px (body), 16px (heading), 12px (small)

### Text Colors
```css
.heading     { color: var(--text-primary); font-weight: 600; }
.body        { color: var(--text-primary); font-weight: 400; }
.muted       { color: var(--text-secondary); font-weight: 400; }
.accent      { color: var(--secondary-yellow-dark); font-weight: 500; }
.highlight   { color: var(--primary-blue); font-weight: 500; }
```

---

## 📋 Before vs After Example

### BEFORE (Glassmorphic)
```css
.card {
  background: linear-gradient(135deg, rgba(27, 38, 59, 0.6) 0%, rgba(13, 27, 42, 0.8) 100%);
  backdrop-filter: blur(20px);
  border: 1.5px solid rgba(230, 57, 70, 0.25);
  box-shadow: 0 20px 60px rgba(230, 57, 70, 0.15), inset 0 1px 0 rgba(230, 57, 70, 0.08);
  color: var(--text-light);
  border-radius: 32px;
}

.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 30px 80px rgba(230, 57, 70, 0.25);
  border-color: rgba(230, 57, 70, 0.4);
}
```

### AFTER (Material Design)
```css
.card {
  background: var(--bg-white);
  border: 1px solid var(--divider);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  color: var(--text-primary);
  border-radius: 16px;
}

.card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: var(--primary-blue);
}
```

---

## 🎯 Quick Checklist for Updates

When updating any component, ensure:

- [ ] Background is white/light (not dark gradient)
- [ ] Border is `1px solid var(--divider)` (not gradient)
- [ ] Text is `var(--text-primary)` (not light/white text)
- [ ] Buttons are blue or yellow (not red gradient)
- [ ] Shadows are subtle (0 2-5px shadows)
- [ ] No `backdrop-filter` blur
- [ ] No complex hover transforms
- [ ] Border radius is 16px, 12px, or 8px (not 32px)
- [ ] All colors match the palette above
- [ ] Animations are 200ms ease-out

---

## 💾 CSS Variables Summary

```css
:root {
  /* Colors */
  --primary-blue: #1E88E5;
  --primary-blue-dark: #1565C0;
  --secondary-yellow: #FFC107;
  --secondary-yellow-dark: #FFA000;
  --accent-red: #FF5252;
  --text-primary: #212121;
  --text-secondary: #616161;
  --bg-white: #FFFFFF;
  --bg-light: #FAFAFA;
  --divider: #BDBDBD;

  /* Sizing */
  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;
}
```

---

## 🚀 Implementation Priority

1. **HIGH**: Complete remaining CSS updates (modals, drawers)
2. **MEDIUM**: Test all components across devices
3. **LOW**: Fine-tune animations and transitions

---

Generated: January 6, 2026 | Cacun App Material Design System v1.0
