import './App.css'

import { SettingsProvider } from './contexts/SettingsContext.jsx'
import { useSettings } from './contexts/useSettings.js'
import Navbar from './Components/Navbar/Navbar.jsx'
import Home from './Components/Home/Home.jsx'
import Footer from './Components/Footer/Footer.jsx'
import FloatingControls from './Components/FloatingControls/FloatingControls.jsx'
import VaniePanel from './Components/VaniePanel/VaniePanel.jsx'
import SettingsDrawer from './Components/SettingsDrawer/SettingsDrawer.jsx'
import AuthDrawer from './Components/AuthDrawer/AuthDrawer.jsx'
import ProductModal from './Components/ProductModal/ProductModal.jsx'
import AboutModal from './Components/AboutModal/AboutModal.jsx'
import LegalModal from './Components/LegalModal/LegalModal.jsx'

function AppContent() {
  const { settingsOpen, authOpen, vanieOpen, activeProductId, aboutPage, legalPage, bgColor } = useSettings()
  const isModalOpen = settingsOpen || authOpen || vanieOpen || activeProductId || aboutPage || legalPage

  return (
    <div className={`appShell ${isModalOpen ? 'overflowHidden' : ''}`} data-bg-color={bgColor}>
      <Navbar />
      <main className="appMain">
        <Home />
      </main>
      <Footer />
      <FloatingControls />
      <VaniePanel />
      <SettingsDrawer />
      <AuthDrawer />
      <ProductModal />
      <AboutModal />
      <LegalModal />
    </div>
  )
}

export default function App() {
  return (
    <SettingsProvider>
      <AppContent />
    </SettingsProvider>
  )
}
