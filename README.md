# NyumbaHub 🏠
> Real Estate Marketplace — Kotlin Android App

Connect property owners, landlords, and agents with buyers and renters.

## Master Plan
See `docs/NyumbaHub_Master_Plan.pdf` for the full feature blueprint, tech stack, roadmap, and build checklist.

## Module Structure
| Module | Purpose |
|---|---|
| `:app` | Single Activity, NavGraph, Hilt setup |
| `:core:ui` | Shared Compose components, Material3 theme |
| `:core:network` | Retrofit client, auth interceptor |
| `:core:data` | Repositories, Room DB, Firebase services |
| `:core:domain` | Use cases, domain models, repository interfaces |
| `:feature:auth` | Login, Register, OTP verification |
| `:feature:listings` | Home feed, listing detail |
| `:feature:search` | Search, filters, map view |
| `:feature:post` | Create and edit listings |
| `:feature:chat` | Inquiries and messaging |
| `:feature:subscription` | Plans, payment flow |
| `:feature:profile` | User profile, my listings, settings |

## Getting Started
1. Clone the repo
2. Replace `app/google-services.json` with your real Firebase config
3. Add your Google Maps API key in `AndroidManifest.xml`
4. Run: `.\gradlew assembleDebug`

## Tech Stack
Kotlin · Jetpack Compose · Hilt · Room · Retrofit · Firebase · Coroutines · Flow · Google Maps
