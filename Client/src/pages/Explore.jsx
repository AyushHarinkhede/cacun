import { useState, useMemo, useEffect, useRef } from 'react';
import { Search, MapPin, Calendar, Users, Filter, ChevronLeft, ChevronRight, Heart, Star, Send, User, Loader2, TrendingUp, Globe, Clock, DollarSign, Mountain, TreePine, Waves, Camera, Map, Compass, Home, Plane, Ship, Car, Train } from 'lucide-react';
import CacunContainer from '../components/CacunContainer';

export default function Explore() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [priceRange, setPriceRange] = useState([0, 5000]);
  const [groupSize, setGroupSize] = useState('all');
  const [sortBy, setSortBy] = useState('newest');
  const [currentPage, setCurrentPage] = useState(1);
  const [savedTrips, setSavedTrips] = useState([]);
  const [showFilters, setShowFilters] = useState(false);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [displayedTrips, setDisplayedTrips] = useState([]);
  const observerRef = useRef(null);
  
  const tripsPerPage = 6;
  
  const categories = [
    'All', 'Mountains', 'Beaches', 'Trekking', 'Road Trip', 
    'Cultural', 'Adventure', 'Relaxation', 'City Tour', 'Wildlife',
    'Desert Safari', 'Island Hopping', 'Historical', 'Photography'
  ];

  const trips = [
    {
      id: 1,
      destination: 'Bali, Indonesia',
      date: 'Dec 15-22, 2024',
      budget: 1200,
      groupSize: 4,
      maxGroupSize: 6,
      organizer: 'Mike Johnson',
      organizerAvatar: 'mike',
      rating: 4.8,
      description: 'Exploring temples, beaches, and rice terraces. Looking for adventure buddies!',
      category: 'Beaches',
      image: 'bali-temple',
      tags: ['Adventure', 'Culture', 'Beach'],
      createdAt: new Date('2024-11-01'),
      difficulty: 'Moderate',
      accommodation: 'Hotel',
      transport: 'Flight',
    },
    {
      id: 2,
      destination: 'Tokyo, Japan',
      date: 'Jan 8-15, 2025',
      budget: 2000,
      groupSize: 2,
      maxGroupSize: 4,
      organizer: 'Yuki Tanaka',
      organizerAvatar: 'yuki',
      rating: 4.9,
      description: 'Cherry blossom season! Food tours, temples, and modern culture.',
      category: 'Cultural',
      image: 'tokyo-cherry',
      tags: ['Culture', 'Food', 'Photography'],
      createdAt: new Date('2024-10-15'),
      difficulty: 'Easy',
      accommodation: 'Ryokan',
      transport: 'Train',
    },
    {
      id: 3,
      destination: 'Paris, France',
      date: 'Feb 14-21, 2025',
      budget: 1800,
      groupSize: 3,
      maxGroupSize: 5,
      organizer: 'Marie Dubois',
      organizerAvatar: 'marie',
      rating: 4.7,
      description: 'Romantic getaway with museums, cafes, and day trips to Versailles.',
      category: 'Cultural',
      image: 'paris-eiffel',
      tags: ['Romance', 'Art', 'History'],
      createdAt: new Date('2024-10-20'),
      difficulty: 'Easy',
      accommodation: 'Boutique Hotel',
      transport: 'Metro',
    },
    {
      id: 4,
      destination: 'Swiss Alps',
      date: 'Mar 10-17, 2025',
      budget: 2500,
      groupSize: 5,
      maxGroupSize: 8,
      organizer: 'Hans Mueller',
      organizerAvatar: 'hans',
      rating: 4.9,
      description: 'Skiing, snowboarding, and mountain hiking in the heart of Alps.',
      category: 'Mountains',
      image: 'swiss-alps',
      tags: ['Skiing', 'Adventure', 'Nature'],
      createdAt: new Date('2024-09-25'),
      difficulty: 'Hard',
      accommodation: 'Mountain Lodge',
      transport: 'Train',
    },
    {
      id: 5,
      destination: 'Santorini, Greece',
      date: 'Apr 5-12, 2025',
      budget: 1600,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Elena Papadopoulos',
      organizerAvatar: 'elena',
      rating: 4.8,
      description: 'White buildings, blue domes, and stunning sunsets over the Aegean.',
      category: 'Beaches',
      image: 'santorini-sunset',
      tags: ['Beach', 'Romance', 'Photography'],
      createdAt: new Date('2024-09-30'),
      difficulty: 'Easy',
      accommodation: 'Villa',
      transport: 'Ferry',
    },
    {
      id: 6,
      destination: 'Machu Picchu, Peru',
      date: 'May 20-27, 2025',
      budget: 2200,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Carlos Rodriguez',
      organizerAvatar: 'carlos',
      rating: 4.9,
      description: 'Ancient Inca trail, sacred valley, and sunrise at Machu Picchu.',
      category: 'Trekking',
      image: 'machu-picchu',
      tags: ['Trekking', 'History', 'Adventure'],
      createdAt: new Date('2024-09-10'),
      difficulty: 'Hard',
      accommodation: 'Camping',
      transport: 'Bus',
    },
    {
      id: 7,
      destination: 'Dubai, UAE',
      date: 'Jun 1-8, 2025',
      budget: 2800,
      groupSize: 2,
      maxGroupSize: 4,
      organizer: 'Ahmed Al-Mansoori',
      organizerAvatar: 'ahmed',
      rating: 4.6,
      description: 'Modern architecture, luxury shopping, and desert safari adventures.',
      category: 'City Tour',
      image: 'dubai-skyline',
      tags: ['Luxury', 'Shopping', 'Adventure'],
      createdAt: new Date('2024-08-25'),
      difficulty: 'Easy',
      accommodation: '5-Star Hotel',
      transport: 'Taxi',
    },
    {
      id: 8,
      destination: 'Iceland',
      date: 'Jul 15-22, 2025',
      budget: 3000,
      groupSize: 6,
      maxGroupSize: 10,
      organizer: 'Bjorn Eriksson',
      organizerAvatar: 'bjorn',
      rating: 5.0,
      description: 'Northern lights, glaciers, hot springs, and dramatic landscapes.',
      category: 'Adventure',
      image: 'iceland-aurora',
      tags: ['Nature', 'Photography', 'Adventure'],
      createdAt: new Date('2024-08-15'),
      difficulty: 'Moderate',
      accommodation: 'Guesthouse',
      transport: 'Rental Car',
    },
    {
      id: 9,
      destination: 'Rajasthan, India',
      date: 'Oct 1-10, 2024',
      budget: 1500,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Raj Singh',
      organizerAvatar: 'raj',
      rating: 4.7,
      description: 'Royal palaces, desert safari, and colorful festivals in the land of kings.',
      category: 'Cultural',
      image: 'rajasthan-palace',
      tags: ['Culture', 'History', 'Desert Safari'],
      createdAt: new Date('2024-07-20'),
      difficulty: 'Easy',
      accommodation: 'Heritage Hotel',
      transport: 'Train',
    },
    {
      id: 10,
      destination: 'Kerala Backwaters, India',
      date: 'Nov 15-22, 2024',
      budget: 1200,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Priya Nair',
      organizerAvatar: 'priya',
      rating: 4.8,
      description: 'Houseboat cruise through serene backwaters, tea gardens, and Ayurvedic wellness.',
      category: 'Relaxation',
      image: 'kerala-backwaters',
      tags: ['Relaxation', 'Nature', 'Wellness'],
      createdAt: new Date('2024-07-10'),
      difficulty: 'Easy',
      accommodation: 'Houseboat',
      transport: 'Car',
    },
    {
      id: 11,
      destination: 'Himalayan Trek, Nepal',
      date: 'Sep 5-15, 2024',
      budget: 1800,
      groupSize: 5,
      maxGroupSize: 12,
      organizer: 'Tenzin Sherpa',
      organizerAvatar: 'tenzin',
      rating: 4.9,
      description: 'Everest Base Camp trek, Sherpa culture, and mountain monasteries.',
      category: 'Mountains',
      image: 'everest-basecamp',
      tags: ['Trekking', 'Adventure', 'Mountains'],
      createdAt: new Date('2024-06-25'),
      difficulty: 'Hard',
      accommodation: 'Tea House',
      transport: 'Flight',
    },
    {
      id: 12,
      destination: 'Serengeti Safari, Tanzania',
      date: 'Aug 10-18, 2024',
      budget: 3500,
      groupSize: 6,
      maxGroupSize: 10,
      organizer: 'Joseph Mwangi',
      organizerAvatar: 'joseph',
      rating: 4.8,
      description: 'Big Five wildlife safari, Maasai villages, and Ngorongoro crater.',
      category: 'Wildlife',
      image: 'serengeti-safari',
      tags: ['Wildlife', 'Safari', 'Adventure'],
      createdAt: new Date('2024-06-15'),
      difficulty: 'Moderate',
      accommodation: 'Safari Lodge',
      transport: '4x4 Vehicle',
    },
    {
      id: 13,
      destination: 'Cape Town & Winelands, South Africa',
      date: 'Mar 20-28, 2025',
      budget: 2200,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Thabo Mbeki',
      organizerAvatar: 'thabo',
      rating: 4.6,
      description: 'Table Mountain, wine tasting, and penguin colonies at Cape Point.',
      category: 'City Tour',
      image: 'cape-town-table',
      tags: ['City Tour', 'Wine', 'Nature'],
      createdAt: new Date('2024-06-10'),
      difficulty: 'Easy',
      accommodation: 'Guest House',
      transport: 'Car Rental',
    },
    {
      id: 14,
      destination: 'Marrakech to Sahara, Morocco',
      date: 'Apr 10-17, 2025',
      budget: 1600,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Youssef Alami',
      organizerAvatar: 'youssef',
      rating: 4.7,
      description: 'Medina markets, Atlas Mountains, and camel trek in Sahara Desert.',
      category: 'Desert Safari',
      image: 'sahara-desert',
      tags: ['Desert Safari', 'Culture', 'Adventure'],
      createdAt: new Date('2024-05-28'),
      difficulty: 'Moderate',
      accommodation: 'Desert Camp',
      transport: '4x4',
    },
    {
      id: 15,
      destination: 'Maldives Island Hopping',
      date: 'Dec 1-8, 2024',
      budget: 2800,
      groupSize: 2,
      maxGroupSize: 4,
      organizer: 'Ahmed Hassan',
      organizerAvatar: 'ahmed',
      rating: 4.9,
      description: 'Overwater villas, coral reefs, and pristine beaches in paradise islands.',
      category: 'Island Hopping',
      image: 'maldives-islands',
      tags: ['Island Hopping', 'Beach', 'Luxury'],
      createdAt: new Date('2024-05-20'),
      difficulty: 'Easy',
      accommodation: 'Overwater Villa',
      transport: 'Seaplane',
    },
    {
      id: 16,
      destination: 'Angkor Wat & Siem Reap, Cambodia',
      date: 'Jan 20-27, 2025',
      budget: 1300,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Sokha Chan',
      organizerAvatar: 'sokha',
      rating: 4.8,
      description: 'Ancient temples, floating villages, and Khmer culture exploration.',
      category: 'Historical',
      image: 'angkor-wat',
      tags: ['Historical', 'Culture', 'Photography'],
      createdAt: new Date('2024-05-15'),
      difficulty: 'Easy',
      accommodation: 'Boutique Hotel',
      transport: 'Tuk-Tuk',
    },
    {
      id: 17,
      destination: 'Northern Lights, Norway',
      date: 'Feb 10-17, 2025',
      budget: 2500,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Lars Andersen',
      organizerAvatar: 'lars',
      rating: 4.9,
      description: 'Aurora hunting, fjord cruising, and Sami culture in Arctic Circle.',
      category: 'Photography',
      image: 'norway-aurora',
      tags: ['Photography', 'Nature', 'Adventure'],
      createdAt: new Date('2024-05-10'),
      difficulty: 'Moderate',
      accommodation: 'Arctic Lodge',
      transport: 'Dog Sled',
    },
    {
      id: 18,
      destination: 'Varanasi & Ganges, India',
      date: 'Mar 1-7, 2025',
      budget: 1000,
      groupSize: 5,
      maxGroupSize: 10,
      organizer: 'Anand Sharma',
      organizerAvatar: 'anand',
      rating: 4.6,
      description: 'Spiritual Ganges ceremonies, ancient temples, and traditional culture.',
      category: 'Cultural',
      image: 'varanasi-ghats',
      tags: ['Spiritual', 'Culture', 'History'],
      createdAt: new Date('2024-05-05'),
      difficulty: 'Easy',
      accommodation: 'Heritage Hotel',
      transport: 'Train',
    },
    {
      id: 19,
      destination: 'Victoria Falls, Zambia/Zimbabwe',
      date: 'Jun 15-22, 2025',
      budget: 2000,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Chipo Banda',
      organizerAvatar: 'chipo',
      rating: 4.7,
      description: 'Devil\'s Pool, bungee jumping, and Zambezi river adventures.',
      category: 'Adventure',
      image: 'victoria-falls',
      tags: ['Adventure', 'Nature', 'Extreme Sports'],
      createdAt: new Date('2024-04-28'),
      difficulty: 'Moderate',
      accommodation: 'Safari Lodge',
      transport: 'Small Aircraft',
    },
    {
      id: 20,
      destination: 'Bali Rice Terraces, Indonesia',
      date: 'Sep 20-27, 2025',
      budget: 1400,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Made Wijaya',
      organizerAvatar: 'made',
      rating: 4.8,
      description: 'Tegallalang rice terraces, traditional villages, and temple ceremonies.',
      category: 'Cultural',
      image: 'bali-rice-terraces',
      tags: ['Culture', 'Photography', 'Nature'],
      createdAt: new Date('2024-04-20'),
      difficulty: 'Moderate',
      accommodation: 'Homestay',
      transport: 'Scooter',
    },
    {
      id: 21,
      destination: 'Kyoto Temples, Japan',
      date: 'Apr 1-8, 2025',
      budget: 2200,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Hiroshi Tanaka',
      organizerAvatar: 'hiroshi',
      rating: 4.9,
      description: 'Ancient temples, traditional gardens, and geisha district experiences.',
      category: 'Cultural',
      image: 'kyoto-temples',
      tags: ['Culture', 'History', 'Photography'],
      createdAt: new Date('2024-04-15'),
      difficulty: 'Easy',
      accommodation: 'Ryokan',
      transport: 'Train',
    },
    {
      id: 22,
      destination: 'Swiss Alps Hiking, Switzerland',
      date: 'Jul 10-20, 2025',
      budget: 2800,
      groupSize: 6,
      maxGroupSize: 12,
      organizer: 'Fritz Weber',
      organizerAvatar: 'fritz',
      rating: 4.8,
      description: 'Alpine hiking, glacier lakes, and mountain village exploration.',
      category: 'Mountains',
      image: 'swiss-hiking',
      tags: ['Hiking', 'Mountains', 'Nature'],
      createdAt: new Date('2024-04-10'),
      difficulty: 'Hard',
      accommodation: 'Mountain Hut',
      transport: 'Cable Car',
    },
    {
      id: 23,
      destination: 'Amazon Rainforest, Brazil',
      date: 'Oct 5-15, 2025',
      budget: 2500,
      groupSize: 5,
      maxGroupSize: 10,
      organizer: 'Carlos Silva',
      organizerAvatar: 'carlos',
      rating: 4.7,
      description: 'Jungle trekking, wildlife spotting, and river boat adventures.',
      category: 'Wildlife',
      image: 'amazon-rainforest',
      tags: ['Wildlife', 'Adventure', 'Nature'],
      createdAt: new Date('2024-04-05'),
      difficulty: 'Moderate',
      accommodation: 'Eco Lodge',
      transport: 'River Boat',
    },
    {
      id: 24,
      destination: 'Greek Islands Hopping',
      date: 'Jun 15-25, 2025',
      budget: 2000,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Elena Papadopoulos',
      organizerAvatar: 'elena',
      rating: 4.8,
      description: 'Mykonos, Santorini, and Crete island hopping with beaches.',
      category: 'Island Hopping',
      image: 'greek-islands',
      tags: ['Island Hopping', 'Beach', 'Culture'],
      createdAt: new Date('2024-03-28'),
      difficulty: 'Easy',
      accommodation: 'Beach Hotel',
      transport: 'Ferry',
    },
    {
      id: 25,
      destination: 'Egypt Pyramids & Nile, Egypt',
      date: 'Mar 10-18, 2025',
      budget: 1800,
      groupSize: 6,
      maxGroupSize: 12,
      organizer: 'Ahmed Hassan',
      organizerAvatar: 'ahmed',
      rating: 4.6,
      description: 'Ancient pyramids, Nile cruise, and Cairo market exploration.',
      category: 'Historical',
      image: 'egypt-pyramids',
      tags: ['History', 'Culture', 'Adventure'],
      createdAt: new Date('2024-03-20'),
      difficulty: 'Easy',
      accommodation: 'Hotel',
      transport: 'Bus',
    },
    {
      id: 26,
      destination: 'New Zealand Road Trip',
      date: 'Jan 5-15, 2025',
      budget: 2400,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Jack Wilson',
      organizerAvatar: 'jack',
      rating: 4.9,
      description: 'South Island scenic drives, fjords, and adventure activities.',
      category: 'Road Trip',
      image: 'newzealand-roadtrip',
      tags: ['Road Trip', 'Nature', 'Adventure'],
      createdAt: new Date('2024-03-15'),
      difficulty: 'Moderate',
      accommodation: 'Campervan',
      transport: 'Rental Car',
    },
    {
      id: 27,
      destination: 'Goa Beaches, India',
      date: 'Dec 20-27, 2024',
      budget: 1100,
      groupSize: 5,
      maxGroupSize: 10,
      organizer: 'Rohit Desai',
      organizerAvatar: 'rohit',
      rating: 4.7,
      description: 'Beach parties, water sports, and Portuguese heritage exploration.',
      category: 'Beaches',
      image: 'goa-beaches',
      tags: ['Beach', 'Party', 'Culture'],
      createdAt: new Date('2024-03-10'),
      difficulty: 'Easy',
      accommodation: 'Beach Resort',
      transport: 'Flight',
    },
    {
      id: 28,
      destination: 'Istanbul & Cappadocia, Turkey',
      date: 'May 1-10, 2025',
      budget: 1600,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Mehmet Yilmaz',
      organizerAvatar: 'mehmet',
      rating: 4.8,
      description: 'Historic mosques, hot air balloons, and bazaar shopping.',
      category: 'Cultural',
      image: 'istanbul-cappadocia',
      tags: ['Culture', 'History', 'Photography'],
      createdAt: new Date('2024-03-05'),
      difficulty: 'Moderate',
      accommodation: 'Boutique Hotel',
      transport: 'Hot Air Balloon',
    },
    {
      id: 29,
      destination: 'Canadian Rockies, Canada',
      date: 'Aug 15-25, 2025',
      budget: 2600,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Mike Thompson',
      organizerAvatar: 'mike',
      rating: 4.9,
      description: 'Banff, Lake Louise, and glacier hiking in Rocky Mountains.',
      category: 'Mountains',
      image: 'canadian-rockies',
      tags: ['Mountains', 'Nature', 'Hiking'],
      createdAt: new Date('2024-02-28'),
      difficulty: 'Moderate',
      accommodation: 'Mountain Lodge',
      transport: 'Rental Car',
    },
    {
      id: 30,
      destination: 'Dubai Desert Safari, UAE',
      date: 'Nov 10-17, 2025',
      budget: 2100,
      groupSize: 6,
      maxGroupSize: 12,
      organizer: 'Khalid Al-Mansoori',
      organizerAvatar: 'khalid',
      rating: 4.7,
      description: 'Dune bashing, camel riding, and Bedouin camp experience.',
      category: 'Desert Safari',
      image: 'dubai-desert',
      tags: ['Desert Safari', 'Adventure', 'Culture'],
      createdAt: new Date('2024-02-20'),
      difficulty: 'Moderate',
      accommodation: 'Desert Camp',
      transport: '4x4 Vehicle',
    },
    {
      id: 31,
      destination: 'Peruvian Andes Trek, Peru',
      date: 'Sep 1-12, 2025',
      budget: 1900,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Maria Rodriguez',
      organizerAvatar: 'maria',
      rating: 4.8,
      description: 'Andean mountain trekking, Inca ruins, and traditional village visits.',
      category: 'Mountains',
      image: 'peruvian-andes',
      tags: ['Trekking', 'Mountains', 'Culture'],
      createdAt: new Date('2024-02-15'),
      difficulty: 'Hard',
      accommodation: 'Mountain Lodge',
      transport: 'Bus',
    },
    {
      id: 32,
      destination: 'Bora Bora, French Polynesia',
      date: 'Dec 5-12, 2025',
      budget: 3500,
      groupSize: 2,
      maxGroupSize: 4,
      organizer: 'Tahiti Tours',
      organizerAvatar: 'tahiti',
      rating: 4.9,
      description: 'Overwater bungalows, coral reefs, and tropical paradise.',
      category: 'Island Hopping',
      image: 'bora-bora',
      tags: ['Island Hopping', 'Luxury', 'Beach'],
      createdAt: new Date('2024-02-10'),
      difficulty: 'Easy',
      accommodation: 'Overwater Bungalow',
      transport: 'Seaplane',
    },
    {
      id: 33,
      destination: 'Mongolian Steppe Adventure',
      date: 'Jul 15-25, 2025',
      budget: 2200,
      groupSize: 6,
      maxGroupSize: 12,
      organizer: 'Batbayar',
      organizerAvatar: 'batbayar',
      rating: 4.6,
      description: 'Nomadic culture, Gobi desert camping, and horseback riding.',
      category: 'Cultural',
      image: 'mongolian-steppe',
      tags: ['Culture', 'Adventure', 'Desert Safari'],
      createdAt: new Date('2024-02-05'),
      difficulty: 'Moderate',
      accommodation: 'Ger Camp',
      transport: '4x4 Vehicle',
    },
    {
      id: 34,
      destination: 'Norwegian Fjords Cruise',
      date: 'Jun 10-20, 2025',
      budget: 2800,
      groupSize: 8,
      maxGroupSize: 15,
      organizer: 'Fjord Tours',
      organizerAvatar: 'fjord',
      rating: 4.8,
      description: 'Scenic fjord cruising, midnight sun, and coastal village visits.',
      category: 'Adventure',
      image: 'norwegian-fjords',
      tags: ['Cruise', 'Nature', 'Photography'],
      createdAt: new Date('2024-01-28'),
      difficulty: 'Easy',
      accommodation: 'Cruise Ship',
      transport: 'Ferry',
    },
    {
      id: 35,
      destination: 'Madagascar Wildlife Safari',
      date: 'Oct 1-10, 2025',
      budget: 2400,
      groupSize: 5,
      maxGroupSize: 10,
      organizer: 'Lala Rabe',
      organizerAvatar: 'lala',
      rating: 4.7,
      description: 'Lemurs, baobab trees, and unique endemic wildlife.',
      category: 'Wildlife',
      image: 'madagascar-wildlife',
      tags: ['Wildlife', 'Nature', 'Adventure'],
      createdAt: new Date('2024-01-25'),
      difficulty: 'Moderate',
      accommodation: 'Eco Lodge',
      transport: '4x4 Vehicle',
    },
    {
      id: 36,
      destination: 'Patagonia Trek, Argentina/Chile',
      date: 'Jan 15-28, 2025',
      budget: 2600,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Patagonia Adventures',
      organizerAvatar: 'patagonia',
      rating: 4.9,
      description: 'Glacier hiking, Torres del Paine, and rugged wilderness.',
      category: 'Mountains',
      image: 'patagonia-trek',
      tags: ['Trekking', 'Mountains', 'Adventure'],
      createdAt: new Date('2024-01-20'),
      difficulty: 'Hard',
      accommodation: 'Refugio',
      transport: 'Bus',
    },
    {
      id: 37,
      destination: 'Sri Lanka Tea Country',
      date: 'Mar 5-12, 2025',
      budget: 1400,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Tea Gardens Tours',
      organizerAvatar: 'tea',
      rating: 4.6,
      description: 'Tea plantation visits, ancient cities, and Buddhist temples.',
      category: 'Cultural',
      image: 'sri-lanka-tea',
      tags: ['Culture', 'Nature', 'Relaxation'],
      createdAt: new Date('2024-01-15'),
      difficulty: 'Easy',
      accommodation: 'Tea Estate',
      transport: 'Train',
    },
    {
      id: 38,
      destination: 'Ireland Road Trip',
      date: 'May 20-30, 2025',
      budget: 1800,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Celtic Tours',
      organizerAvatar: 'celtic',
      rating: 4.7,
      description: 'Castles, coastal drives, pub culture, and green landscapes.',
      category: 'Road Trip',
      image: 'ireland-roadtrip',
      tags: ['Road Trip', 'Culture', 'History'],
      createdAt: new Date('2024-01-10'),
      difficulty: 'Moderate',
      accommodation: 'Boutique Hotel',
      transport: 'Rental Car',
    },
    {
      id: 39,
      destination: 'Jordan Petra & Wadi Rum',
      date: 'Apr 10-18, 2025',
      budget: 1700,
      groupSize: 5,
      maxGroupSize: 10,
      organizer: 'Desert Rose Tours',
      organizerAvatar: 'petra',
      rating: 4.8,
      description: 'Ancient Petra city, Wadi Rum desert camping, and Bedouin culture.',
      category: 'Historical',
      image: 'jordan-petra',
      tags: ['History', 'Desert Safari', 'Adventure'],
      createdAt: new Date('2024-01-05'),
      difficulty: 'Moderate',
      accommodation: 'Desert Camp',
      transport: '4x4 Vehicle',
    },
    {
      id: 40,
      destination: 'Costa Rica Rainforest',
      date: 'Aug 1-10, 2025',
      budget: 1600,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Pura Vida',
      organizerAvatar: 'pura',
      rating: 4.8,
      description: 'Cloud forest zip-lining, volcano hiking, and wildlife reserves.',
      category: 'Adventure',
      image: 'costa-rica-rainforest',
      tags: ['Adventure', 'Nature', 'Wildlife'],
      createdAt: new Date('2024-01-01'),
      difficulty: 'Moderate',
      accommodation: 'Eco Lodge',
      transport: 'Shuttle Bus',
    },
  ];

  const filteredAndSortedTrips = useMemo(() => {
    let filtered = trips.filter(trip => {
      const matchesCategory = selectedCategory === 'All' || trip.category === selectedCategory;
      const matchesSearch = searchQuery === '' || 
        trip.destination.toLowerCase().includes(searchQuery.toLowerCase()) ||
        trip.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        trip.organizer.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesPrice = trip.budget >= priceRange[0] && trip.budget <= priceRange[1];
      const matchesGroupSize = groupSize === 'all' || 
        (groupSize === 'small' && trip.maxGroupSize <= 4) ||
        (groupSize === 'medium' && trip.maxGroupSize >= 5 && trip.maxGroupSize <= 8) ||
        (groupSize === 'large' && trip.maxGroupSize > 8);
      
      return matchesCategory && matchesSearch && matchesPrice && matchesGroupSize;
    });

    // Sort trips
    filtered.sort((a, b) => {
      switch(sortBy) {
        case 'newest':
          return b.createdAt - a.createdAt;
        case 'oldest':
          return a.createdAt - b.createdAt;
        case 'price-low':
          return a.budget - b.budget;
        case 'price-high':
          return b.budget - a.budget;
        case 'rating':
          return b.rating - a.rating;
        default:
          return 0;
      }
    });

    return filtered;
  }, [trips, selectedCategory, searchQuery, priceRange, groupSize, sortBy]);

  // Pagination
  const totalPages = Math.ceil(filteredAndSortedTrips.length / tripsPerPage);
  const startIndex = (currentPage - 1) * tripsPerPage;
  const paginatedTrips = filteredAndSortedTrips.slice(startIndex, startIndex + tripsPerPage);

  const handleSaveTrip = (tripId) => {
    setSavedTrips(prev => 
      prev.includes(tripId) 
        ? prev.filter(id => id !== tripId)
        : [...prev, tripId]
    );
  };

  const handleRequestToJoin = (tripId) => {
    if (requests.includes(tripId)) {
      alert('You have already requested to join this trip!');
      return;
    }
    setRequests(prev => [...prev, tripId]);
    alert('Request sent successfully! The organizer will contact you soon.');
  };

  // Infinite scroll implementation - Show all trips at once
  useEffect(() => {
    setDisplayedTrips(filteredAndSortedTrips);
    setHasMore(false);
  }, [filteredAndSortedTrips]);

  // Remove infinite scroll - show all trips
  useEffect(() => {
    // No observer needed since we show all trips
  }, []);

  // No load more function needed - all trips are shown

  return (
    <div className="explore-container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      {/* Search Section */}
      <div className="slide-in-up">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-text">Explore Trips</h1>
          <div className="flex items-center gap-4">
            <span className="text-text-muted text-sm">
              {filteredAndSortedTrips.length} trips found
            </span>
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-2 px-4 py-2 bg-card border border-primary/20 rounded-xl text-text hover:bg-primary/10 transition"
            >
              <Filter className="w-4 h-4" />
              Filters
            </button>
          </div>
        </div>
        
        {/* Search Bar */}
        <div className="search-bar mb-6">
          <Search className="search-icon w-5 h-5" />
          <input
            type="text"
            placeholder="Search destinations, descriptions, or organizers..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(1);
            }}
            className="search-input"
          />
        </div>

        {/* Category Pills */}
        <div className="category-pills mb-6">
          {categories.map((category) => (
            <button
              key={category}
              onClick={() => {
                setSelectedCategory(category);
                setCurrentPage(1);
              }}
              className={`category-pill ${selectedCategory === category ? 'active' : ''}`}
            >
              {category}
            </button>
          ))}
        </div>

        {/* Advanced Filters */}
        {showFilters && (
          <CacunContainer className="glass-container mb-6">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <label className="block text-text text-sm font-medium mb-2">Price Range</label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    placeholder="Min"
                    value={priceRange[0]}
                    onChange={(e) => setPriceRange([parseInt(e.target.value) || 0, priceRange[1]])}
                    className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                  />
                  <span className="text-text-muted">-</span>
                  <input
                    type="number"
                    placeholder="Max"
                    value={priceRange[1]}
                    onChange={(e) => setPriceRange([priceRange[0], parseInt(e.target.value) || 5000])}
                    className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-text text-sm font-medium mb-2">Group Size</label>
                <select
                  value={groupSize}
                  onChange={(e) => {
                    setGroupSize(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                >
                  <option value="all">All Sizes</option>
                  <option value="small">Small (4 or less)</option>
                  <option value="medium">Medium (5-8)</option>
                  <option value="large">Large (8+)</option>
                </select>
              </div>
              
              <div>
                <label className="block text-text text-sm font-medium mb-2">Sort By</label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                >
                  <option value="newest">Newest First</option>
                  <option value="oldest">Oldest First</option>
                  <option value="price-low">Price: Low to High</option>
                  <option value="price-high">Price: High to Low</option>
                  <option value="rating">Highest Rated</option>
                </select>
              </div>
              
              <div className="flex items-end">
                <button
                  onClick={() => {
                    setPriceRange([0, 5000]);
                    setGroupSize('all');
                    setSortBy('newest');
                    setSearchQuery('');
                    setSelectedCategory('All');
                    setCurrentPage(1);
                  }}
                  className="w-full px-4 py-2 bg-primary/20 text-primary rounded-lg hover:bg-primary/30 transition"
                >
                  Reset Filters
                </button>
              </div>
            </div>
          </CacunContainer>
        )}
      </div>

      {/* Results */}
      <div className="space-y-6">
        {/* Featured Destinations */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-text mb-4 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-primary" />
            Featured Destinations
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { name: 'India', icon: <Map className="w-6 h-6 text-primary" />, count: 7 },
              { name: 'Southeast Asia', icon: <Compass className="w-6 h-6 text-primary" />, count: 7 },
              { name: 'Europe', icon: <Home className="w-6 h-6 text-primary" />, count: 6 },
              { name: 'Africa', icon: <Globe className="w-6 h-6 text-primary" />, count: 7 },
            ].map((region, index) => (
              <div key={index} className="bg-card border border-primary/20 rounded-xl p-4 text-center hover:bg-primary/10 transition cursor-pointer">
                <div className="mb-2 flex justify-center">{region.icon}</div>
                <div className="font-semibold text-text">{region.name}</div>
                <div className="text-sm text-text-muted">{region.count} trips</div>
              </div>
            ))}
          </div>
        </div>

        {/* Stats Bar */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-card border border-primary/20 rounded-xl p-4">
            <div className="flex items-center gap-2 text-text-muted mb-1">
              <Globe className="w-4 h-4" />
              <span className="text-sm">Total Trips</span>
            </div>
            <div className="text-xl font-bold text-text">{trips.length}</div>
          </div>
          <div className="bg-card border border-primary/20 rounded-xl p-4">
            <div className="flex items-center gap-2 text-text-muted mb-1">
              <Users className="w-4 h-4" />
              <span className="text-sm">Active Travelers</span>
            </div>
            <div className="text-xl font-bold text-text">2,847</div>
          </div>
          <div className="bg-card border border-primary/20 rounded-xl p-4">
            <div className="flex items-center gap-2 text-text-muted mb-1">
              <Star className="w-4 h-4" />
              <span className="text-sm">Avg Rating</span>
            </div>
            <div className="text-xl font-bold text-text">4.8</div>
          </div>
          <div className="bg-card border border-primary/20 rounded-xl p-4">
            <div className="flex items-center gap-2 text-text-muted mb-1">
              <DollarSign className="w-4 h-4" />
              <span className="text-sm">Avg Budget</span>
            </div>
            <div className="text-xl font-bold text-text">${Math.round(trips.reduce((acc, trip) => acc + trip.budget, 0) / trips.length)}</div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {displayedTrips.map((trip, index) => (
            <div 
              key={trip.id} 
              className={`trip-card slide-in-up-delay-${Math.min(index + 1, 3)}`}
            >
              {/* Cover Image */}
              <div className="relative">
                <img 
                  src={`https://picsum.photos/seed/${trip.image}/600/300.jpg`}
                  alt={trip.destination}
                  className="trip-card-image"
                  loading="lazy"
                />
                <div className="absolute top-4 right-4 flex items-center gap-2">
                  <button
                    onClick={() => handleSaveTrip(trip.id)}
                    className={`p-2 rounded-lg backdrop-blur-sm transition ${
                      savedTrips.includes(trip.id)
                        ? 'bg-primary/20 text-primary'
                        : 'bg-black/20 text-white hover:bg-black/30'
                    }`}
                  >
                    <Heart className={`w-4 h-4 ${savedTrips.includes(trip.id) ? 'fill-current' : ''}`} />
                  </button>
                </div>
                <div className="absolute bottom-4 left-4">
                  <span className="px-3 py-1 bg-primary/90 text-white text-sm rounded-full backdrop-blur-sm">
                    {trip.category}
                  </span>
                </div>
                {/* Quick Info Overlay */}
                <div className="absolute top-4 left-4">
                  <div className="flex items-center gap-2 bg-black/60 backdrop-blur-sm rounded-lg px-2 py-1">
                    <Clock className="w-3 h-3 text-white" />
                    <span className="text-white text-xs">{trip.date.split(',')[0]}</span>
                  </div>
                </div>
              </div>

              {/* Trip Content */}
              <div className="space-y-4">
                {/* Header */}
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-xl font-bold text-text mb-1">
                      {trip.destination}
                    </h2>
                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 fill-primary text-primary" />
                        <span className="text-sm text-text">{trip.rating}</span>
                      </div>
                      <span className="text-text-muted text-sm">•</span>
                      <span className="text-text-muted text-sm">by {trip.organizer}</span>
                    </div>
                  </div>
                  <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                    <span className="text-primary font-semibold text-sm">
                      {trip.organizer.slice(0, 1).toUpperCase()}
                    </span>
                  </div>
                </div>

                {/* Description */}
                <p className="text-text-muted text-sm line-clamp-2">
                  {trip.description}
                </p>

                {/* Tags */}
                <div className="flex flex-wrap gap-2">
                  {trip.tags.map((tag, tagIndex) => (
                    <span
                      key={tagIndex}
                      className="px-2 py-1 bg-background/50 border border-white/20 rounded-full text-xs text-text"
                    >
                      {tag}
                    </span>
                  ))}
                </div>

                {/* Trip Details with Icons */}
                <div className="trip-details">
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="flex items-center space-x-2 text-text-muted">
                      <Calendar className="w-4 h-4 text-primary" />
                      <span>{trip.date}</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-muted">
                      <Users className="w-4 h-4 text-primary" />
                      <span>{trip.groupSize}/{trip.maxGroupSize} people</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-muted">
                      <Mountain className="w-4 h-4 text-primary" />
                      <span className="text-text font-medium">{trip.difficulty}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <DollarSign className="w-4 h-4 text-primary" />
                      <span className="trip-budget">${trip.budget}</span>
                    </div>
                  </div>
                </div>

                {/* Additional Features */}
                <div className="flex flex-wrap gap-2 mb-3">
                  {trip.transport && (
                    <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium flex items-center gap-1">
                      {trip.transport === 'Flight' && <Plane className="w-3 h-3" />}
                      {trip.transport === 'Train' && <Train className="w-3 h-3" />}
                      {trip.transport === 'Car' && <Car className="w-3 h-3" />}
                      {trip.transport === '4x4 Vehicle' && <Car className="w-3 h-3" />}
                      {trip.transport === 'Rental Car' && <Car className="w-3 h-3" />}
                      {trip.transport === 'Seaplane' && <Plane className="w-3 h-3" />}
                      {trip.transport === 'Dog Sled' && <Car className="w-3 h-3" />}
                      {trip.transport === 'Small Aircraft' && <Plane className="w-3 h-3" />}
                      {trip.transport === 'Scooter' && <Car className="w-3 h-3" />}
                      {trip.transport === 'Tuk-Tuk' && <Car className="w-3 h-3" />}
                      {trip.transport === 'Metro' && <Train className="w-3 h-3" />}
                      {trip.transport === 'Taxi' && <Car className="w-3 h-3" />}
                      {trip.transport === 'Ferry' && <Ship className="w-3 h-3" />}
                      {trip.transport || 'Transport'}
                    </span>
                  )}
                  {trip.accommodation && (
                    <span className="px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium flex items-center gap-1">
                      <Home className="w-3 h-3" />
                      {trip.accommodation}
                    </span>
                  )}
                  {trip.rating >= 4.8 && (
                    <span className="px-2 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-medium">
                      <Star className="w-3 h-3 inline mr-1" />
                      Top Rated
                    </span>
                  )}
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3">
                  <button 
                    onClick={() => handleRequestToJoin(trip.id)}
                    className={`flex-1 py-3 rounded-xl font-semibold transition ${
                      requests.includes(trip.id)
                        ? 'bg-green-500/20 text-green-500 border border-green-500/30'
                        : 'request-button'
                    }`}
                  >
                    {requests.includes(trip.id) ? 'Request Sent ✓' : 'Request to Join'}
                  </button>
                  <button className="p-3 rounded-xl bg-background/50 border border-white/20 text-text hover:bg-background/70 transition">
                    <Send className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* All trips shown - no load more button needed */}

        {displayedTrips.length === 0 && (
          <div className="text-center py-12">
            <div className="text-text-muted text-lg">
              No trips found matching your criteria.
            </div>
            <p className="text-text-muted mt-2">
              Try adjusting your search or filters.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
