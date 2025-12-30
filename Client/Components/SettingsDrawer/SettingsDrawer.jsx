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
            <div className="settingsSectionTitle">Language</div>
            <label className="settingsRow">
              <span className="settingsRowLabel">Language</span>
              <select className="settingsSelect" value={language} onChange={(e) => setLanguage(e.target.value)} aria-label="Language">
                <option value="en">English</option>
                <option value="hi">Hindi</option>
              </select>
            </label>
          </div>

          <div className="settingsSection">
            <div className="settingsSectionTitle">Contact</div>
            <button className="settingsRowBtn" type="button">Support</button>
            <button className="settingsRowBtn settingsRowBtnMaroon" type="button">Send feedback</button>
          </div>
        </div>
      </div>
    </div>
  )
}
