const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccount.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seed() {
  console.log("Seeding Firestore...");

  // ── LISTINGS ──────────────────────────────────────────────
  const listings = [
    {
      title: "Modern 2BR Apartment - Kacyiru",
      description: "Beautiful modern apartment in the heart of Kacyiru with stunning city views. Recently renovated with high-end finishes.",
      type: "RENT", propertyType: "APARTMENT", status: "FEATURED",
      price: 800, currency: "USD", bedrooms: 2, bathrooms: 1, sizeSqm: 85,
      location: { city: "Kigali", district: "Gasabo", neighbourhood: "Kacyiru", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Spacious Villa - Kimihurura",
      description: "Luxurious 4-bedroom villa with private garden, swimming pool and 24h security in prime Kimihurura.",
      type: "SALE", propertyType: "VILLA", status: "FEATURED",
      price: 250000, currency: "USD", bedrooms: 4, bathrooms: 3, sizeSqm: 320,
      location: { city: "Kigali", district: "Gasabo", neighbourhood: "Kimihurura", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Studio Apartment - Remera",
      description: "Cozy studio apartment near MTN Centre. Perfect for young professionals. All utilities included.",
      type: "RENT", propertyType: "APARTMENT", status: "ACTIVE",
      price: 450, currency: "USD", bedrooms: 1, bathrooms: 1, sizeSqm: 45,
      location: { city: "Kigali", district: "Gasabo", neighbourhood: "Remera", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Family House - Nyamirambo",
      description: "Comfortable 3-bedroom family house in quiet Nyamirambo neighbourhood. Large compound with parking.",
      type: "RENT", propertyType: "HOUSE", status: "ACTIVE",
      price: 500, currency: "USD", bedrooms: 3, bathrooms: 2, sizeSqm: 140,
      location: { city: "Kigali", district: "Nyarugenge", neighbourhood: "Nyamirambo", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Penthouse - CBD Kigali",
      description: "Stunning penthouse with 360-degree views of Kigali. Top floor, fully furnished, premium location.",
      type: "RENT", propertyType: "APARTMENT", status: "FEATURED",
      price: 2500, currency: "USD", bedrooms: 3, bathrooms: 2, sizeSqm: 180,
      location: { city: "Kigali", district: "Nyarugenge", neighbourhood: "CBD", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Townhouse - Gisozi",
      description: "Modern 3-bedroom townhouse in gated community. Solar power, borehole water, backup generator.",
      type: "SALE", propertyType: "TOWNHOUSE", status: "ACTIVE",
      price: 120000, currency: "USD", bedrooms: 3, bathrooms: 2, sizeSqm: 180,
      location: { city: "Kigali", district: "Gasabo", neighbourhood: "Gisozi", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Lake View Apartment - Rubavu",
      description: "Beautiful apartment with direct Lake Kivu views. Perfect for expats or holiday rental investment.",
      type: "SALE", propertyType: "APARTMENT", status: "ACTIVE",
      price: 85000, currency: "USD", bedrooms: 2, bathrooms: 1, sizeSqm: 90,
      location: { city: "Rubavu", district: "Rubavu", neighbourhood: "Gisenyi", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Office Space - Kigali Heights",
      description: "Premium office space in Kigali Heights tower. 200sqm open plan with conference room.",
      type: "RENT", propertyType: "COMMERCIAL", status: "ACTIVE",
      price: 3000, currency: "USD", bedrooms: 0, bathrooms: 2, sizeSqm: 200,
      location: { city: "Kigali", district: "Gasabo", neighbourhood: "Kacyiru", country: "Rwanda" },
      photos: ["https://images.unsplash.com/photo-1497366216548-37526070297c?w=800"],
      listerId: "admin", createdAt: Date.now()
    }
  ];

  // ── MOTORS ────────────────────────────────────────────────
  const motors = [
    {
      make: "Toyota", model: "RAV4", year: 2020, category: "SUV",
      price: 28000, currency: "USD", mileage: 45000, fuelType: "Petrol",
      transmission: "Automatic", color: "White", condition: "Used",
      city: "Kigali", description: "Well maintained RAV4 with full service history. Single owner.",
      photos: ["https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      make: "Toyota", model: "Land Cruiser", year: 2019, category: "SUV",
      price: 65000, currency: "USD", mileage: 60000, fuelType: "Diesel",
      transmission: "Automatic", color: "Black", condition: "Used",
      city: "Kigali", description: "V8 Land Cruiser in excellent condition. Full leather interior.",
      photos: ["https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      make: "Volkswagen", model: "Polo", year: 2021, category: "Sedan",
      price: 18000, currency: "USD", mileage: 22000, fuelType: "Petrol",
      transmission: "Manual", color: "Silver", condition: "Used",
      city: "Kigali", description: "Nearly new VW Polo. Fuel efficient, perfect city car.",
      photos: ["https://images.unsplash.com/photo-1471444928139-48c5bf5173f8?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      make: "Toyota", model: "Hilux", year: 2020, category: "Pickup",
      price: 35000, currency: "USD", mileage: 55000, fuelType: "Diesel",
      transmission: "Manual", color: "Blue", condition: "Used",
      city: "Musanze", description: "Double cab Hilux, great for both city and off-road.",
      photos: ["https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=800"],
      listerId: "admin", createdAt: Date.now()
    }
  ];

  // ── AGENTS ────────────────────────────────────────────────
  const agents = [
    {
      name: "Jean Baptiste Habimana", agency: "Kigali Homes Ltd",
      district: "Gasabo", city: "Kigali", phone: "+250788123456",
      totalListings: 24, yearsExperience: 8, rating: 4.8,
      isVerified: true, speciality: "Residential",
      photoUrl: "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=400"
    },
    {
      name: "Marie Claire Uwimana", agency: "Rwanda Real Estate",
      district: "Nyarugenge", city: "Kigali", phone: "+250788234567",
      totalListings: 18, yearsExperience: 5, rating: 4.6,
      isVerified: true, speciality: "Commercial",
      photoUrl: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400"
    },
    {
      name: "Patrick Nkurunziza", agency: "Prime Properties RW",
      district: "Kicukiro", city: "Kigali", phone: "+250788345678",
      totalListings: 31, yearsExperience: 12, rating: 4.9,
      isVerified: true, speciality: "Luxury",
      photoUrl: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400"
    },
    {
      name: "Diane Ingabire", agency: "Affordable Homes RW",
      district: "Gasabo", city: "Kigali", phone: "+250788456789",
      totalListings: 12, yearsExperience: 3, rating: 4.4,
      isVerified: false, speciality: "Residential",
      photoUrl: "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400"
    }
  ];

  // ── ROOMS ─────────────────────────────────────────────────
  const rooms = [
    {
      title: "Self-contained Room - Kacyiru",
      neighbourhood: "Kacyiru", city: "Kigali",
      price: 150, currency: "USD", isShared: false,
      amenities: ["WiFi", "Water", "Security", "Power backup"],
      description: "Clean self-contained room with private bathroom. Near bus stop.",
      photos: ["https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Shared Room - Remera",
      neighbourhood: "Remera", city: "Kigali",
      price: 80, currency: "USD", isShared: true,
      amenities: ["WiFi", "Kitchen", "Water"],
      description: "Shared room in a clean house. 2 people per room. Near MTN Centre.",
      photos: ["https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800"],
      listerId: "admin", createdAt: Date.now()
    },
    {
      title: "Studio Room - Nyamirambo",
      neighbourhood: "Nyamirambo", city: "Kigali",
      price: 200, currency: "USD", isShared: false,
      amenities: ["WiFi", "Water", "Power backup", "Kitchen"],
      description: "Modern studio room with kitchenette. Quiet neighbourhood.",
      photos: ["https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=800"],
      listerId: "admin", createdAt: Date.now()
    }
  ];

  // ── OFF-PLAN ───────────────────────────────────────────────
  const offplan = [
    {
      title: "Kigali Heights Residences",
      developer: "Heights Development Ltd",
      neighbourhood: "Kacyiru", city: "Kigali",
      priceFrom: 75000, currency: "USD",
      completionYear: 2026, completionPercent: 65,
      propertyType: "Apartments", totalUnits: 120, availableUnits: 48,
      description: "Luxury apartments with panoramic city views in the heart of Kigali.",
      photos: ["https://images.unsplash.com/photo-1486325212027-8081e485255e?w=800"]
    },
    {
      title: "Green Valley Estate",
      developer: "Rwanda Urban Homes",
      neighbourhood: "Nyamata", city: "Bugesera",
      priceFrom: 45000, currency: "USD",
      completionYear: 2027, completionPercent: 30,
      propertyType: "Villas", totalUnits: 60, availableUnits: 52,
      description: "Eco-friendly gated community with solar power and green spaces.",
      photos: ["https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800"]
    },
    {
      title: "Rubavu Lakefront Towers",
      developer: "Lake Invest Co",
      neighbourhood: "Gisenyi", city: "Rubavu",
      priceFrom: 90000, currency: "USD",
      completionYear: 2026, completionPercent: 80,
      propertyType: "Apartments", totalUnits: 80, availableUnits: 15,
      description: "Premium lakefront apartments with direct Lake Kivu access.",
      photos: ["https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800"]
    }
  ];

  // ── SEED ALL ───────────────────────────────────────────────
  const batch = db.batch();

  listings.forEach(d => batch.set(db.collection("listings").doc(), d));
  motors.forEach(d => batch.set(db.collection("motors").doc(), d));
  agents.forEach(d => batch.set(db.collection("agents").doc(), d));
  rooms.forEach(d => batch.set(db.collection("rooms").doc(), d));
  offplan.forEach(d => batch.set(db.collection("offplan").doc(), d));

  await batch.commit();
  console.log("✅ Done! Seeded:");
  console.log(`   ${listings.length} listings`);
  console.log(`   ${motors.length} motors`);
  console.log(`   ${agents.length} agents`);
  console.log(`   ${rooms.length} rooms`);
  console.log(`   ${offplan.length} off-plan projects`);
  process.exit(0);
}

seed().catch(e => { console.error(e); process.exit(1); });
