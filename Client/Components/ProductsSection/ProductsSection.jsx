import { useMemo, useState } from 'react'
import './ProductsSection.css'

import { products } from '../../data/products.js'
import { IconBasket, IconHeart } from '../icons.jsx'

export default function ProductsSection() {
  const [liked, setLiked] = useState(() => new Set())
  const [cart, setCart] = useState(() => new Set())

  const items = useMemo(() => products, [])

  return (
    <section className="prodWrap" id="products" aria-label="Products">
      <div className="prodHeader">
        <div>
          <div className="prodTitle">Products</div>
          <div className="prodSub">Plastic-free • Non-toxic • Recycled • Nature-made</div>
        </div>
        <div className="prodHint">Scroll</div>
      </div>

      <div className="prodRail" role="list">
        {items.map((p) => {
          const isLiked = liked.has(p.id)
          const inCart = cart.has(p.id)

          return (
            <article key={p.id} className="prodCard" role="listitem" tabIndex={0}>
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
                    onClick={() => {
                      setLiked((prev) => {
                        const next = new Set(prev)
                        if (next.has(p.id)) next.delete(p.id)
                        else next.add(p.id)
                        return next
                      })
                    }}
                  >
                    <IconHeart filled={isLiked} />
                  </button>

                  <button
                    className={inCart ? 'prodAddBtn prodAddBtnActive' : 'prodAddBtn'}
                    type="button"
                    onClick={() => {
                      setCart((prev) => {
                        const next = new Set(prev)
                        if (next.has(p.id)) next.delete(p.id)
                        else next.add(p.id)
                        return next
                      })
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
