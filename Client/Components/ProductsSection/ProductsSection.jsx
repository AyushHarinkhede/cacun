import { useMemo } from 'react'
import './ProductsSection.css'

import { products } from '../../data/products.js'
import { IconBasket, IconHeart } from '../icons.jsx'
import { useSettings } from '../../contexts/useSettings.js'

export default function ProductsSection({ showViewMore = false }) {
  const { likedIds, cartIds, toggleLiked, toggleCart, setActiveProductId } = useSettings()

  const items = useMemo(() => products, [])
  const displayed = useMemo(() => (showViewMore ? items.slice(0, 5) : items), [items, showViewMore])

  const goShop = () => {
    const el = document.getElementById('shop')
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <section className="prodWrap" id="products" aria-label="Products">
      <div className="prodHeader">
        <div>
          <div className="prodTitle">Products</div>
          <div className="prodSub">Plastic-free • Non-toxic • Recycled • Nature-made</div>
        </div>
        <div className="prodHeaderActions">
          {showViewMore ? (
            <button className="prodMore" type="button" onClick={goShop}>
              View more
            </button>
          ) : null}
          <div className="prodHint">Scroll</div>
        </div>
      </div>

      <div className="prodRail" role="list">
        {displayed.map((p) => {
          const isLiked = likedIds.has(p.id)
          const inCart = cartIds.has(p.id)

          return (
            <article
              key={p.id}
              className="prodCard"
              role="listitem"
              tabIndex={0}
              onClick={() => setActiveProductId(p.id)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') setActiveProductId(p.id)
              }}
            >
              <div className="prodMedia">
                <img className="prodImg" src={p.images[0]} alt={p.title} />
                <div className="prodBadge">-{p.discountPercent}%</div>
              </div>

              <div className="prodBody">
                <div className="prodName">{p.title}</div>
                <div className="prodCat">{p.category}</div>
                <div className="prodMeta">{p.material}</div>
                <div className="prodBrand">Brand: {p.brand}</div>

                <div className="prodActions">
                  <button
                    className={isLiked ? 'prodIconBtn prodIconBtnActive' : 'prodIconBtn'}
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
                    className={inCart ? 'prodAddBtn prodAddBtnActive' : 'prodAddBtn'}
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
