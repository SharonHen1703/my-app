# 📂 מבנה מפורט של `C:\projects\my-app\server`

_מסמך זה מתאר את המבנה המלא של השרת כולל מספר שורות קוד בכל קובץ_

---

## 🏗️ מבנה כללי

```
C:\projects\my-app\server\
├── 📁 .mvn/                          # Maven Wrapper Configuration
├── 📁 data/                          # H2 Database Files
├── 📁 src/                          # Source Code
├── 📁 target/                       # Compiled Classes (Build Output)
├── .gitignore                       # Git ignore rules (33 שורות)
├── HELP.md                          # Maven help documentation (29 שורות)
├── mvnw                            # Maven wrapper Unix (259 שורות)
├── mvnw.cmd                        # Maven wrapper Windows (149 שורות)
├── pom.xml                         # Maven configuration (155 שורות)
└── server.log                     # Application logs (109 שורות)
```

**📊 סיכום ברמה העליונה:** 734 שורות בקבצי תצורה ותיעוד

---

## 🛠️ Maven Configuration - `.mvn/`

```
.mvn/
└── wrapper/
    └── maven-wrapper.properties    # Maven wrapper settings (19 שורות)
```

---

## 🗄️ Database Files - `data/`

```
data/
├── auction_db.lock.db             # H2 Database lock file (binary)
└── auction_db.mv.db               # H2 Database main file (binary)
```

---

## 💻 Source Code - `src/`

### 📦 Java Source - `src/main/java/com/myapp/server/`

## 📊 סיכום כללי

**📋 Java Source Files (Production):**

- ⭐ **40 Java files** - 2,873 lines of production code
- 📁 17 files in auctions module (1,569 lines)
- 🔐 9 files in auth module (519 lines)
- 💰 7 files in bids module (654 lines)
- 👥 2 files in users module (93 lines)
- ⚙️ 2 files in config module (34 lines)
- 🏥 2 files in common module (3 lines)
- 🚀 1 main application file (1 line)

**📋 Additional Files:**

- 1 test file in src/test/

**🗃️ SQL Migration Files:**

- ⭐ **24 SQL files** - 923 lines of database schema and data

#### 🏪 Auctions Module - `auctions/`

**📊 סה״כ: 1,373 שורות**

```
auctions/
├── controller/
│   └── AuctionsController.java      # REST API endpoints (131 שורות)
│
├── converter/
│   ├── AuctionConditionConverter.java  # JPA converter (32 שורות)
│   └── AuctionStatusConverter.java     # JPA converter (32 שורות)
│
├── dto/                            # Data Transfer Objects
│   ├── AuctionDetail.java           # Auction detail response (21 שורות)
│   ├── AuctionListItem.java         # Auction list item (20 שורות)
│   ├── CreateAuctionRequest.java    # Create auction request (46 שורות)
│   └── CreateAuctionResponse.java   # Create auction response (7 שורות)
│
├── entity/
│   ├── Auction.java                 # JPA Entity (149 שורות)
│   └── enums/
│       ├── AuctionCategory.java     # Category enum (73 שורות)
│       ├── AuctionCondition.java    # Condition enum (34 שורות)
│       └── AuctionStatus.java       # Status enum (17 שורות)
│
├── repository/
│   ├── AuctionRepository.java       # JPA Repository (352 שורות)
│   └── AuctionsDao.java            # Custom DAO (לא מיושם - 0 שורות)
│
├── service/
│   ├── AuctionService.java          # Business Logic (415 שורות) 🔴
│   ├── AuctionStatusUpdateService.java  # Status updates (69 שורות)
│   └── BiddingService.java          # Bidding Logic (לא מיושם - 0 שורות)
│
└── utils/
    └── AuctionStatusTranslator.java # Status translation (31 שורות)
```

#### 🔐 Authentication Module - `auth/`

**📊 סה״כ: 464 שורות**

```
auth/
├── dto/
│   ├── LoginRequest.java            # Login request DTO (15 שורות)
│   ├── SignupRequest.java           # Signup request DTO (23 שורות)
│   └── UserResponse.java            # User response DTO (12 שורות)
│
├── AuthController.java              # Authentication endpoints (181 שורות) 🔴
├── AuthService.java                 # Auth business logic (64 שורות)
├── JwtService.java                  # JWT token handling (38 שורות)
├── LoginRateLimiter.java            # Rate limiting (60 שורות)
├── User.java                        # User entity (51 שורות)
└── UserRepository.java              # User repository (12 שורות)
```

#### 💰 Bids Module - `bids/`

**📊 סה״כ: 654 שורות**

```
bids/
├── controller/
│   └── BidsController.java          # Bidding endpoints (67 שורות)
│
├── dto/
│   ├── NextBidInfo.java             # Next bid information (8 שורות)
│   ├── PlaceBidRequest.java         # Place bid request (9 שורות)
│   ├── PlaceBidResponse.java        # Place bid response (15 שורות)
│   └── UserBidSummaryItem.java      # User bid summary (15 שורות)
│
├── repository/
│   └── BidsDao.java                 # Bids data access (396 שורות) 🔴
│
└── service/
    └── BidsService.java             # Bidding business logic (193 שורות)
```

#### 👥 Users Module - `users/`

**📊 סה״כ: 93 שורות**

```
users/
├── controller/
│   └── UsersController.java         # User management endpoints (80 שורות)
└── dto/
    └── UserAuctionItem.java         # User's auction DTO (13 שורות)
```

#### 🔧 Common & Config - `common/` & `config/`

**📊 סה״כ: 129 שורות**

