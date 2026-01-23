# CACUN UI - Quick Reference Guide

## 🎯 Core Design Rules

### ✅ DO THIS
- Use `box-shadow` for all glows and depth
- Use 1px solid borders (#222)
- Use solid #111 for surfaces
- Use spring physics for animations
- Make interactions feel tactile and magnetic
- Use Space Grotesk with tight tracking
- Apply accent colors on hover
- Use motion values for cursor tracking

### ❌ DON'T DO THIS
- ❌ No `backdrop-blur` or glassmorphism
- ❌ No gradient backgrounds
- ❌ No opacity overlays without purpose
- ❌ No light colors on light backgrounds
- ❌ No easing animations (use spring physics)
- ❌ No default font families
- ❌ No hamburger menus (sticky nav only)

---

## 🎨 Quick Color Reference

```jsx
// Neon Pink - Primary Accent
#FF007F - Use for: CTAs, hover states, primary glows

// Electric Purple - Secondary Accent  
#7000FF - Use for: Alternative accents, secondary glows

// Obsidian - Base
#050505 - Page background

// Surface - Cards
#111111 - Card/container backgrounds

// Border - Lines
#222222 - 1px borders
```

---

## 🧲 Magnetic Button Usage

```jsx
<MagneticButton>
  Click Me →
</MagneticButton>
```

**Features:**
- Spring-powered cursor attraction
- Glow on hover (neon pink)
- Scale animation on tap (0.95)
- Hover scale (1.05)

---

## 📦 Bento Card Usage

```jsx
<BentoCard 
  title="Feature Name"
  accentColor={COLORS.neonPink}
>
  <p>Your content here</p>
</BentoCard>
```

**Features:**
- Animated top border glow
- Border color change on hover
- Full-height responsive layout

---

## 🎬 Animation Patterns

### Entrance (Page Load)
```jsx
initial={{ opacity: 0, y: 30 }}
animate={{ opacity: 1, y: 0 }}
transition={{ duration: 0.8, delay: 0.2 }}
```

### Scroll Reveal
```jsx
initial={{ opacity: 0, y: 20 }}
whileInView={{ opacity: 1, y: 0 }}
transition={{ delay: index * 0.1 }}
```

### Hover Scale
```jsx
whileHover={{ scale: 1.05 }}
transition={{ type: "spring", stiffness: 300 }}
```

### Magnetic Attraction
```jsx
x.set(deltaX * 0.25)  // 25% of movement
springX = useSpring(x, { stiffness: 400, damping: 30 })
```

---

## 📐 Layout Grid

```jsx
style={{
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
  gap: 24,
}}
```

**Results:**
- Mobile: 1 column (320px+)
- Tablet: 2 columns (640px+)
- Desktop: 3 columns (960px+)
- Large: 4-6 columns (1280px+)

---

## 🔤 Typography Scale

```jsx
// Headings
<h1 className="text-5xl md:text-7xl lg:text-8xl font-bold"
    style={{ letterSpacing: "-0.05em" }}>
  
// Subheadings
<h2 className="text-4xl md:text-5xl font-bold"
    style={{ letterSpacing: "-0.04em" }}>

// Body Text
<p className="text-lg md:text-2xl"
   style={{ letterSpacing: "-0.03em" }}>
```

---

## ✨ Glow Effects

### Pink Glow
```jsx
boxShadow: `0 0 32px 0 #FF007F40`
```

### Purple Glow
```jsx
boxShadow: `0 0 32px 0 #7000FF40`
```

### Combined Glow
```jsx
boxShadow: `0 0 32px 0 #FF007F30, 0 0 2px 1px #7000FF60`
```

### Subtle Glow
```jsx
boxShadow: `0 0 16px 0 #FF007F20`
```

---

## 🎯 Common Components

### Sticky Header
```jsx
<nav className="sticky top-0 z-40" 
     style={{ borderBottom: `1px solid ${COLORS.border}` }}>
```

### Feature Section
```jsx
<section className="w-full px-4 md:px-8 py-24 
                    bg-gradient-to-b from-transparent to-[#050505]">
```

### Footer with Glow
```jsx
<footer className="w-full border-t"
        style={{ borderTop: `1px solid ${COLORS.border}` }}>
  <motion.div
    className="h-px mx-auto max-w-sm"
    style={{
      background: `linear-gradient(90deg, transparent, ${COLORS.electricPurple}, transparent)`,
      boxShadow: `0 0 16px ${COLORS.electricPurple}40`,
    }}
  />
</footer>
```

---

## 🚀 Performance Tips

1. **Use `whileInView`** instead of scroll listeners
2. **Lazy load images** with Intersection Observer
3. **Memoize components** if rendering large lists
4. **Use `pointer-events-none`** on decorative elements
5. **Limit simultaneous animations** to 3-5 per section
6. **Use CSS custom properties** for dynamic values

---

## 📱 Responsive Breakpoints

```jsx
// Mobile First
className="text-sm md:text-base lg:text-lg xl:text-xl"

// Grid Columns
className="grid-cols-1 md:grid-cols-2 lg:grid-cols-3"

// Padding
className="px-4 md:px-8 lg:px-12"
```

---

## 🔧 Configuration Files

**tailwind.config.js** - Theme colors, spacing, shadows
**vite.config.js** - Build and dev server configuration  
**index.css** - Global styles and Tailwind directives
**App.jsx** - Main application component

---

**Last Updated:** January 2026
**Version:** 1.0.0 - Production Ready
