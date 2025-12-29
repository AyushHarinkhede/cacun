import { useEffect, useMemo, useState } from 'react'

import { SettingsContext } from './settingsContext.js'

function getInitialTheme() {
  const stored = localStorage.getItem('cacun.theme')
  if (stored === 'light' || stored === 'dark') return stored
  return 'light'
}

function getInitialScale() {
  const stored = Number(localStorage.getItem('cacun.uiScale'))
  if (!Number.isFinite(stored)) return 1
  return Math.min(1.15, Math.max(0.9, stored))
}

function getInitialUser() {
  const raw = localStorage.getItem('cacun.session')
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw)
    if (parsed && typeof parsed === 'object') return parsed
  } catch {
    return null
  }
  return null
}

function getInitialIdSet(key) {
  const raw = localStorage.getItem(key)
  if (!raw) return new Set()
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) return new Set(parsed)
  } catch {
    return new Set()
  }
  return new Set()
}

export function SettingsProvider({ children }) {
  const [theme, setTheme] = useState(getInitialTheme)
  const [uiScale, setUiScale] = useState(getInitialScale)
  const [notificationsEnabled, setNotificationsEnabled] = useState(true)
  const [language, setLanguage] = useState('en')
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [vanieOpen, setVanieOpen] = useState(false)
  const [authOpen, setAuthOpen] = useState(false)
  const [user, setUser] = useState(getInitialUser)

  const [likedIds, setLikedIds] = useState(() => getInitialIdSet('cacun.likes'))
  const [cartIds, setCartIds] = useState(() => getInitialIdSet('cacun.cart'))
  const [activeProductId, setActiveProductId] = useState(null)

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    localStorage.setItem('cacun.theme', theme)
  }, [theme])

  useEffect(() => {
    document.documentElement.style.setProperty('--ui-scale', String(uiScale))
    localStorage.setItem('cacun.uiScale', String(uiScale))
  }, [uiScale])

  useEffect(() => {
    localStorage.setItem('cacun.likes', JSON.stringify(Array.from(likedIds)))
  }, [likedIds])

  useEffect(() => {
    localStorage.setItem('cacun.cart', JSON.stringify(Array.from(cartIds)))
  }, [cartIds])

  const toggleLiked = (id) => {
    setLikedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const toggleCart = (id) => {
    setCartIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const value = useMemo(
    () => ({
      theme,
      setTheme,
      toggleTheme: () => setTheme((t) => (t === 'light' ? 'dark' : 'light')),
      uiScale,
      setUiScale,
      notificationsEnabled,
      setNotificationsEnabled,
      language,
      setLanguage,
      settingsOpen,
      setSettingsOpen,
      vanieOpen,
      setVanieOpen,
      authOpen,
      setAuthOpen,
      user,
      setUser,
      likedIds,
      cartIds,
      likesCount: likedIds.size,
      basketCount: cartIds.size,
      toggleLiked,
      toggleCart,
      activeProductId,
      setActiveProductId,
    }),
    [
      theme,
      uiScale,
      notificationsEnabled,
      language,
      settingsOpen,
      vanieOpen,
      authOpen,
      user,
      likedIds,
      cartIds,
      activeProductId,
    ],
  )

  return (
    <SettingsContext.Provider value={value}>{children}</SettingsContext.Provider>
  )
}
