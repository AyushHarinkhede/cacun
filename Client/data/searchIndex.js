import { products } from './products.js'

const brands = Array.from(new Set(products.map((p) => p.brand))).map((b) => ({
  id: `brand_${b.toLowerCase().replace(/\s+/g, '_')}`,
  label: b,
  type: 'brand',
}))

export const searchIndex = {
  products: products.map((p) => ({
    id: p.id,
    label: `${p.title} (${p.category})`,
    type: 'product',
    brand: p.brand,
  })),
  ngos: [
    { id: 'n1', label: 'Clean Earth Initiative', type: 'ngo' },
    { id: 'n2', label: 'Plastic-Free India', type: 'ngo' },
    { id: 'n3', label: 'Green Rivers Foundation', type: 'ngo' },
  ],
  campaigns: [
    { id: 'c1', label: 'Beach Cleanup Week', type: 'campaign' },
    { id: 'c2', label: 'Zero Plastic Month', type: 'campaign' },
    { id: 'c3', label: 'Plant 1 Tree / Order', type: 'campaign' },
  ],
  brands,
}
