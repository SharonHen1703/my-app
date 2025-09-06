#!/bin/bash

echo "path,lines,type,module,layer" > docs/reports/analysis.csv

while IFS= read -r file; do
    if [[ -f "$file" ]]; then
        lines=$(wc -l < "$file" 2>/dev/null || echo "0")
        type="${file##*.}"
        
        # Extract module from path
        module=""
        if [[ $file =~ server/src/main/java/com/myapp/server/([^/]+)/ ]]; then
            module="${BASH_REMATCH[1]}"
        fi
        
        # Extract layer from path
        layer=""
        if [[ $file =~ /(controller|service|repository|entity|dto|mapper|policy|config|utils)/ ]]; then
            layer="${BASH_REMATCH[1]}"
        elif [[ $file =~ /db/migration/ ]]; then
            layer="migration"
        elif [[ $file =~ /converter/ ]]; then
            layer="converter"
        elif [[ $file =~ /enums/ ]]; then
            layer="entity"
        elif [[ $file =~ /impl/ ]]; then
            layer="repository"
        elif [[ $file =~ /health/ ]]; then
            layer="controller"
        elif [[ $file =~ /exception/ ]]; then
            layer="config"
        fi
        
        echo "$file,$lines,$type,$module,$layer" >> docs/reports/analysis.csv
    fi
done < server_files.txt

echo "Analysis complete. First 10 lines:"
head -10 docs/reports/analysis.csv
