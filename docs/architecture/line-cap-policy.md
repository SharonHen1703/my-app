# Line Cap Policy

## Overview

This document defines the line count limits for Java source files in the Spring Boot application to maintain code readability, maintainability, and adherence to clean architecture principles.

## Default Policy

**Default Cap**: 150 lines per file

This applies to all Java source files including:

- Controllers (`**/controller/**/*.java`)
- Services (`**/service/**/*.java`)
- Repositories (`**/repository/**/*.java`)
- Mappers (`**/mapper/**/*.java`)
- DTOs, entities, and other domain classes

## Rationale

The 150-line limit ensures:

- **Readability**: Files can be easily reviewed and understood
- **Single Responsibility**: Classes stay focused on one primary concern
- **Maintainability**: Smaller files are easier to modify and test
- **Code Review Efficiency**: Reviewers can digest changes more effectively

## Accepted Exceptions

The following files are explicitly permitted to exceed the 150-line default cap due to their specialized nature:

| **File Path**                                                                             | **Current Cap** | **Reason**                                                |
| ----------------------------------------------------------------------------------------- | --------------- | --------------------------------------------------------- |
| `server/src/main/java/com/myapp/server/bids/repository/BidsJdbcWriteOps.java`             | 179 lines       | Complex JDBC operations with multiple prepared statements |
| `server/src/main/java/com/myapp/server/auctions/repository/impl/UserAuctionsQueries.java` | 169 lines       | Comprehensive user auction query implementations          |

## Important Notes

- **No Behavior Changes**: These exceptions do not imply any behavioral modifications to the existing functionality
- **Future Refactoring**: These files may be candidates for future refactoring to reduce their size while maintaining the same functionality
- **Strict Enforcement**: No new exceptions should be added without architectural review and documentation updates
- **CI Integration**: Automated checks enforce these limits using the allowlist defined in `tools/line-cap-allowlist.json`

## Enforcement

Line cap violations are checked during:

- Continuous Integration (CI) builds
- Pre-commit hooks (when implemented)
- Architecture reviews

Files exceeding their defined caps will fail CI checks and must be refactored or explicitly added to the exceptions list with proper justification.

## Review Process

To add a new exception:

1. **Justification Required**: Document why the file cannot be reduced to ≤150 lines
2. **Architectural Review**: Review with team leads or architects
3. **Update Documentation**: Add to both this policy and the allowlist
4. **Temporary Nature**: Consider exceptions as technical debt to be addressed in future iterations
