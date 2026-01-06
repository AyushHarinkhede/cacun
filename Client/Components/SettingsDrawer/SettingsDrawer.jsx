import { X, User, Palette, Bell, Shield, HelpCircle, ChevronRight, Moon, Sun } from 'lucide-react'
import { useSettings } from '../../contexts/useSettings.js'

export default function SettingsDrawer() {
  const { settingsOpen, setSettingsOpen, theme, toggleTheme } = useSettings()

  if (!settingsOpen) return null

  return (
    <div className="settingsOverlay" onClick={() => setSettingsOpen(false)}>
      <div className="settingsDrawer" onClick={(e) => e.stopPropagation()}>
        <div className="settingsHeader">
          <h2 className="settingsTitle">Settings</h2>
          <button className="settingsCloseBtn" onClick={() => setSettingsOpen(false)}>
            <X size={20} />
          </button>
        </div>

        <div className="settingsBody">
          <div className="settingsSection">
            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  {theme === 'dark' ? <Moon size={20} /> : <Sun size={20} />}
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
                  onChange={toggleTheme}
                />
                <span className="settingsToggleSlider"></span>
              </label>
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <User size={20} />
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Account</div>
                  <div className="settingsDescription">Manage your profile</div>
                </div>
              </div>
              <ChevronRight size={20} className="settingsArrow" />
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <Palette size={20} />
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Appearance</div>
                  <div className="settingsDescription">Customize theme colors</div>
                </div>
              </div>
              <ChevronRight size={20} className="settingsArrow" />
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <Bell size={20} />
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
                  <Shield size={20} />
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Privacy</div>
                  <div className="settingsDescription">Privacy settings</div>
                </div>
              </div>
              <ChevronRight size={20} className="settingsArrow" />
            </div>

            <div className="settingsItem">
              <div className="settingsItemLeft">
                <div className="settingsIcon">
                  <HelpCircle size={20} />
                </div>
                <div className="settingsContent">
                  <div className="settingsLabel">Help & Support</div>
                  <div className="settingsDescription">Get help and support</div>
                </div>
              </div>
              <ChevronRight size={20} className="settingsArrow" />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
