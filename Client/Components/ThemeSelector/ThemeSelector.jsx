import './ThemeSelector.css'

import { useSettings } from '../../contexts/useSettings.js'

const themes = [
  { id: 'bright-blue', name: 'Bright Blue', bgClass: 'bright-blue', preview: '#235789' },
  { id: 'bright-yellow', name: 'Bright Yellow', bgClass: 'bright-yellow', preview: '#FFED29' }
]

export default function ThemeSelector() {
  const { bgColor, setBgColor } = useSettings()

  return (
    <div className="themeSelector">
      <div className="themeSelectorHeader">
        <h3>🎨 Choose Theme</h3>
        <p>Select your preferred color scheme for Cacun</p>
      </div>
      
      <div className="themeGrid">
        {themes.map((theme) => (
          <button
            key={theme.id}
            className={`themeOption ${bgColor === theme.id ? 'active' : ''}`}
            onClick={() => setBgColor(theme.id)}
            title={`Apply ${theme.name} theme`}
          >
            <div 
              className="themePreview" 
              style={{ background: theme.preview }}
            />
            <span className="themeName">{theme.name}</span>
          </button>
        ))}
      </div>
    </div>
  )
}
