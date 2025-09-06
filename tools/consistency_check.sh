#!/bin/bash

# Calculate total Java lines
java_total=$(tail -n +2 docs/reports/docs/reports/analysis.csv | grep ",java," | awk -F',' '{sum+=$2} END {print sum+0}')

# Calculate module breakdown
echo "=== MODULE ANALYSIS ==="
echo "Module,Files,Lines"
for module in auctions auth bids users common config; do
    files=$(tail -n +2 docs/reports/analysis.csv | grep ",java,$module," | wc -l)
    lines=$(tail -n +2 docs/reports/analysis.csv | grep ",java,$module," | awk -F',' '{sum+=$2} END {print sum+0}')
    if [[ $lines -gt 0 ]]; then
        echo "$module,$files,$lines"
    fi
done

# Calculate module total
module_total=$(tail -n +2 docs/reports/analysis.csv | grep ",java," | grep -v ",,," | awk -F',' '{sum+=$2} END {print sum+0}')

echo ""
echo "=== CONSISTENCY CHECK ==="
echo "Total Java lines: $java_total"
echo "Module Java total: $module_total"
echo "Delta: $((java_total - module_total))"

if [[ $((java_total - module_total)) -ne 0 ]]; then
    echo ""
    echo "=== UNATTRIBUTED JAVA FILES ==="
    tail -n +2 docs/reports/analysis.csv | grep ",java," | grep ",,," | awk -F',' '{print $1 " (" $2 " lines)"}'
fi

# Check for specific files
echo ""
echo "=== EXPECTED FILES AUDIT ==="
echo "Looking for key architecture files..."

expected_files=(
    "server/src/main/java/com/myapp/server/auctions/repository/AuctionRepository.java"
    "server/src/main/java/com/myapp/server/auctions/repository/AuctionRepositoryImpl.java"
    "server/src/main/java/com/myapp/server/auctions/repository/impl/ActiveAuctionsQueries.java"
    "server/src/main/java/com/myapp/server/auctions/service/AuctionQueryService.java"
    "server/src/main/java/com/myapp/server/auctions/mapper/AuctionMapper.java"
    "server/src/main/java/com/myapp/server/auth/controller/AuthLoginController.java"
    "server/src/main/java/com/myapp/server/auth/controller/AuthSignupController.java"
    "server/src/main/java/com/myapp/server/auth/controller/AuthController.java"
)

for file in "${expected_files[@]}"; do
    lines=$(grep "^$file," docs/reports/analysis.csv | cut -d',' -f2)
    if [[ -n "$lines" ]]; then
        if [[ "$file" == *"AuthController.java" ]]; then
            echo "⚠️  REGRESSION: $file exists ($lines lines) - should be split"
        else
            echo "✅ $file: $lines lines"
        fi
    else
        echo "❌ MISSING: $file"
    fi
done

