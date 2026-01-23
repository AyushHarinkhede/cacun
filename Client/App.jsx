/* eslint-disable no-unused-vars */
import React, { useEffect, useRef, useState } from "react";
import { motion, useMotionValue, useSpring } from "framer-motion";

// Google Fonts - Space Grotesk for Typography-First approach
const FONT_URL =
  "https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@300;400;500;600;700&display=swap";

// Neon color palette
const COLORS = {
  neonPink: "#FF007F",
  electricPurple: "#7000FF",
  white: "#FFFFFF",
  obsidian: "#050505",
  surface: "#111111",
  border: "#222222",
};

// Spotlight Cursor - Accent Glow Effect
const SpotlightCursor = () => {
  const cursorX = useMotionValue(-100);
  const cursorY = useMotionValue(-100);
  const springX = useSpring(cursorX, { stiffness: 300, damping: 30 });
  const springY = useSpring(cursorY, { stiffness: 300, damping: 30 });

  useEffect(() => {
    const handleMouseMove = (e) => {
      cursorX.set(e.clientX - 40);
      cursorY.set(e.clientY - 40);
    };

    window.addEventListener("mousemove", handleMouseMove);
    return () => window.removeEventListener("mousemove", handleMouseMove);
  }, [cursorX, cursorY]);

  return (
    <motion.div
      className="pointer-events-none fixed z-50"
      style={{
        x: springX,
        y: springY,
        width: 80,
        height: 80,
      }}
    >
      {/* Outer glow - neon pink */}
      <div
        className="absolute inset-0 rounded-full"
        style={{
          background: `radial-gradient(circle, ${COLORS.neonPink}20 0%, ${COLORS.neonPink}05 70%, transparent 100%)`,
          boxShadow: `0 0 40px 10px ${COLORS.neonPink}15, 0 0 80px 20px ${COLORS.electricPurple}08`,
        }}
      />
      {/* Inner core - purple */}
      <div
        className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-6 h-6 rounded-full"
        style={{
          background: `radial-gradient(circle, ${COLORS.electricPurple}40 0%, ${COLORS.electricPurple}10 100%)`,
          boxShadow: `0 0 24px ${COLORS.electricPurple}`,
        }}
      />
    </motion.div>
  );
};

// Magnetic Button - Spring Physics with Accent Glow
const MagneticButton = ({ children, className = "", onClick, ...props }) => {
  const ref = useRef(null);
  const [isHovered, setIsHovered] = useState(false);
  const x = useMotionValue(0);
  const y = useMotionValue(0);
  const springX = useSpring(x, { stiffness: 400, damping: 30 });
  const springY = useSpring(y, { stiffness: 400, damping: 30 });

  const handleMouseMove = (e) => {
    if (!ref.current) return;
    const rect = ref.current.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const deltaX = (e.clientX - centerX) * 0.25;
    const deltaY = (e.clientY - centerY) * 0.25;
    x.set(deltaX);
    y.set(deltaY);
  };

  const handleMouseLeave = () => {
    setIsHovered(false);
    x.set(0);
    y.set(0);
  };

  return (
    <motion.button
      ref={ref}
      className={`relative px-6 py-3 font-semibold text-base transition-all duration-200 outline-none ${className}`}
      style={{
        x: springX,
        y: springY,
        letterSpacing: "-0.03em",
        background: COLORS.electricPurple,
        color: COLORS.white,
        border: `1px solid ${isHovered ? COLORS.neonPink : COLORS.electricPurple}`,
        borderRadius: 8,
        boxShadow: isHovered
          ? `0 0 32px 0 ${COLORS.neonPink}40, 0 0 2px 1px ${COLORS.electricPurple}60, inset 0 0 16px ${COLORS.neonPink}15`
          : `0 0 16px 0 ${COLORS.electricPurple}40, 0 0 2px 1px ${COLORS.electricPurple}40`,
        cursor: "pointer",
      }}
      onMouseMove={handleMouseMove}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={handleMouseLeave}
      onClick={onClick}
      whileHover={{ scale: 1.05 }}
      whileTap={{ scale: 0.95 }}
      {...props}
    >
      <span className="relative z-10">{children}</span>
      {/* Animated border highlight on hover */}
      <motion.div
        className="absolute inset-0 pointer-events-none rounded"
        style={{
          border: `1px solid ${COLORS.neonPink}`,
          opacity: isHovered ? 1 : 0,
          boxShadow: isHovered
            ? `0 0 16px 2px ${COLORS.neonPink}30`
            : "0 0 0 0 transparent",
        }}
        transition={{ duration: 0.2 }}
      />
    </motion.button>
  );
};

