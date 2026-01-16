import './ThemeSelector.css'

import { useSettings } from '../../contexts/useSettings.js'

const themes = [
  { id: 'ocean', name: 'Ocean', bgClass: 'ocean', preview: 'linear-gradient(135deg, #0EA5E9 0%, #14B8A6 100%)' },
  { id: 'sunset', name: 'Sunset', bgClass: 'sunset', preview: 'linear-gradient(135deg, #F59E0B 0%, #DC2626 100%)' },
  { id: 'forest', name: 'Forest', bgClass: 'forest', preview: 'linear-gradient(135deg, #10B981 0%, #059669 100%)' },
  { id: 'berry', name: 'Berry', bgClass: 'berry', preview: 'linear-gradient(135deg, #EC4899 0%, #BE185D 100%)' },
  { id: 'midnight', name: 'Midnight', bgClass: 'midnight', preview: 'linear-gradient(135deg, #1E293B 0%, #334155 100%)' },
  { id: 'aurora', name: 'Aurora', bgClass: 'aurora', preview: 'linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%)' },
  { id: 'candy', name: 'Candy', bgClass: 'candy', preview: 'linear-gradient(135deg, #FF6B6B 0%, #FFC93D 100%)' },
  { id: 'volcano', name: 'Volcano', bgClass: 'volcano', preview: 'linear-gradient(135deg, #EF4444 0%, #F59E0B 100%)' }
]

export default function ThemeSelector() {
  const { bgColor, setBgColor } = useSettings()

  return (
    <div className="themeSelector">
      <div className="themeSelectorHeader">
        <h3>Choose Theme</h3>
        <p>Select your preferred color scheme</p>
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
