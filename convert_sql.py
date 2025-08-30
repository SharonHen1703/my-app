#!/usr/bin/env python3
import re
import sys

def convert_dateadd_to_postgresql(content):
    """Convert H2 DATEADD functions to PostgreSQL interval syntax"""
    
    # Handle positive intervals
    content = re.sub(r"DATEADD\('day', (\d+), (CURRENT_TIMESTAMP|NOW\(\))\)", 
                     r"\2 + INTERVAL '\1 days'", content)
    content = re.sub(r"DATEADD\('hour', (\d+), (CURRENT_TIMESTAMP|NOW\(\))\)", 
                     r"\2 + INTERVAL '\1 hours'", content)
    content = re.sub(r"DATEADD\('minute', (\d+), (CURRENT_TIMESTAMP|NOW\(\))\)", 
                     r"\2 + INTERVAL '\1 minutes'", content)
    
    # Handle negative intervals (convert to minus)
    content = re.sub(r"DATEADD\('day', -(\d+), (CURRENT_TIMESTAMP|NOW\(\))\)", 
                     r"\2 - INTERVAL '\1 days'", content)
    content = re.sub(r"DATEADD\('hour', -(\d+), (CURRENT_TIMESTAMP|NOW\(\))\)", 
                     r"\2 - INTERVAL '\1 hours'", content)
    content = re.sub(r"DATEADD\('minute', -(\d+), (CURRENT_TIMESTAMP|NOW\(\))\)", 
                     r"\2 - INTERVAL '\1 minutes'", content)
    
    return content

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python3 convert_sql.py <sql_file>")
        sys.exit(1)
    
    filename = sys.argv[1]
    
    # Read the file
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Convert DATEADD to PostgreSQL syntax
    converted_content = convert_dateadd_to_postgresql(content)
    
    # Write back to file
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(converted_content)
    
    print(f"Converted {filename} from H2 to PostgreSQL syntax")