// Bento Card with Animated Border and Accent Glow
const BentoCard = ({ children, className = "", accentColor = COLORS.neonPink, title }) => {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <motion.div
      className={`relative rounded-lg overflow-hidden p-6 ${className}`}
      style={{
        background: COLORS.surface,
        border: `1px solid ${COLORS.border}`,
      }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      whileHover={{
        boxShadow: `0 0 32px 0 ${accentColor}30, 0 0 2px 1px ${accentColor}60, inset 0 0 16px ${accentColor}10`,
        borderColor: accentColor,
      }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
    >
      {/* Animated top border glow */}
      <motion.div
        className="absolute top-0 left-0 right-0 h-px"
        style={{
          background: `linear-gradient(90deg, transparent, ${accentColor}, transparent)`,
          boxShadow: `0 0 16px ${accentColor}`,
          opacity: isHovered ? 1 : 0.3,
        }}
        transition={{ opacity: { duration: 0.3 } }}
      />

      <div className="relative z-10">
        {title && (
          <motion.h3
            className="text-xl font-bold mb-3"
            style={{
              color: accentColor,
              letterSpacing: "-0.04em",
              fontFamily: '"Space Grotesk", sans-serif',
            }}
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ delay: 0.1 }}
          >
            {title}
          </motion.h3>
        )}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          {children}
        </motion.div>
      </div>
    </motion.div>
  );
};

// Animated SVG Hero with Stroke Animation
const AnimatedHeroSVG = () => {
  return (
    <motion.svg
      width="140"
      height="140"
      viewBox="0 0 140 140"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className="mx-auto mb-8"
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.8 }}
    >
      import React from "react";
      import BottomNav from "./Components/BottomNav";

      // Placeholder components for layout
      const Feed = () => <div className="card" style={{ minHeight: 400, marginBottom: 24 }}>Feed (Trip Posts)</div>;
      const HighlightCarousel = () => <div className="card" style={{ minHeight: 180, marginBottom: 24 }}>Trip Highlights Carousel</div>;
      const PartnerReviews = () => <div className="card" style={{ minHeight: 180, marginBottom: 24 }}>Partner Reviews</div>;
      const ProfileCard = () => <div className="card" style={{ minHeight: 120, marginBottom: 24 }}>Profile Card</div>;
      const PartnerFinder = () => <div className="card" style={{ minHeight: 220, marginBottom: 24 }}>Find Travel Partner</div>;

      export default function App() {
        return (
          <div style={{ minHeight: "100vh", background: "var(--bg-dark)", display: "flex", flexDirection: "column" }}>
            <div style={{ flex: 1, display: "flex", justifyContent: "center", alignItems: "center", padding: 32 }}>
              <div style={{
                width: "100%",
                maxWidth: 1200,
                background: "var(--bg-dark)",
                borderRadius: 32,
                boxShadow: "0 8px 48px rgba(0,0,0,0.25)",
                padding: 32,
                display: "grid",
                gridTemplateColumns: "2fr 2.2fr 1.2fr",
                gap: 32,
              }}>
                {/* Left: Feed */}
                <div>
                  <Feed />
                  <Feed />
                </div>
                {/* Center: Highlights & Reviews */}
                <div>
                  <HighlightCarousel />
                  <PartnerReviews />
                </div>
                {/* Right: Profile & Partner Finder */}
                <div>
                  <ProfileCard />
                  <PartnerFinder />
                </div>
              </div>
            </div>
            <BottomNav />
          </div>
        );
      }
    title: "Spotlight Cursor",
    description:
      "A glowing spotlight that follows your mouse, creating an immersive and tactile interaction experience.",
    accentColor: COLORS.neonPink,
    icon: "🎯",
  },
  {
    title: "Magnetic Buttons",
    description:
      "Spring physics-powered buttons with magnetic attraction. Every click feels premium and responsive.",
    accentColor: COLORS.electricPurple,
    icon: "🧲",
  },
  {
    title: "Spring Animations",
    description:
      "Every transition powered by Framer Motion's spring physics engine for natural, tactile motion.",
    accentColor: COLORS.neonPink,
    icon: "⚡",
  },
  {
    title: "Scroll Reveal",
    description:
      "Elements fade and scale into view as you scroll. Animations trigger based on viewport visibility.",
    accentColor: COLORS.electricPurple,
    icon: "👁️",
  },
];

