import { useMemo, useEffect } from 'react'
import './ShopSection.css'

import { products } from '../../data/products.js'
import { useSettings } from '../../contexts/useSettings.js'
import { IconBasket, IconHeart } from '../icons.jsx'

export default function ShopSection() {
  const { likedIds, cartIds, toggleLiked, toggleCart, setActiveProductId } = useSettings()

  const items = useMemo(() => products, [])

  useEffect(() => {
    const handleClickOutside = (e) => {
      const shopSection = document.getElementById('shop')
      if (shopSection && shopSection.classList.contains('shopSectionVisible')) {
        if (!shopSection.contains(e.target) && !e.target.closest('[data-shop-trigger]')) {
          shopSection.classList.remove('shopSectionVisible')
        }
      }
    }

    document.addEventListener('click', handleClickOutside)
    return () => document.removeEventListener('click', handleClickOutside)
  }, [])

  return (
    <section className="shopWrap" id="shop" aria-label="Shop">
      <div className="shopHeader">
        <div>
          <div className="shopTitle">Shop</div>
          <div className="shopSub">Everything in one place — tap a product to view more</div>
        </div>
        <button 
          className="shopCloseBtn" 
          type="button"
          onClick={() => {
            const shopSection = document.getElementById('shop')
            if (shopSection) {
              shopSection.classList.remove('shopSectionVisible')
            }
          }}
          aria-label="Close shop"
        >
          ✕
        </button>
      </div>

      <div className="shopGrid" role="list">
        {items.map((p) => {
          const isLiked = likedIds.has(p.id)
          const inCart = cartIds.has(p.id)

          return (
            <article
              key={p.id}
              className="shopCard"
              role="listitem"
              tabIndex={0}
              onClick={() => setActiveProductId(p.id)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') setActiveProductId(p.id)
              }}
            >
              <div className="shopMedia">
                <img className="shopImg" src={p.images[0]} alt={p.title} />
                <div className="shopBadge">-{p.discountPercent}%</div>
              </div>

              <div className="shopBody">
                <div className="shopName">{p.title}</div>
                <div className="shopCat">{p.category}</div>
                <div className="shopMeta">{p.material}</div>
                <div className="shopBrand">Brand: {p.brand}</div>

                <div className="shopActions">
                  <button
                    className={isLiked ? 'shopIcon shopIconActive' : 'shopIcon'}
                    type="button"
                    aria-label="Like"
                    onClick={(e) => {
                      e.stopPropagation()
                      toggleLiked(p.id)
                    }}
                  >
                    <IconHeart filled={isLiked} />
                  </button>

                  <button
                    className={inCart ? 'shopAdd shopAddActive' : 'shopAdd'}
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation()
                      toggleCart(p.id)
                    }}
                  >
                    <IconBasket />
                    {inCart ? 'Added' : 'Add'}
                  </button>
                </div>
              </div>
            </article>
          )
        })}
      </div>
    </section>
  )
}
