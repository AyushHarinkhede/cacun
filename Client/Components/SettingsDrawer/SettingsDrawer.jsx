import { useSettings } from '../../contexts/useSettings.js'

export default function SettingsDrawer() {
  const { settingsOpen, setSettingsOpen, theme, toggleTheme, bgColor, setBgColor } = useSettings()

  if (!settingsOpen) return null

  return (
    <div className="settingsOverlay" onClick={() => setSettingsOpen(false)}>
      <div className="settingsDrawer" onClick={(e) => e.stopPropagation()}>
        <div className="settingsHeader">
          <h2 className="settingsTitle">Settings</h2>
          <button className="settingsCloseBtn" onClick={() => setSettingsOpen(false)}>
            <span aria-hidden>×</span>
          </button>
        </div>

        <div className="settingsBody">
          <div className="settingsSection">
            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <span aria-hidden>{theme === 'dark' ? '🌙' : '☀'}</span>
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Dark Mode</div>
                  <div className="settingsDescription">Toggle dark/light theme</div>
                </div>
              </div>
              <label className="settingsToggle">
                <input
                  type="checkbox"
                  checked={theme === 'dark'}
                  onChange={() => toggleTheme()}
                />
                <span className="settingsToggleSlider"></span>
              </label>
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <span aria-hidden>🎨</span>
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Theme Color</div>
                  <div className="settingsDescription">Choose your favorite color</div>
                </div>
              </div>
            </div>

            <div className="colorSelectorGrid">
              <button
                className={`colorOption colorPurple ${bgColor === 'purple' ? 'active' : ''}`}
                onClick={() => setBgColor('purple')}
                aria-label="Purple theme"
                title="Purple"
              />
              <button
                className={`colorOption colorBlue ${bgColor === 'blue' ? 'active' : ''}`}
                onClick={() => setBgColor('blue')}
                aria-label="Blue theme"
                title="Blue"
              />
              <button
                className={`colorOption colorPink ${bgColor === 'pink' ? 'active' : ''}`}
                onClick={() => setBgColor('pink')}
                aria-label="Pink theme"
                title="Pink"
              />
              <button
                className={`colorOption colorGreen ${bgColor === 'green' ? 'active' : ''}`}
                onClick={() => setBgColor('green')}
                aria-label="Green theme"
                title="Green"
              />
              <button
                className={`colorOption colorYellow ${bgColor === 'yellow' ? 'active' : ''}`}
                onClick={() => setBgColor('yellow')}
                aria-label="Yellow theme"
                title="Yellow"
              />
              <button
                className={`colorOption colorOrange ${bgColor === 'orange' ? 'active' : ''}`}
                onClick={() => setBgColor('orange')}
                aria-label="Orange theme"
                title="Orange"
              />
              <button
                className={`colorOption colorTeal ${bgColor === 'teal' ? 'active' : ''}`}
                onClick={() => setBgColor('teal')}
                aria-label="Teal theme"
                title="Teal"
              />
              <button
                className={`colorOption colorRed ${bgColor === 'red' ? 'active' : ''}`}
                onClick={() => setBgColor('red')}
                aria-label="Red theme"
                title="Red"
              />
              <button
                className={`colorOption colorIndigo ${bgColor === 'indigo' ? 'active' : ''}`}
                onClick={() => setBgColor('indigo')}
                aria-label="Indigo theme"
                title="Indigo"
              />
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <span aria-hidden>👤</span>
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Account</div>
                  <div className="settingsDescription">Manage your profile</div>
                </div>
              </div>
              <span className="settingsArrow" aria-hidden>›</span>
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <span aria-hidden>🔔</span>
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Notifications</div>
                  <div className="settingsDescription">Manage notifications</div>
                </div>
              </div>
              <label className="settingsToggle">
                <input type="checkbox" defaultChecked />
                <span className="settingsToggleSlider"></span>
              </label>
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <span aria-hidden>🛡</span>
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Privacy</div>
                  <div className="settingsDescription">Privacy settings</div>
                </div>
              </div>
              <span className="settingsArrow" aria-hidden>›</span>
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <span aria-hidden>❓</span>
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Help & Support</div>
                  <div className="settingsDescription">Get help and support</div>
                </div>
              </div>
              <span className="settingsArrow" aria-hidden>›</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
