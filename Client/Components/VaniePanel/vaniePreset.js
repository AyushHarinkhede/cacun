export function buildVanieReply(message) {
  const msg = message.trim().toLowerCase()

  if (msg === 'theme dark') {
    return {
      text: 'Done. Switching theme now.',
      action: 'toggleTheme',
    }
  }

  if (msg === 'theme light') {
    return {
      text: 'Done. Switching theme now.',
      action: 'toggleTheme',
    }
  }

  if (msg.includes('plastic') && msg.includes('free')) {
    return {
      text: 'Plastic-free picks: edible spoons & plates, leafy plates, reusable bottles, paper-based pouches, coconut coir scrub, refill detergent capsules.',
    }
  }

  if (msg.includes('recycled')) {
    return {
      text: 'Recycled-material picks: shoes from recycled fabric, carry bags from recycled packaging, recycled cloth socks, boxes/pouches made from reused paper and fibers.',
    }
  }

  if (msg.includes('non toxic') || msg.includes('nontoxic')) {
    return {
      text: 'Non-toxic picks: chemical-free soap, organic skincare, plant-based detergents, safe farm items, plastic-free packaging alternatives.',
    }
  }

  return {
    text: 'I can help you find products by category. Try: plastic free, recycled, non toxic, or type: theme dark.',
  }
}
