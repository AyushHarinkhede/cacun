import { useMemo, useState } from 'react'
import './Navbar.css'

import logo from '../cacun.png'
import { useSettings } from '../../contexts/useSettings.js'
import { IconBasket, IconHeart, IconSearch } from '../icons.jsx'
import { searchIndex } from '../../data/searchIndex.js'

export default function Navbar() {
  const { setAuthOpen, user, likesCount, basketCount } = useSettings()
  const [query, setQuery] = useState('')

  const suggestions = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (q.length < 2) return []

    const all = [
      ...searchIndex.products,
      ...searchIndex.ngos,
      ...searchIndex.campaigns,
      ...searchIndex.brands,
    ]

    return all
      .filter((item) => item.label.toLowerCase().includes(q))
      .slice(0, 8)
  }, [query])

  const placeholder = useMemo(() => {
    return 'Search nature products, recycled items, plastic-free…'
  }, [])

  const goTo = (item) => {
    const targetId =
      item.type === 'product' || item.type === 'brand'
        ? 'products'
        : item.type === 'ngo'
          ? 'ngos'
          : item.type === 'campaign'
            ? 'campaigns'
            : 'home'
    const el = document.getElementById(targetId)
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <header className="navWrap">
      <div className="navInner">
        <div className="navLeft">
          <a className="brand" href="#home" aria-label="cacun home">
            <img className="brandLogo" src={logo} alt="cacun" />
          </a>
        </div>

        <div className="navSearch" role="search">
          <span className="navSearchIcon" aria-hidden="true">
            <IconSearch />
          </span>
          <input
            className="navSearchInput"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && suggestions.length > 0) {
                goTo(suggestions[0])
              } else if (e.key === 'Enter' && query.trim().length > 0) {
                const el = document.getElementById('products')
                if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
              }
            }}
            placeholder={placeholder}
            aria-label="Search products"
          />

          {suggestions.length > 0 ? (
            <div className="navSuggest" role="listbox" aria-label="Search suggestions">
              {suggestions.map((s) => (
                <button
                  key={`${s.type}:${s.id}`}
                  type="button"
                  className="navSuggestItem"
                  onClick={() => {
                    setQuery(s.label)
                    goTo(s)
                  }}
                >
                  <span className="navSuggestLabel">{s.label}</span>
                  <span className="navSuggestType">{s.type.toUpperCase()}</span>
                </button>
              ))}
            </div>
          ) : null}
        </div>

        <div className="navRight">
          <button className="navIconBtn" type="button" aria-label="Liked products">
            <IconHeart />
            {likesCount > 0 ? <span className="navBadge">{likesCount}</span> : null}
          </button>
          <button className="navIconBtn" type="button" aria-label="Basket">
            <IconBasket />
            {basketCount > 0 ? <span className="navBadge">{basketCount}</span> : null}
          </button>
          <button className="navAuthBtn" type="button" onClick={() => setAuthOpen(true)}>
            {user ? 'Profile' : 'Sign in'}
          </button>
        </div>
      </div>
    </header>
  )
}
