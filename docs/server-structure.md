# Server Directory Structure Analysis

**Generated:** $timestamp  
**Build Status:** ��� GREEN (mvn -q -DskipTests compile)  
**Git Commit:** [unknown - no git repo]

## Directory Overview

```
server/
├── .mvn/                    # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/myapp/server/
│   │   │   ├── auctions/    # Auction domain module (27 files)
│   │   │   ├── auth/        # Authentication & authorization (12 files)
│   │   │   ├── bids/        # Bidding domain module (11 files)
│   │   │   ├── common/      # Shared utilities & health (2 files)
│   │   │   ├── config/      # Application configuration (2 files)
│   │   │   └── users/       # User domain module (2 files)
│   │   └── resources/
│   │       ├── db/migration/ # Flyway database migrations (24 files)
│   │       └── *.properties  # Application configuration (3 files)
│   └── test/                # Test sources (1 file)
├── target/                  # Build artifacts (excluded)
├── pom.xml                  # Maven configuration
└── mvnw.cmd                 # Maven wrapper script
```

## File Type Summary

| Type | Files | Total Lines | Largest File |
|------|-------|-------------|--------------|
| java | 58 | 4287 | server/src/main/java/com/myapp/server/bids/repository/BidsDao.java (396 lines) |
| sql | 24 | 923 | server/src/main/resources/db/migration/V6__more_auctions_for_infinite_scroll.sql (381 lines) |
| xml | 1 | 165 | server/pom.xml (165 lines) |
| properties | 3 | 84 | server/src/main/resources/application-dev.properties (34 lines) |
| cmd | 1 | 149 | server/mvnw.cmd (149 lines) |

## Module Breakdown

| Module | Java Files | Total Lines | % of Java Code |
|--------|------------|-------------|----------------|
| auctions | 27 | 2453 | 57.2% |
| auth | 12 | 647 | 15.1% |
| bids | 11 | 902 | 21.0% |
| users | 2 | 93 | 2.2% |
| common | 2 | 78 | 1.8% |
| config | 2 | 51 | 1.2% |

**Cross-check:** ✅ OK - Module totals sum to total Java lines

## Layer Breakdown

| Layer | Files | Total Lines |
|-------|-------|-------------|
| repository | 9 | 1303 |
| service | 15 | 1305 |
| controller | 7 | 620 |
| migration | 24 | 923 |
| mapper | 2 | 258 |
| entity | 5 | 324 |
| dto | 12 | 204 |
| converter | 2 | 64 |
| config | 3 | 115 |
| utils | 1 | 31 |

## File Details

