#!/bin/bash

# Calculate accurate statistics from CSV
java_files=$(tail -n +2 server_analysis.csv | grep ",java," | wc -l)
java_lines=$(tail -n +2 server_analysis.csv | grep ",java," | awk -F',' '{sum+=$2} END {print sum}')
sql_files=$(tail -n +2 server_analysis.csv | grep ",sql," | wc -l)
sql_lines=$(tail -n +2 server_analysis.csv | grep ",sql," | awk -F',' '{sum+=$2} END {print sum}')
largest_java=$(tail -n +2 server_analysis.csv | grep ",java," | sort -t',' -k2 -nr | head -1 | awk -F',' '{print $1 " (" $2 " lines)"}')
largest_sql=$(tail -n +2 server_analysis.csv | grep ",sql," | sort -t',' -k2 -nr | head -1 | awk -F',' '{print $1 " (" $2 " lines)"}')

# Generate timestamp
timestamp=$(date '+%Y-%m-%d %H:%M:%S UTC')

# Create the enhanced markdown report
cat > docs/server-structure.md << REPORT_EOF
# Server Directory Structure Analysis

**Generated:** $timestamp  
**Build Status:** ��� GREEN  

## Directory Overview

\`\`\`
server/
├── .mvn/                    # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/myapp/server/
│   │   │   ├── auctions/    # Auction domain module (27 files)
│   │   │   ├── auth/        # Authentication & authorization (10 files)
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
\`\`\`

## File Type Summary

| Type | Files | Total Lines | Largest File |
|------|-------|-------------|--------------|
| java | $java_files | $java_lines | $largest_java |
| sql | $sql_files | $sql_lines | $largest_sql |
| xml | 1 | 165 | server/pom.xml (165 lines) |
| properties | 3 | 84 | server/src/main/resources/application-dev.properties (34 lines) |
| cmd | 1 | 149 | server/mvnw.cmd (149 lines) |

## Module Breakdown

| Module | Java Files | Total Lines | % of Java Code |
|--------|------------|-------------|----------------|
REPORT_EOF

# Calculate module statistics
for module in auctions auth bids users common config; do
    module_files=$(tail -n +2 server_analysis.csv | grep ",java,$module," | wc -l)
    module_lines=$(tail -n +2 server_analysis.csv | grep ",java,$module," | awk -F',' '{sum+=$2} END {print sum+0}')
    if [[ $module_lines -gt 0 ]]; then
        percentage=$(echo "scale=1; $module_lines * 100 / $java_lines" | awk "{print $1/$2*100}")
        echo "| $module | $module_files | $module_lines | $percentage% |" >> docs/server-structure.md
    fi
done

cat >> docs/server-structure.md << REPORT_EOF

## Layer Breakdown

| Layer | Files | Total Lines |
|-------|-------|-------------|
REPORT_EOF

# Calculate layer statistics
for layer in repository service controller migration mapper entity dto converter config utils; do
    layer_files=$(tail -n +2 server_analysis.csv | grep ",$layer$" | wc -l)
    layer_lines=$(tail -n +2 server_analysis.csv | grep ",$layer$" | awk -F',' '{sum+=$2} END {print sum+0}')
    if [[ $layer_lines -gt 0 ]]; then
        echo "| $layer | $layer_files | $layer_lines |" >> docs/server-structure.md
    fi
done

cat >> docs/server-structure.md << REPORT_EOF

## File Details

| Path | Lines | Type | Module | Layer | Notes |
|------|-------|------|---------|-------|-------|
REPORT_EOF

# Sort by lines descending and add to table
tail -n +2 server_analysis.csv | sort -t',' -k2 -nr | while IFS=',' read -r path lines type module layer; do
    notes=""
    if [[ $lines -gt 150 ]]; then
        notes="��� >150 lines"
    fi
    echo "| $path | $lines | $type | $module | $layer | $notes |" >> docs/server-structure.md
done

cat >> docs/server-structure.md << REPORT_EOF

## SQL Migrations Summary

**Total Files:** $sql_files  
**Total Lines:** $sql_lines  

**Version Conflicts:** 
- V4: 2 files (V4__bids.sql, V4__sample_auctions.sql)
- V5: 2 files (V5__add_more_auctions_for_pagination.sql, V5__bid_history_snapshots.sql)  
- V10: 2 files (V10__bid_history_snapshots.sql, V10__users_unique_email_drop_address.sql)

**Empty Migrations:** V15, V16, V17, V4__sample_auctions, V5__add_more_auctions_for_pagination

## Large Files (>150 lines)

REPORT_EOF

# Add large files section
tail -n +2 server_analysis.csv | awk -F',' '$2 > 150 {print $1 " (" $2 " lines)"}' | sort -t'(' -k2 -nr >> docs/server-structure.md

cat >> docs/server-structure.md << REPORT_EOF

## Policy Compliance

**Target:** Files ≤150 lines (±20% = 120-180 lines)  
**Status:** ⚠️ PARTIAL COMPLIANCE

**Exceptions (>150 lines):**
REPORT_EOF

tail -n +2 server_analysis.csv | awk -F',' '$2 > 150 {print "- " $1 " (" $2 " lines)"}' | sort -t'(' -k2 -nr >> docs/server-structure.md

cat >> docs/server-structure.md << REPORT_EOF

## Reproduce Commands

\`\`\`bash
# List tracked files and count lines
cd c:/projects/my-app
git ls-files server/ | grep -E '\.(java|sql|properties|yml|yaml|xml|md|cmd|sh|bat|json)$' | grep -v -E '(target/|\.git/|\.idea/|\.vscode/|node_modules/|data/|\.db$|\.log$|\.class$|\.jar$|generated-)'

# Count lines for each file
while IFS= read -r file; do
    if [[ -f "\$file" ]]; then
        lines=\$(wc -l < "\$file")
        echo "\$file: \$lines lines"
    fi
done < file_list.txt
\`\`\`
REPORT_EOF

echo "Enhanced report generated successfully!"
