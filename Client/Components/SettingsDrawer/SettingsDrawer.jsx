import { useId } from 'react'
import './SettingsDrawer.css'

import { useSettings } from '../../contexts/useSettings.js'

export default function SettingsDrawer() {
  const {
    settingsOpen,
    setSettingsOpen,
    theme,
    toggleTheme,
    uiScale,
    setUiScale,
    notificationsEnabled,
    setNotificationsEnabled,
    language,
    setLanguage,
    setAuthOpen,
    bgColor,
    setBgColor,
    setAboutPage,
  } = useSettings()

  const titleId = useId()

  return (
    <div className={settingsOpen ? 'settingsOverlay settingsOverlayOpen' : 'settingsOverlay'}>
      <button className="settingsBackdrop" type="button" aria-label="Close settings backdrop" onClick={() => setSettingsOpen(false)} />
      <div className={settingsOpen ? 'settingsDrawer settingsDrawerOpen' : 'settingsDrawer'} aria-labelledby={titleId}>
        <div className="settingsHeader">
          <div className="settingsTitle" id={titleId}>Settings</div>
          <button className="settingsClose" type="button" onClick={() => setSettingsOpen(false)} aria-label="Close settings">
            Close
          </button>
        </div>

        <div className="settingsBody">
          <div className="settingsSection">
            <div className="settingsSectionTitle">Profile</div>
            <button className="settingsRowBtn" type="button" onClick={() => setAuthOpen(true)}>Edit profile</button>
          </div>

          <div className="settingsSection">
            <div className="settingsSectionTitle">Appearance</div>
            <button className="settingsRowBtn" type="button" onClick={toggleTheme}>
              Theme: {theme === 'light' ? 'Royal Gold' : 'Royal Blue'}
            </button>
            <div className="settingsRow">
              <span className="settingsRowLabel">Background Color</span>
              <div className="colorSelector">
                <button
                  className={`colorOption colorBlue ${bgColor === 'blue' ? 'active' : ''}`}
                  onClick={() => setBgColor('blue')}
                  aria-label="Blue background"
                  title="Blue"
                />
                <button
                  className={`colorOption colorYellow ${bgColor === 'yellow' ? 'active' : ''}`}
                  onClick={() => setBgColor('yellow')}
                  aria-label="Yellow background"
                  title="Yellow"
                />
              </div>
            </div>
            <label className="settingsRow">
              <span className="settingsRowLabel">UI size</span>
              <input
                className="settingsRange"
                type="range"
                min="0.9"
                max="1.15"
                step="0.01"
                value={uiScale}
                onChange={(e) => setUiScale(Number(e.target.value))}
                aria-label="UI size"
              />
            </label>
          </div>

          <div className="settingsSection">
            <div className="settingsSectionTitle">Accessibility</div>
            <label className="settingsRow">
              <span className="settingsRowLabel">Notifications</span>
              <label className="settingsToggle">
                <input
                  type="checkbox"
                  checked={notificationsEnabled}
                  onChange={(e) => setNotificationsEnabled(e.target.checked)}
                  aria-label="Notifications"
                />
                <span className="settingsToggleSlider"></span>
              </label>
            </label>
          </div>

          <div className="settingsSection">
            <div className="settingsSectionTitle">Support</div>
            <button className="settingsRowBtn" type="button" onClick={() => window.open('mailto:support@cacun.com')}>
              📧 Email Support
            </button>
            <button className="settingsRowBtn" type="button" onClick={() => window.open('tel:+919876543210')}>
              📱 Call Us
            </button>
            <button className="settingsRowBtn settingsRowBtnRed" type="button" onClick={() => window.open('#feedback')}>
              💬 Send Feedback
            </button>
          </div>

          <div className="settingsSection">
            <div className="settingsSectionTitle">Account</div>
            <button className="settingsRowBtn" type="button" onClick={() => setAboutPage && setAboutPage('orders')}>
              📦 My Orders
            </button>
            <button className="settingsRowBtn" type="button" onClick={() => setAboutPage && setAboutPage('wishlist')}>
              ❤️ Wishlist
            </button>
            <button className="settingsRowBtn" type="button" onClick={() => setAboutPage && setAboutPage('addresses')}>
              📍 Shipping Addresses
            </button>
            <button className="settingsRowBtn" type="button" onClick={() => setAboutPage && setAboutPage('payment')}>
              💳 Payment Methods
            </button>
          </div>

          <div className="settingsSection">
            <div className="settingsSectionTitle">Preferences</div>
            <label className="settingsRow">
              <span className="settingsRowLabel">Currency</span>
              <select className="settingsSelect">
                <option>₹ INR</option>
                <option>$ USD</option>
                <option>€ EUR</option>
              </select>
            </label>
            <label className="settingsRow">
              <span className="settingsRowLabel">Newsletter</span>
              <label className="settingsToggle">
                <input
                  type="checkbox"
                  defaultChecked={true}
                  aria-label="Newsletter subscription"
                />
                <span className="settingsToggleSlider"></span>
              </label>
            </label>
          </div>
        </div>
      </div>
    </div>
  )
}