export default function App() {
  useEffect(() => {
    // Inject Space Grotesk font
    const link = document.createElement("link");
    link.href = FONT_URL;
    link.rel = "stylesheet";
    document.head.appendChild(link);

    // Apply global styles
    document.documentElement.style.scrollBehavior = "smooth";
    document.body.style.background = COLORS.obsidian;
    document.body.style.color = COLORS.white;
    document.body.style.fontFamily =
      '"Space Grotesk", "Syne", "Inter", system-ui, sans-serif';
    document.body.style.overflow = "overlay";
    document.documentElement.style.overflow = "overlay";

    return () => {
      document.documentElement.style.scrollBehavior = "";
      document.body.style.background = "";
      document.body.style.color = "";
      document.body.style.fontFamily = "";
      document.head.removeChild(link);
    };
  }, []);

  return (
    <motion.div
      className="min-h-screen w-full flex flex-col"
      style={{
        background: COLORS.obsidian,
        color: COLORS.white,
        fontFamily: '"Space Grotesk", "Syne", "Inter", system-ui, sans-serif',
        letterSpacing: "-0.02em",
        overflow: "hidden",
      }}
    >
      {/* Spotlight Cursor Effect */}
      <SpotlightCursor />

      {/* Sticky Navigation */}
      <motion.nav
        className="sticky top-0 z-40 w-full flex items-center justify-between px-6 md:px-8 py-5 border-b"
        style={{
          background: `linear-gradient(180deg, ${COLORS.surface}CC 0%, ${COLORS.surface}99 100%)`,
          backdropFilter: "none",
          borderBottom: `1px solid ${COLORS.border}`,
        }}
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <motion.span
          className="text-xl md:text-2xl font-bold"
          style={{
            color: COLORS.neonPink,
            letterSpacing: "-0.05em",
            textShadow: `0 0 16px ${COLORS.neonPink}40`,
          }}
          whileHover={{ scale: 1.05 }}
        >
          ◆ CACUN
        </motion.span>
        <MagneticButton>Get Started →</MagneticButton>
      </motion.nav>

      {/* Hero Section */}
      <section className="w-full flex-1 flex items-center justify-center px-4 py-16 md:py-24">
        <motion.div className="max-w-4xl w-full text-center">
          {/* Animated SVG Hero */}
          <AnimatedHeroSVG />

          {/* Main Heading */}
          <motion.h1
            className="text-5xl md:text-7xl lg:text-8xl font-bold mb-6"
            style={{
              letterSpacing: "-0.05em",
              color: COLORS.white,
              textShadow: `0 0 40px ${COLORS.neonPink}35, 0 0 20px ${COLORS.electricPurple}20`,
              lineHeight: 1.1,
            }}
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
          >
            Build Next-Gen UI
            <br />
            <span style={{ color: COLORS.neonPink }}>Experiences</span>
          </motion.h1>

          {/* Subtitle */}
          <motion.p
            className="text-lg md:text-2xl mb-12 max-w-2xl mx-auto"
            style={{
              color: COLORS.electricPurple,
              opacity: 0.9,
              lineHeight: 1.6,
            }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.4 }}
          >
            Neon. Tactile. Ultra-responsive. No glassmorphism, no gradients—just pure
            interaction with accent glows and spring physics.
          </motion.p>

          {/* CTA Button */}
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.6 }}
            className="flex gap-4 justify-center"
          >
            <MagneticButton>Explore the Demo</MagneticButton>
            <motion.button
              className="px-6 py-3 font-semibold text-base"
              style={{
                background: "transparent",
                color: COLORS.neonPink,
                border: `1px solid ${COLORS.neonPink}`,
                borderRadius: 8,
                letterSpacing: "-0.03em",
                boxShadow: `0 0 16px ${COLORS.neonPink}30`,
                cursor: "pointer",
              }}
              whileHover={{
                boxShadow: `0 0 32px ${COLORS.neonPink}50, inset 0 0 16px ${COLORS.neonPink}20`,
                scale: 1.05,
              }}
              whileTap={{ scale: 0.95 }}
            >
              View Docs →
            </motion.button>
          </motion.div>
        </motion.div>
      </section>

      {/* Features Grid Section */}
      <section className="w-full px-4 md:px-8 py-24 bg-gradient-to-b from-transparent via-transparent to-[#050505]">
        <motion.div
          className="max-w-6xl mx-auto"
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          transition={{ duration: 0.6 }}
        >
          <motion.h2
            className="text-4xl md:text-5xl font-bold mb-4 text-center"
            style={{
              letterSpacing: "-0.04em",
              color: COLORS.white,
            }}
          >
            Premium Features
          </motion.h2>
          <motion.p
            className="text-center text-lg mb-16"
            style={{
              color: COLORS.electricPurple,
              opacity: 0.8,
            }}
          >
            Crafted with precision. Built for performance.
          </motion.p>

          {/* Bento Grid */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
              gap: 24,
            }}
          >
            {FEATURES.map((feature, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1, duration: 0.6 }}
              >
                <BentoCard
                  title={feature.title}
                  accentColor={feature.accentColor}
                  className="h-full"
                >
                  <div className="text-4xl mb-4">{feature.icon}</div>
                  <p
                    className="text-base leading-relaxed"
                    style={{ color: COLORS.white, opacity: 0.8 }}
                  >
                    {feature.description}
                  </p>
                </BentoCard>
              </motion.div>
            ))}
          </div>
        </motion.div>
      </section>

      {/* Footer */}
      <footer
        className="w-full mt-16 py-12 border-t"
        style={{
          borderTop: `1px solid ${COLORS.border}`,
          background: `linear-gradient(180deg, transparent 0%, ${COLORS.darkGray}40 100%)`,
        }}
      >
        <motion.div
          className="max-w-6xl mx-auto px-4 text-center"
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          transition={{ duration: 0.8 }}
        >
          <motion.div className="mb-6">
            <p
              className="text-sm font-medium mb-2"
              style={{ color: COLORS.neonPink, letterSpacing: "-0.03em" }}
            >
              CRAFTED WITH PRECISION & CARE
            </p>
            <p
              className="text-xs"
              style={{ color: COLORS.white, opacity: 0.6 }}
            >
              © {new Date().getFullYear()} Cacun UI. All rights reserved. | Built with React,
              Tailwind, and Framer Motion
            </p>
          </motion.div>

          {/* Footer accent glow */}
          <motion.div
            className="h-px mx-auto max-w-sm"
            style={{
              background: `linear-gradient(90deg, transparent, ${COLORS.electricPurple}, transparent)`,
              boxShadow: `0 0 16px ${COLORS.electricPurple}40`,
            }}
          />
        </motion.div>
      </footer>
    </motion.div>
  );
}
