import { useMemo, useState } from 'react'
import './Navbar.css'

import logo from '../cacun.png'
import { useSettings } from '../../contexts/useSettings.js'
import { IconBasket, IconHeart, IconSearch } from '../icons.jsx'

export default function Navbar() {
  const { toggleTheme, setSettingsOpen } = useSettings()
  const [query, setQuery] = useState('')
  const [likesCount] = useState(0)
  const [basketCount] = useState(0)

  const placeholder = useMemo(() => {
    return 'Search nature products, recycled items, plastic-free…'
  }, [])

  return (
    <header className="navWrap">
      <div className="navInner">
        <div className="navLeft">
          <a className="brand" href="#home" aria-label="cacun home">
            <img className="brandLogo" src={logo} alt="cacun" />
            <span className="brandName">cacun</span>
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
