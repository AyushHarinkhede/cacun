import { useEffect, useRef } from 'react'
import './Home.css'

import logo from '../cacun.png'
import ProductsSection from '../ProductsSection/ProductsSection.jsx'
import NewsletterSection from '../NewsletterSection/NewsletterSection.jsx'
import ShopCtaSection from '../ShopCtaSection/ShopCtaSection.jsx'
import ShopSection from '../ShopSection/ShopSection.jsx'

export default function Home() {
  const heroRef = useRef(null)

  useEffect(() => {
    const el = heroRef.current
    if (!el) return

    const onScroll = () => {
      const y = window.scrollY
      const scale = Math.max(0.88, Math.min(1.12, 1 + y / 1800))
      el.style.setProperty('--hero-scale', String(scale))
    }

    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <div className="home" id="home">
      <section className="hero">
        <div className="heroCard">
          <p className="heroKicker">Nature-first marketplace</p>
          <div ref={heroRef} className="heroLogoWrap" aria-label="cacun">
            <img className="heroLogo" src={logo} alt="cacun" />
          </div>
          <p className="heroSub">
            Plastic-free, non-toxic, recycled-material products — made to keep you close to nature.
          </p>
          <div className="heroPills">
            <span className="pill">Plastic Free</span>
            <span className="pill">Non-Toxic</span>
            <span className="pill">Recycled Material</span>
            <span className="pill">Nature Products</span>
            <span className="pill">Reuse</span>
          </div>
        </div>
      </section>

      <section className="stackGrid" id="stacks" aria-label="Product stacks">
        <div className="stackCard">
          <h2 className="stackTitle">Plastic Free</h2>
          <p className="stackText">Packaging and products with zero plastic use.</p>
        </div>
        <div className="stackCard">
          <h2 className="stackTitle">Non Toxic</h2>
          <p className="stackText">Safe beauty, soap, detergents, farm items and daily essentials.</p>
        </div>
        <div className="stackCard">
          <h2 className="stackTitle">Recycled Material</h2>
          <p className="stackText">Shoes, clothes, carry bags, pouches, boxes, furniture and more.</p>
        </div>
        <div className="stackCard">
          <h2 className="stackTitle">Nature Products</h2>
          <p className="stackText">Leafy plates, edible spoons, coconut coir scrub, organic skincare.</p>
        </div>
        <div className="stackCard">
          <h2 className="stackTitle">Reuse Products</h2>
          <p className="stackText">Refillable bottles, reusable shampoo packaging, cleaner capsules.</p>
        </div>
      </section>

      <ShopCtaSection />

      <ProductsSection showViewMore />

      <section className="calloutRow" id="campaigns" aria-label="Campaigns and NGOs">
        <div className="callout">
          <h2 className="calloutTitle">Campaigns</h2>
          <p className="calloutText">Join clean-earth missions and track impact through the products you buy.</p>
          <button className="primaryBtn" type="button">Explore campaigns</button>
        </div>
        <div className="callout" id="ngos">
          <h2 className="calloutTitle">NGO Partners</h2>
          <p className="calloutText">Support verified organizations working on waste reduction and nature protection.</p>
          <button className="primaryBtn" type="button">See NGO partners</button>
        </div>
      </section>

      <NewsletterSection />

      <ShopSection />
    </div>
  )
}