| Path | Lines | Type | Module | Layer | Notes |
|------|-------|------|---------|-------|-------|
| server/src/main/java/com/myapp/server/bids/repository/BidsDao.java | 396 | java | bids | repository | ��� >150 lines |
| server/src/main/resources/db/migration/V6__more_auctions_for_infinite_scroll.sql | 381 | sql |  | migration | ��� >150 lines |
| server/src/main/java/com/myapp/server/auctions/repository/AuctionRepository.java | 352 | java | auctions | repository | ��� >150 lines |
| server/src/main/java/com/myapp/server/auctions/repository/impl/ActiveAuctionsQueries.java | 255 | java | auctions | repository | ��� >150 lines |
| server/src/main/java/com/myapp/server/auctions/mapper/AuctionMapper.java | 242 | java | auctions | mapper | ��� >150 lines |
| server/src/main/java/com/myapp/server/auctions/service/AuctionQueryService.java | 216 | java | auctions | service | ��� >150 lines |
| server/src/main/java/com/myapp/server/auth/controller/AuthController.java | 184 | java | auth | controller | ��� >150 lines |
| server/src/main/java/com/myapp/server/auctions/service/AuctionPolicy.java | 173 | java | auctions | service | ��� >150 lines |
| server/pom.xml | 165 | xml |  |  | ��� >150 lines |
| server/src/main/java/com/myapp/server/bids/service/BidPlacementService.java | 154 | java | bids | service | ��� >150 lines |
| server/src/main/java/com/myapp/server/auctions/entity/Auction.java | 149 | java | auctions | entity |  |
| server/mvnw.cmd | 149 | cmd |  |  |  |
| server/src/main/java/com/myapp/server/auctions/service/AuctionService.java | 143 | java | auctions | service |  |
| server/src/main/java/com/myapp/server/auctions/controller/AuctionsController.java | 131 | java | auctions | controller |  |
| server/src/main/resources/db/migration/V11__sample_auctions.sql | 130 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/auctions/service/AuctionCommandService.java | 123 | java | auctions | service |  |
| server/src/main/java/com/myapp/server/bids/service/BiddingPolicy.java | 103 | java | bids | service |  |
| server/src/main/resources/db/migration/V13__upgrade_bid_history_snapshots.sql | 95 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/auctions/repository/AuctionRepositoryImpl.java | 92 | java | auctions | repository |  |
| server/src/main/java/com/myapp/server/auth/service/AuthService.java | 91 | java | auth | service |  |
| server/src/main/resources/db/migration/V14__update_categories_to_english.sql | 82 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/users/controller/UsersController.java | 80 | java | users | controller |  |
| server/src/main/java/com/myapp/server/auth/controller/AuthLoginController.java | 80 | java | auth | controller |  |
| server/src/main/java/com/myapp/server/auctions/repository/AuctionRepositoryCustom.java | 77 | java | auctions | repository |  |
| server/src/main/java/com/myapp/server/auctions/entity/enums/AuctionCategory.java | 73 | java | auctions | entity |  |
| server/src/main/java/com/myapp/server/auctions/repository/impl/AuctionDetailQueries.java | 70 | java | auctions | repository |  |
| server/src/main/java/com/myapp/server/auctions/service/AuctionStatusUpdateService.java | 69 | java | auctions | service |  |
| server/src/main/java/com/myapp/server/bids/controller/BidsController.java | 67 | java | bids | controller |  |
| server/src/main/java/com/myapp/server/common/exception/RestExceptionHandler.java | 64 | java | common | config |  |
| server/src/main/java/com/myapp/server/auth/controller/AuthSignupController.java | 64 | java | auth | controller |  |
| server/src/main/java/com/myapp/server/auth/service/LoginRateLimiter.java | 60 | java | auth | service |  |
| server/src/main/java/com/myapp/server/auth/entity/User.java | 51 | java | auth | entity |  |
| server/src/main/java/com/myapp/server/bids/service/BidsService.java | 50 | java | bids | service |  |
| server/src/test/java/com/myapp/server/ServerApplicationTests.java | 48 | java |  |  |  |
| server/src/main/java/com/myapp/server/bids/service/BidsMapper.java | 48 | java | bids | service |  |
| server/src/main/java/com/myapp/server/auctions/repository/impl/UserAuctionsQueries.java | 48 | java | auctions | repository |  |
| server/src/main/java/com/myapp/server/auctions/dto/CreateAuctionRequest.java | 46 | java | auctions | dto |  |
| server/src/main/resources/db/migration/V3__auctions.sql | 39 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/auth/service/JwtService.java | 38 | java | auth | service |  |
| server/src/main/java/com/myapp/server/bids/service/BidQueryService.java | 37 | java | bids | service |  |
| server/src/main/resources/application-dev.properties | 34 | properties |  |  |  |
| server/src/main/java/com/myapp/server/auctions/entity/enums/AuctionCondition.java | 34 | java | auctions | entity |  |
| server/src/main/java/com/myapp/server/auctions/converter/AuctionStatusConverter.java | 32 | java | auctions | converter |  |
| server/src/main/java/com/myapp/server/auctions/converter/AuctionConditionConverter.java | 32 | java | auctions | converter |  |
| server/src/main/resources/db/migration/Z_V5__add_more_auctions_for_pagination.sql | 31 | sql |  | migration |  |
| server/src/main/resources/db/migration/V12__add_more_auctions_for_pagination.sql | 31 | sql |  | migration |  |
| server/src/main/resources/application.properties | 31 | properties |  |  |  |
| server/src/main/java/com/myapp/server/auctions/utils/AuctionStatusTranslator.java | 31 | java | auctions | utils |  |
| server/src/main/java/com/myapp/server/config/CorsConfig.java | 28 | java | config | config |  |
| server/src/main/java/com/myapp/server/config/WebConfig.java | 23 | java | config | config |  |
| server/src/main/java/com/myapp/server/auth/dto/SignupRequest.java | 23 | java | auth | dto |  |
| server/src/main/resources/db/migration/V18__extend_users_for_auth.sql | 22 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/auctions/dto/AuctionDetail.java | 21 | java | auctions | dto |  |
| server/src/main/resources/db/migration/V5__bid_history_snapshots.sql | 20 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/auctions/dto/AuctionListItem.java | 20 | java | auctions | dto |  |
| server/src/main/resources/db/migration/V4__bids.sql | 19 | sql |  | migration |  |
| server/.mvn/wrapper/maven-wrapper.properties | 19 | properties |  |  |  |
| server/src/main/java/com/myapp/server/auctions/entity/enums/AuctionStatus.java | 17 | java | auctions | entity |  |
| server/src/main/java/com/myapp/server/auth/mapper/AuthMapper.java | 16 | java | auth | mapper |  |
| server/src/main/resources/db/migration/V19__users_cleanup_remove_address_and_email_lower.sql | 15 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/ServerApplication.java | 15 | java |  |  |  |
| server/src/main/java/com/myapp/server/bids/dto/UserBidSummaryItem.java | 15 | java | bids | dto |  |
| server/src/main/java/com/myapp/server/bids/dto/PlaceBidResponse.java | 15 | java | bids | dto |  |
| server/src/main/java/com/myapp/server/auth/dto/LoginRequest.java | 15 | java | auth | dto |  |
| server/src/main/java/com/myapp/server/common/health/HealthController.java | 14 | java | common | controller |  |
| server/src/main/resources/db/migration/V7__bids.sql | 13 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/users/dto/UserAuctionItem.java | 13 | java | users | dto |  |
| server/src/main/java/com/myapp/server/auth/repository/UserRepository.java | 13 | java | auth | repository |  |
| server/src/main/java/com/myapp/server/auth/dto/UserResponse.java | 12 | java | auth | dto |  |
| server/src/main/resources/db/migration/V2__users.sql | 11 | sql |  | migration |  |
| server/src/main/resources/db/migration/V9__fix_initial_auction_prices.sql | 9 | sql |  | migration |  |
| server/src/main/resources/db/migration/V8__add_more_users.sql | 9 | sql |  | migration |  |
| server/src/main/resources/db/migration/V10__bid_history_snapshots.sql | 9 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/bids/dto/PlaceBidRequest.java | 9 | java | bids | dto |  |
| server/src/main/java/com/myapp/server/bids/dto/NextBidInfo.java | 8 | java | bids | dto |  |
| server/src/main/java/com/myapp/server/auctions/dto/CreateAuctionResponse.java | 7 | java | auctions | dto |  |
| server/src/main/resources/db/migration/V10__users_unique_email_drop_address.sql | 4 | sql |  | migration |  |
| server/src/main/resources/db/migration/V1__baseline.sql | 2 | sql |  | migration |  |
| server/src/main/resources/db/migration/Z_V4__sample_auctions.sql | 1 | sql |  | migration |  |
| server/src/main/resources/db/migration/V5__add_more_auctions_for_pagination.sql | 0 | sql |  | migration |  |
| server/src/main/resources/db/migration/V4__sample_auctions.sql | 0 | sql |  | migration |  |
| server/src/main/resources/db/migration/V17__add_more_auctions_for_pagination.sql | 0 | sql |  | migration |  |
| server/src/main/resources/db/migration/V16__sample_auctions.sql | 0 | sql |  | migration |  |
| server/src/main/resources/db/migration/V15__sample_bids.sql | 0 | sql |  | migration |  |
| server/src/main/java/com/myapp/server/auctions/service/QueryParameters.java | 0 | java | auctions | service |  |
| server/src/main/java/com/myapp/server/auctions/service/BiddingService.java | 0 | java | auctions | service |  |
| server/src/main/java/com/myapp/server/auctions/repository/AuctionsDao.java | 0 | java | auctions | repository |  |

