# Loot-Gamehub

![Android CI](https://github.com/ST10345327/Loot-Gamehub/actions/workflows/build.yml/badge.svg)

A game discovery and personal library app for Android, built with Kotlin and Jetpack Compose.
Users can search games (IGDB), save them to a personal library, wishlist and favourites,
compare titles and get a random recommendation with **Surprise Me**.

**Module:** OPSC6311 - Part 2: App Prototype Development

## Team

| Student number | Name |
| --- | --- |
| ST10345327 | Olebogeng Phawe |
| ST10449154 | Mbuso Sbusiso Dube |
| ST10437200 | Nkosikhona Dlamini |

## Demo video

https://youtu.be/T6mjEAAWMFs

## Purpose of the app

_The primary reason behind the existence of GameHub lies in addressing a certain challenge in the gaming industry: the increasing difficulty of discovering, researching and managing the video games that come out today in a more and more crowded environment.

Given the number of games that appear each year on multiple platforms (PC, PlayStation, Xbox, Nintendo, mobile), it's easy for gamers to become overwhelmed with all the possibilities of playing different games. GameHub was created to organize this chaos through one platform where users can discover new games, research them, bookmark them and track their progress in gaming.

The Purpose of GameHub
1. Game Discovery Gateway
GameHub acts as a bridge to the world of video games. Using IGDB API (Internet Game Database), GameHub gives its users access to an extensive database about all kinds of games — names, dates of release, genres, platforms, ratings, descriptions, images, developers and publishers in one easy-to-use mobile interface.

2. Game Library Management
Apart from being a platform for discovery, GameHub also lets its users create and manage their personal libraries of games. The games are organized in various statuses like Want to Play, Playing and Completed.

## Design considerations

The app uses the design approach which prioritizes clearness and accessibility.

Visual Identity & UI
Visual Style: The design of the UI uses the elements from Material Design. It includes dark visual theme by default (as seen in mockups) with bright accent color for interactive elements.

Iconography: The icon of the app (Loot Game Center) has dark background with four colored icons (Square, Triangle, Cross, Circle), corresponding to controller buttons.

Typography: Clean sans-serif font. Big bold typography used for names of games, secondary typography used for additional information (game rating, release year, genre).

Screen Structure
The app has linear screen structure:
Splash Screen → Onboarding → Login/Registration → Home

Home/Discover: Screen includes personalized greeting, search bar, and categories for Trending Now, Popular Games and Upcoming Releases.

Game Details: High quality images for covers, game descriptions, rating and buttons to Add to Library, Wishlist, Favourites and Compare games.

Comparison: A separate screen, where a user can select two games and compare their statistics.

Architecture & Technology Stack
Language: Kotlin

Architecture: MVVM (Model-View-ViewModel)

Networking: Retrofit / OkHttp

Local Storage: Room Database

Backend: Custom REST API and MySQL Database

External API: IGDB (Internet Game Database)

## Architecture

MVVM with a repository layer:

```
com.gamehub.app
├── data
│   ├── remote      # Retrofit services (own REST API)
│   ├── local       # Room (offline cache) + DataStore (settings)
│   ├── repository  # single source of truth for the ViewModels
│   └── model       # data classes
├── ui
│   ├── theme       # colours, typography, GameHubTheme
│   ├── navigation  # routes, NavHost, bottom bar
│   ├── components  # reusable composables
│   └── <feature>   # screen + ViewModel per feature
└── util
```

## REST API and database

_Endpoint table, hosting provider, database screenshots. To be completed in Phase 2._

## Features

| Feature | Status |
| --- | --- |
| Project setup, navigation, theme, 3 languages (strings) | In progress |
| Register / login (hashed passwords, token) | Planned |
| IGDB search and game details | Planned |
| Library, wishlist, favourites | Planned |
| Settings (theme, language, sign out) | Planned |
| Game comparison, Surprise Me | Planned |
| Deferred to PoE: Google Sign-In, push notifications, full offline sync | Deferred |

## GitHub and GitHub Actions

Every push and pull request runs `.github/workflows/build.yml`, which runs the unit tests
(`./gradlew testDebugUnitTest`) and builds the debug APK (`./gradlew assembleDebug`).

<img width="1135" height="623" alt="Screenshot_22-9-2026_23546_github com" src="https://github.com/user-attachments/assets/b0a57a1f-867e-4c73-ad29-c7fa14de6ca6" />


## How to run

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync, then run the `app` configuration on a device or emulator.

## AI use statement

As part of the development of this project, Artificial Intelligence (AI) has been used as an aid to support our efforts, rather than replace them. The final outputs, design choices, and coding implementations in the project are the sole works of the team members, who bear full responsibility for the correctness and quality of the submitted project.

AI Usage
AI tools (like language models) have been used in the following capacities:

Brainstorming/Ideation: AI was used to come up with ideas related to the features of the project and different approaches to solving the design problems. They were then critiqued and modified to include them in the project.

Improving Grammatical Language: AI was used to proofread and improve the written documents of the project, including this planning and design document.

Coding Help and Error Troubleshooting: AI was used to give us tips on error troubleshooting, coding, and Android development, and the best REST API design practices.

Documentation Help: AI gave suggestions regarding the structure and formatting of documentation, such as this README file and this planning document.

Non-use of AI
For maintaining academic integrity and authentic submission of work:

AI did not create any of the core code of the application, UI, and architecture without substantial human intervention and modification.

AI was not responsible for making any design or implementation decisions. Any design or implementation decision was made after thorough research and testing by the team.

AI was not responsible for writing any part of the document which was then reviewed, edited, and validated by humans.

AI was not responsible for generating false data, references or results. All the research and testing data are real.

Responsibilities of the Team Members
It is acknowledged by all members of the team that:

We know the code and content we submit.

We can justify all our design and implementation decisions.

We have verified the accuracy and appropriateness of any content that may be generated using AI assistance.

We take complete responsibility for our work.

Ethical Considerations
We are aware of the ethical implications of the usage of AI in academic and professional contexts. We have used AI responsibly in a manner consistent with the policies of our academic institution for academic integrity.

## References

- Android Developers (2026) *Navigation with Compose*. Available at: https://developer.android.com/develop/ui/compose/navigation
- Android Developers (2026) *Material Design 3 in Compose*. Available at: https://developer.android.com/develop/ui/compose/designsystems/material3
- IGDB (2026) *IGDB API Documentation*. Available at: https://api-docs.igdb.com/
