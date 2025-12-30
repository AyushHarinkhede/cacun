import { useId, useState } from 'react'
import './AuthDrawer.css'

import { useSettings } from '../../contexts/useSettings.js'
import { IconGoogle, IconFacebook, IconApple, IconMicrosoft } from '../icons.jsx'

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
  const [showLoginPassword, setShowLoginPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  const [signupName, setSignupName] = useState('')
  const [signupEmail, setSignupEmail] = useState('')
  const [signupPassword, setSignupPassword] = useState('')
  const [showSignupPassword, setShowSignupPassword] = useState(false)

  const [editName, setEditName] = useState('')
  const [editEmail, setEditEmail] = useState('')
  const [editDob, setEditDob] = useState('')
  const [editAddress, setEditAddress] = useState('')
  const [editMobile, setEditMobile] = useState('')
  const [editPersonalInfo, setEditPersonalInfo] = useState('')
  const [editAvatar, setEditAvatar] = useState(null)

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

    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
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
    const dob = editDob.trim()
    const address = editAddress.trim()
    const mobile = editMobile.trim()
    const personalInfo = editPersonalInfo.trim()

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
      return { ...u, name, email, dob, address, mobile, personalInfo, avatar: editAvatar }
    })

    persistUsers(nextUsers)

    const nextSession = { ...user, name, email, dob, address, mobile, personalInfo, avatar: editAvatar }
    localStorage.setItem('cacun.session', JSON.stringify(nextSession))
    setUser(nextSession)
  }

  const overlayClass = authOpen ? 'authOverlay authOverlayOpen' : 'authOverlay'
  const drawerClass = authOpen ? 'authDrawer authDrawerOpen' : 'authDrawer'

  return (
    <div className={overlayClass}>
      <button className="authBackdrop" type="button" aria-label="Close auth backdrop" onClick={close} />
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
                    <div className="authPasswordWrap">
                      <input 
                        value={loginPassword} 
                        onChange={(e) => setLoginPassword(e.target.value)} 
                        type={showLoginPassword ? 'text' : 'password'} 
                        placeholder="min 8 characters" 
                        minLength={8}
                      />
                      <button 
                        type="button" 
                        className="authPasswordToggle"
                        onClick={() => setShowLoginPassword(!showLoginPassword)}
                        aria-label={showLoginPassword ? 'Hide password' : 'Show password'}
                      >
                        {showLoginPassword ? '👁️' : '👁️‍🗨️'}
                      </button>
                    </div>
                  </label>
                  <label className="authCheckbox">
                    <input 
                      type="checkbox" 
                      checked={rememberMe} 
                      onChange={(e) => setRememberMe(e.target.checked)} 
                    />
                    <span>Remember me</span>
                  </label>
                  <div className="authSocial">
                    <button className="authSocialBtn" type="button" aria-label="Sign in with Google">
                      <IconGoogle /> Google
                    </button>
                    <button className="authSocialBtn" type="button" aria-label="Sign in with Facebook">
                      <IconFacebook /> Facebook
                    </button>
                    <button className="authSocialBtn" type="button" aria-label="Sign in with Apple">
                      <IconApple /> Apple
                    </button>
                    <button className="authSocialBtn" type="button" aria-label="Sign in with Microsoft">
                      <IconMicrosoft /> Microsoft
                    </button>
                  </div>
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
                    <div className="authPasswordWrap">
                      <input 
                        value={signupPassword} 
                        onChange={(e) => setSignupPassword(e.target.value)} 
                        type={showSignupPassword ? 'text' : 'password'} 
                        placeholder="min 8 characters" 
                        minLength={8}
                      />
                      <button 
                        type="button" 
                        className="authPasswordToggle"
                        onClick={() => setShowSignupPassword(!showSignupPassword)}
                        aria-label={showSignupPassword ? 'Hide password' : 'Show password'}
                      >
                        {showSignupPassword ? '👁️' : '👁️‍🗨️'}
                      </button>
                    </div>
                  </label>
                  <div className="authSocial">
                    <button className="authSocialBtn" type="button" aria-label="Sign up with Google">
                      <IconGoogle /> Google
                    </button>
                    <button className="authSocialBtn" type="button" aria-label="Sign up with Facebook">
                      <IconFacebook /> Facebook
                    </button>
                    <button className="authSocialBtn" type="button" aria-label="Sign up with Apple">
                      <IconApple /> Apple
                    </button>
                    <button className="authSocialBtn" type="button" aria-label="Sign up with Microsoft">
                      <IconMicrosoft /> Microsoft
                    </button>
                  </div>
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
                <div className="authAvatarSection">
                  <div className="authAvatarPreview">
                    {editAvatar ? (
                      <img src={URL.createObjectURL(editAvatar)} alt="Avatar" className="authAvatarImg" />
                    ) : user?.avatar ? (
                      <img src={user.avatar} alt="Avatar" className="authAvatarImg" />
                    ) : (
                      <div className="authAvatarPlaceholder">Avatar</div>
                    )}
                  </div>
                  <label className="authAvatarBtn">
                    Upload image
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) setEditAvatar(file)
                      }}
                      style={{ display: 'none' }}
                    />
                  </label>
                </div>
                <label className="authField">
                  <span>Name</span>
                  <input
                    value={editName || ''}
                    onChange={(e) => setEditName(e.target.value)}
                    placeholder="Your name"
                  />
                </label>
                <label className="authField">
                  <span>Email</span>
                  <input
                    value={editEmail || ''}
                    onChange={(e) => setEditEmail(e.target.value)}
                    placeholder="you@example.com"
                  />
                </label>
                <label className="authField">
                  <span>Date of Birth</span>
                  <input
                    type="date"
                    value={editDob || ''}
                    onChange={(e) => setEditDob(e.target.value)}
                  />
                </label>
                <label className="authField">
                  <span>Address</span>
                  <textarea
                    value={editAddress || ''}
                    onChange={(e) => setEditAddress(e.target.value)}
                    rows={2}
                    placeholder="Your address..."
                  />
                </label>
                <label className="authField">
                  <span>Mobile Number</span>
                  <input
                    type="tel"
                    value={editMobile || ''}
                    onChange={(e) => setEditMobile(e.target.value)}
                    placeholder="Your mobile number..."
                  />
                </label>
                <label className="authField">
                  <span>Personal Info</span>
                  <textarea
                    value={editPersonalInfo || ''}
                    onChange={(e) => setEditPersonalInfo(e.target.value)}
                    rows={3}
                    placeholder="Tell us about yourself..."
                  />
                </label>
                <button
                  className="authPrimary"
                  type="button"
                  onClick={onSaveProfile}
                >
                  Save profile
                </button>

                {user?.avatar && (
                  <div className="authProfileCard" style={{ marginTop: '16px' }}>
                    <div className="authProfileTitle">Your Profile</div>
                    <div className="authAvatarSection">
                      <div className="authAvatarPreview">
                        <img src={user.avatar} alt="Profile" className="authAvatarImg" />
                      </div>
                    </div>
                    <div className="authField">
                      <span>Name</span>
                      <div>{user.name}</div>
                    </div>
                    <div className="authField">
                      <span>Email</span>
                      <div>{user.email}</div>
                    </div>
                    {user.dob && (
                      <div className="authField">
                        <span>Date of Birth</span>
                        <div>{user.dob}</div>
                      </div>
                    )}
                    {user.address && (
                      <div className="authField">
                        <span>Address</span>
                        <div>{user.address}</div>
                      </div>
                    )}
                    {user.mobile && (
                      <div className="authField">
                        <span>Mobile Number</span>
                        <div>{user.mobile}</div>
                      </div>
                    )}
                    {user.personalInfo && (
                      <div className="authField">
                        <span>Personal Info</span>
                        <div>{user.personalInfo}</div>
                      </div>
                    )}
                  </div>
                )}
              </div>

              <button className="authDanger" type="button" onClick={onLogout}>
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
