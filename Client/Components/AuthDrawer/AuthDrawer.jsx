import { useId, useState } from 'react'
import './AuthDrawer.css'

import { useSettings } from '../../contexts/useSettings.js'

function safeParse(json) {
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export default function AuthDrawer() {
  const { authOpen, setAuthOpen, user, setUser } = useSettings()

  const titleId = useId()
  const [tab, setTab] = useState('login')

  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')

  const [signupName, setSignupName] = useState('')
  const [signupEmail, setSignupEmail] = useState('')
  const [signupPassword, setSignupPassword] = useState('')

  const [editName, setEditName] = useState('')
  const [editEmail, setEditEmail] = useState('')

  const [error, setError] = useState('')

  const readUsers = () => {
    const raw = localStorage.getItem('cacun.users')
    const parsed = safeParse(raw)
    return Array.isArray(parsed) ? parsed : []
  }

  const persistUsers = (next) => localStorage.setItem('cacun.users', JSON.stringify(next))

  const close = () => setAuthOpen(false)

  const onLogin = () => {
    setError('')
    const email = loginEmail.trim().toLowerCase()
    const password = loginPassword

    if (!isValidEmail(email)) {
      setError('Enter a valid email.')
      return
    }

    const storedUsers = readUsers()
    const match = storedUsers.find((u) => u.email === email && u.password === password)
    if (!match) {
      setError('Invalid credentials.')
      return
    }

    const nextUser = { id: match.id, name: match.name, email: match.email }
    localStorage.setItem('cacun.session', JSON.stringify(nextUser))
    setUser(nextUser)
  }

  const onSignup = () => {
    setError('')

    const name = signupName.trim()
    const email = signupEmail.trim().toLowerCase()
    const password = signupPassword

    if (name.length < 2) {
      setError('Name is too short.')
      return
    }

    if (!isValidEmail(email)) {
      setError('Enter a valid email.')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.')
      return
    }

    const storedUsers = readUsers()
    if (storedUsers.some((u) => u.email === email)) {
      setError('Account already exists. Please login.')
      setTab('login')
      return
    }

    const newUser = {
      id: `u_${Date.now()}`,
      name,
      email,
      password,
    }

    const nextUsers = [newUser, ...storedUsers]
    persistUsers(nextUsers)

    const sessionUser = { id: newUser.id, name: newUser.name, email: newUser.email }
    localStorage.setItem('cacun.session', JSON.stringify(sessionUser))
    setUser(sessionUser)
  }

  const onLogout = () => {
    localStorage.removeItem('cacun.session')
    setUser(null)
    setTab('login')
  }

  const onSaveProfile = () => {
    setError('')

    const name = editName.trim()
    const email = editEmail.trim().toLowerCase()

    if (!user) return

    if (name.length < 2) {
      setError('Name is too short.')
      return
    }

    if (!isValidEmail(email)) {
      setError('Enter a valid email.')
      return
    }

    const storedUsers = readUsers()
    const nextUsers = storedUsers.map((u) => {
      if (u.id !== user.id) return u
      return { ...u, name, email }
    })

    persistUsers(nextUsers)

    const nextSession = { ...user, name, email }
    localStorage.setItem('cacun.session', JSON.stringify(nextSession))
    setUser(nextSession)
  }

  const overlayClass = authOpen ? 'authOverlay authOverlayOpen' : 'authOverlay'
  const drawerClass = authOpen ? 'authDrawer authDrawerOpen' : 'authDrawer'

  return (
    <div className={overlayClass}>
      <div className={drawerClass} aria-labelledby={titleId}>
        <div className="authHeader">
          <div className="authTitle" id={titleId}>
            {user ? 'Profile' : 'Sign in'}
          </div>
          <button className="authClose" type="button" onClick={close} aria-label="Close auth">
            Close
          </button>
        </div>

        <div className="authBody">
          {error ? <div className="authError">{error}</div> : null}

          {!user ? (
            <>
              <div className="authTabs" role="tablist" aria-label="Auth tabs">
                <button
                  className={tab === 'login' ? 'authTab authTabActive' : 'authTab'}
                  type="button"
                  onClick={() => setTab('login')}
                  role="tab"
                  aria-selected={tab === 'login'}
                >
                  Login
                </button>
                <button
                  className={tab === 'signup' ? 'authTab authTabActive' : 'authTab'}
                  type="button"
                  onClick={() => setTab('signup')}
                  role="tab"
                  aria-selected={tab === 'signup'}
                >
                  Signup
                </button>
              </div>

              {tab === 'login' ? (
                <div className="authForm">
                  <label className="authField">
                    <span>Email</span>
                    <input value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} placeholder="you@example.com" />
                  </label>
                  <label className="authField">
                    <span>Password</span>
                    <input value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} type="password" placeholder="••••••••" />
                  </label>
                  <button className="authPrimary" type="button" onClick={onLogin}>
                    Login
                  </button>
                </div>
              ) : (
                <div className="authForm">
                  <label className="authField">
                    <span>Name</span>
                    <input value={signupName} onChange={(e) => setSignupName(e.target.value)} placeholder="Your name" />
                  </label>
                  <label className="authField">
                    <span>Email</span>
                    <input value={signupEmail} onChange={(e) => setSignupEmail(e.target.value)} placeholder="you@example.com" />
                  </label>
                  <label className="authField">
                    <span>Password</span>
                    <input value={signupPassword} onChange={(e) => setSignupPassword(e.target.value)} type="password" placeholder="min 6 characters" />
                  </label>
                  <button className="authPrimary" type="button" onClick={onSignup}>
                    Create account
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="authProfile">
              <div className="authProfileCard">
                <div className="authProfileTitle">Edit profile</div>
                <label className="authField">
                  <span>Name</span>
                  <input
                    value={editName || user.name || ''}
                    onChange={(e) => setEditName(e.target.value)}
                  />
                </label>
                <label className="authField">
                  <span>Email</span>
                  <input
                    value={editEmail || user.email || ''}
                    onChange={(e) => setEditEmail(e.target.value)}
                  />
                </label>
                <button
                  className="authPrimary"
                  type="button"
                  onClick={() => {
                    if (!editName) setEditName(user.name || '')
                    if (!editEmail) setEditEmail(user.email || '')
                    onSaveProfile()
                  }}
                >
                  Save profile
                </button>
              </div>

              <button className="authDanger" type="button" onClick={onLogout}>
                Logout
              </button>
            </div>
          )}
        </div>
      </div>

      <button className="authBackdrop" type="button" aria-label="Close auth backdrop" onClick={close} />
    </div>
  )
}