```
common/
├── HealthController.java            # Health check endpoint (14 שורות)
└── RestExceptionHandler.java       # Global exception handling (64 שורות)

config/
├── CorsConfig.java                  # CORS configuration (28 שורות)
└── WebConfig.java                   # Web configuration (23 שורות)
```

#### 🏠 Main Application

```
ServerApplication.java               # Main Spring Boot Application (15 שורות)
```

### 📋 Resources - `src/main/resources/`

#### ⚙️ Configuration Files

```
application.properties              # Main config (28 שורות)
application-dev.properties          # Development config (34 שורות)
```

#### 🗃️ Database Migrations - `db/migration/`

**📊 סה״כ: 923 שורות SQL ב-24 קבצים**

```
db/migration/
├── V1__baseline.sql                 # Initial schema (2 שורות)
├── V2__users.sql                    # Users table (11 שורות)
├── V3__auctions.sql                 # Auctions table (39 שורות)
├── V4__bids.sql                     # Bids table (19 שורות)
├── V4__sample_auctions.sql          # Sample data (0 שורות - ריק)
├── V5__add_more_auctions_for_pagination.sql  # (0 שורות - ריק)
├── V5__bid_history_snapshots.sql   # Bid history (20 שורות)
├── V6__more_auctions_for_infinite_scroll.sql  # More auctions (381 שורות) 🔴
├── V7__bids.sql                     # Bids updates (13 שורות)
├── V8__add_more_users.sql           # More users (9 שורות)
├── V9__fix_initial_auction_prices.sql  # Price fixes (9 שורות)
├── V10__bid_history_snapshots.sql  # Bid history updates (9 שורות)
├── V10__users_unique_email_drop_address.sql  # User schema (4 שורות)
├── V11__sample_auctions.sql         # Sample auctions (130 שורות)
├── V12__add_more_auctions_for_pagination.sql  # Pagination (31 שורות)
├── V13__upgrade_bid_history_snapshots.sql  # History upgrade (95 שורות)
├── V14__update_categories_to_english.sql  # Categories (82 שורות)
├── V15__sample_bids.sql             # Sample bids (0 שורות - ריק)
├── V16__sample_auctions.sql         # More samples (0 שורות - ריק)
├── V17__add_more_auctions_for_pagination.sql  # (0 שורות - ריק)
├── V18__extend_users_for_auth.sql   # User auth (22 שורות)
├── V19__users_cleanup_remove_address_and_email_lower.sql  # Cleanup (15 שורות)
├── Z_V4__sample_auctions.sql        # Legacy sample (1 שורה)
└── Z_V5__add_more_auctions_for_pagination.sql  # Legacy (31 שורות)
```

### 🧪 Tests - `src/test/java/com/myapp/server/`

```
ServerApplicationTests.java         # Basic Spring Boot test (48 שורות)
```

---

## 🎯 Build Output - `target/`

```
target/
├── classes/                        # Compiled .class files + resources
│   ├── com/myapp/server/           # Java classes (mirroring src structure)
│   ├── db/migration/               # SQL files (copied from src)
│   ├── application.properties      # Config files (28 שורות)
│   └── application-dev.properties  # Dev config (34 שורות)
├── generated-sources/              # Generated source code
├── generated-test-sources/         # Generated test sources
├── maven-status/                   # Maven build status
└── test-classes/                   # Compiled test classes
```

---

## 📊 סיכום מספרי

### 📈 Code Metrics

| סוג קובץ          | מספר קבצים | סה״כ שורות | הקובץ הגדול ביותר                                       |
| ----------------- | ---------- | ---------- | ------------------------------------------------------- |
| **Java Files**    | 41         | 2,873      | AuctionService.java (415 שורות)                         |
| **SQL Files**     | 24         | 923        | V6\_\_more_auctions_for_infinite_scroll.sql (381 שורות) |
| **Config Files**  | 6          | 288        | pom.xml (155 שורות)                                     |
| **Documentation** | 2          | 138        | mvnw (259 שורות)                                        |
| **Scripts**       | 2          | 408        | mvnw (259 שורות)                                        |

### 🎯 Module Breakdown

| Module            | קבצי Java | שורות | אחוז  |
| ----------------- | --------- | ----- | ----- |
| **Auctions**      | 13        | 1,373 | 47.8% |
| **Bids**          | 6         | 654   | 22.8% |
| **Auth**          | 9         | 464   | 16.1% |
| **Users**         | 2         | 93    | 3.2%  |
| **Common/Config** | 4         | 129   | 4.5%  |
| **Main App**      | 1         | 15    | 0.5%  |
| **Tests**         | 1         | 48    | 1.7%  |

### 🔴 Large Files (>200 שורות)

1. **AuctionService.java** - 415 שורות (Business Logic)
2. **BidsDao.java** - 396 שורות (Data Access)
3. **V6\_\_more_auctions_for_infinite_scroll.sql** - 381 שורות (Sample Data)
4. **AuctionRepository.java** - 352 שורות (JPA Repository)
5. **mvnw** - 259 שורות (Maven Wrapper)
6. **BidsService.java** - 193 שורות (Business Logic)
7. **AuthController.java** - 181 שורות (REST Controller)

---

## 🏗️ ארכיטקטורה

- **🚀 Framework:** Spring Boot 3.x
- **🗄️ Database:** H2 (פיתוח) + Flyway migrations
- **🔐 Security:** JWT authentication + rate limiting
- **📡 API:** RESTful endpoints
- **🏗️ Pattern:** Domain-driven design + Repository pattern
- **🛠️ Build:** Maven with standard layout

---

**📅 נוצר:** 4 בספטמבר 2025  
**📊 סה״כ שורות קוד:** 4,630 שורות  
**📁 סה״כ קבצים:** 153 קבצים  
**🏗️ סה״כ תיקיות:** 81 תיקיות