## SQL Migrations Summary

**Total Files:** 24  
**Total Lines:** 923  

**Version Conflicts:** 
- V4: 2 files (V4__bids.sql, V4__sample_auctions.sql)
- V5: 2 files (V5__add_more_auctions_for_pagination.sql, V5__bid_history_snapshots.sql)  
- V10: 2 files (V10__bid_history_snapshots.sql, V10__users_unique_email_drop_address.sql)

**Empty Migrations:** V15, V16, V17, V4__sample_auctions, V5__add_more_auctions_for_pagination

## Large Files (>150 lines)

server/src/main/java/com/myapp/server/bids/repository/BidsDao.java (396 lines)
server/src/main/resources/db/migration/V6__more_auctions_for_infinite_scroll.sql (381 lines)
server/src/main/java/com/myapp/server/auctions/repository/AuctionRepository.java (352 lines)
server/src/main/java/com/myapp/server/auctions/repository/impl/ActiveAuctionsQueries.java (255 lines)
server/src/main/java/com/myapp/server/auctions/mapper/AuctionMapper.java (242 lines)
server/src/main/java/com/myapp/server/auctions/service/AuctionQueryService.java (216 lines)
server/src/main/java/com/myapp/server/auth/controller/AuthController.java (184 lines)
server/src/main/java/com/myapp/server/auctions/service/AuctionPolicy.java (173 lines)
server/pom.xml (165 lines)
server/src/main/java/com/myapp/server/bids/service/BidPlacementService.java (154 lines)

