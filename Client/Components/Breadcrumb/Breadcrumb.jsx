import './Breadcrumb.css'

export default function Breadcrumb() {
  const path = window.location.hash.slice(1).split('/')
  const items = ['home', ...path].filter(Boolean)

  return (
    <div className="breadcrumbWrap" aria-label="Breadcrumb navigation">
      {items.map((seg, i) => (
        <button
          key={seg}
          className="breadcrumbItem"
          type="button"
          onClick={() => {
            const targetId = seg === 'home' ? 'home' : seg
            const el = document.getElementById(targetId)
            if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
          }}
        >
          {seg}
          {i < items.length - 1 && <span className="breadcrumbSep">›</span>}
        </button>
      ))}
    </div>
  )
}
