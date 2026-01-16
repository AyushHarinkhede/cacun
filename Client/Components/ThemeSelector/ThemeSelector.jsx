import './ThemeSelector.css'

import { useSettings } from '../../contexts/useSettings.js'

const themes = [
  { id: 'ocean', name: 'Ocean', bgClass: 'ocean', preview: '#1E40AF' },
  { id: 'sunshine', name: 'Sunshine', bgClass: 'sunshine', preview: '#F59E0B' },
  { id: 'sky', name: 'Sky', bgClass: 'sky', preview: '#0EA5E9' },
  { id: 'gold', name: 'Gold', bgClass: 'gold', preview: '#D97706' },
  { id: 'navy', name: 'Navy', bgClass: 'navy', preview: '#1E3A8A' },
  { id: 'amber', name: 'Amber', bgClass: 'amber', preview: '#F59E0B' },
  { id: 'azure', name: 'Azure', bgClass: 'azure', preview: '#0EA5E9' },
  { id: 'lemon', name: 'Lemon', bgClass: 'lemon', preview: '#FCD34D' }
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