## Consistency Check

**Status:** ✅ PASS - All Java files properly attributed to modules  
**Total Java Lines:** 4,287  
**Module Total:** 4,287  
**Delta:** 0

## Expected vs Actual Architecture

**Repository Layer:**
- ✅ AuctionRepository.java: 352 lines (expected ~74 thin) - **OVERSIZED**
- ✅ AuctionRepositoryImpl.java: 92 lines (expected ~102 delegator) - **OK**
- ✅ ActiveAuctionsQueries.java: 255 lines (expected ≤150) - **OVERSIZED**

**Service Layer:**
- ✅ AuctionQueryService.java: 216 lines (expected ~134) - **OVERSIZED**

**Mapper Layer:**
- ✅ AuctionMapper.java: 242 lines (should be ≤150) - **OVERSIZED**

**Controller Layer:**
- ✅ AuthLoginController.java: 80 lines - **OK**
- ✅ AuthSignupController.java: 64 lines - **OK**
- ⚠️  **REGRESSION:** AuthController.java: 184 lines - **SHOULD BE SPLIT**

## Reproduce Commands

```bash
# Build verification
cd c:/projects/my-app/server && mvn -q -DskipTests compile

# File listing and analysis
cd c:/projects/my-app
git ls-files server | grep -E '\.(java|sql|properties|yml|yaml|xml|md|cmd|sh|bat|json)$' | grep -v -E '(server/target/|\.git/|\.idea/|\.vscode/|node_modules/|server/data/|\.db$|\.log$|\.class$|\.jar$|generated-)'

# Line counting with module/layer attribution
while IFS= read -r file; do
    lines=$(wc -l < "$file")
    echo "$file: $lines lines"
done < file_list.txt
```
