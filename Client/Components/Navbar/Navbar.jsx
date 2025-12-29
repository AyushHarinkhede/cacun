import { useMemo, useState } from 'react'
import './Navbar.css'

import logo from '../cacun.png'
import { useSettings } from '../../contexts/useSettings.js'
import { IconBasket, IconHeart, IconSearch } from '../icons.jsx'
import { searchIndex } from '../../data/searchIndex.js'

export default function Navbar() {
  const { toggleTheme, setSettingsOpen } = useSettings()
  const [query, setQuery] = useState('')
  const [likesCount] = useState(0)
  const [basketCount] = useState(0)

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
            placeholder={placeholder}
            aria-label="Search products"
          />
          <button className="navSearchBtn" type="button" onClick={() => setSettingsOpen(true)}>
            Filters
          </button>

          {suggestions.length > 0 ? (
            <div className="navSuggest" role="listbox" aria-label="Search suggestions">
              {suggestions.map((s) => (
                <button
                  key={`${s.type}:${s.id}`}
                  type="button"
                  className="navSuggestItem"
                  onClick={() => setQuery(s.label)}
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
          <div className="navAuth">
            <button className="navAuthBtn" type="button">
              Sign in
            </button>
            <div className="navAuthMenu" role="menu">
              <button type="button" className="navMenuItem">
                Login
              </button>
              <button type="button" className="navMenuItem">
                Signup
              </button>
              <button type="button" className="navMenuItem" onClick={toggleTheme}>
                Toggle theme
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  )
}
