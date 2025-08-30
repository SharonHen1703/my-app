import { describe, it, expect } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import SmartTable, { type ColumnDef } from "./index";
import {
  createNumericComparator,
  createTimeRemainingComparator,
  createGroupedSort,
  compareHebrewStrings,
  parseNumericValue,
} from "./sortingUtils";

// Mock data for testing
type TestItem = {
  id: number;
  title: string;
  price: number;
  priceFormatted: string;
  status: "active" | "ended";
  endDate: string;
};

const mockData: TestItem[] = [
  {
    id: 1,
    title: "אלף",
    price: 800,
    priceFormatted: "800",
    status: "active",
    endDate: "2025-08-28T10:00:00Z",
  },
  {
    id: 2,
    title: "בית",
    price: 1580,
    priceFormatted: "1,580",
    status: "active",
    endDate: "2025-08-28T09:00:00Z",
  },
  {
    id: 3,
    title: "גמל",
    price: 300,
    priceFormatted: "300",
    status: "ended",
    endDate: "2025-08-27T15:00:00Z",
  },
  {
    id: 4,
    title: "דלת",
    price: 50,
    priceFormatted: "50",
    status: "ended",
    endDate: "2025-08-27T14:00:00Z",
  },
];

const columns: ColumnDef<TestItem>[] = [
  {
    key: "title",
    header: "כותרת",
    accessor: (item) => item.title,
    comparator: (a, b) => compareHebrewStrings(a.title, b.title),
    sortable: true,
  },
  {
    key: "price",
    header: "מחיר",
    accessor: (item) => `₪${item.priceFormatted}`,
    comparator: createNumericComparator((item) => item.price),
    sortable: true,
  },
  {
    key: "timeRemaining",
    header: "זמן שנותר",
    accessor: (item) => (item.status === "active" ? "פעיל" : "הסתיים"),
    comparator: createTimeRemainingComparator(
      (item) => item.endDate,
      (item) => item.status
    ),
    sortable: true,
  },
  {
    key: "status",
    header: "סטטוס",
    accessor: (item) => item.status,
    sortable: false,
  },
];

describe("SmartTable Sorting", () => {
  describe("Utility Functions", () => {
    it("parseNumericValue handles formatted numbers correctly", () => {
      expect(parseNumericValue(1580)).toBe(1580);
      expect(parseNumericValue("1,580")).toBe(1580);
      expect(parseNumericValue("800")).toBe(800);
      expect(parseNumericValue("1,234.56")).toBe(1234.56);
      expect(parseNumericValue(null)).toBe(-Infinity);
      expect(parseNumericValue(undefined)).toBe(-Infinity);
      expect(parseNumericValue("invalid")).toBe(-Infinity);
    });

    it("compareHebrewStrings sorts Hebrew text correctly", () => {
      expect(compareHebrewStrings("אלף", "בית")).toBeLessThan(0); // A before B
      expect(compareHebrewStrings("גמל", "אלף")).toBeGreaterThan(0); // G after A
      expect(compareHebrewStrings("אלף", "אלף")).toBe(0); // Same strings
    });

    it("createNumericComparator sorts numbers correctly", () => {
      const comparator = createNumericComparator(
        (item: TestItem) => item.price
      );
      const sorted = [mockData[1], mockData[0], mockData[2], mockData[3]].sort(
        comparator
      );

      // Should be: 50, 300, 800, 1580
      expect(sorted[0].price).toBe(50);
      expect(sorted[1].price).toBe(300);
      expect(sorted[2].price).toBe(800);
      expect(sorted[3].price).toBe(1580);
    });
  });

  describe("Table Rendering", () => {
    it("renders table headers correctly", () => {
      render(<SmartTable rows={mockData} columns={columns} />);

      expect(screen.getByText("כותרת")).toBeDefined();
      expect(screen.getByText("מחיר")).toBeDefined();
      expect(screen.getByText("סטטוס")).toBeDefined();
    });

    it("renders table data correctly", () => {
      render(<SmartTable rows={mockData} columns={columns} />);

      expect(screen.getByText("אלף")).toBeDefined();
      expect(screen.getByText("₪800")).toBeDefined();
      expect(screen.getByText("active")).toBeDefined();
    });

    it("shows correct sort arrow directions", () => {
      render(<SmartTable rows={mockData} columns={columns} />);

      // Find sort buttons for title column
      const sortButtons = screen.getAllByRole("button");
      expect(sortButtons.length).toBeGreaterThan(0);

      // Check aria labels exist for sort buttons
      const ascButton = sortButtons.find((btn) =>
        btn.getAttribute("aria-label")?.includes("עולה")
      );
      const descButton = sortButtons.find((btn) =>
        btn.getAttribute("aria-label")?.includes("יורד")
      );

      expect(ascButton).toBeDefined();
      expect(descButton).toBeDefined();
    });
  });

  describe("Sort Direction Mapping", () => {
    it("clicking ascending button activates ascending sort", () => {
      render(<SmartTable rows={mockData} columns={columns} />);

      const sortButtons = screen.getAllByRole("button");
      const ascButton = sortButtons.find(
        (btn) =>
          btn.getAttribute("aria-label")?.includes("כותרת") &&
          btn.getAttribute("aria-label")?.includes("עולה")
      );

      if (ascButton) {
        fireEvent.click(ascButton);
        expect(ascButton.className).toContain("active");
      }
    });

    it("clicking descending button activates descending sort", () => {
      render(<SmartTable rows={mockData} columns={columns} />);

      const sortButtons = screen.getAllByRole("button");
      const descButton = sortButtons.find(
        (btn) =>
          btn.getAttribute("aria-label")?.includes("כותרת") &&
          btn.getAttribute("aria-label")?.includes("יורד")
      );

      if (descButton) {
        fireEvent.click(descButton);
        expect(descButton.className).toContain("active");
      }
    });
  });

  describe("Numeric Sorting", () => {
    it("sorts numbers in ascending order (800 before 1580)", () => {
      const testData = [
        {
          id: 1,
          title: "Test1",
          price: 1580,
          priceFormatted: "1,580",
          status: "active" as const,
          endDate: "2025-08-28T10:00:00Z",
        },
        {
          id: 2,
          title: "Test2",
          price: 800,
          priceFormatted: "800",
          status: "active" as const,
          endDate: "2025-08-28T09:00:00Z",
        },
      ];

      const comparator = createNumericComparator(
        (item: TestItem) => item.price
      );
      const sorted = testData.sort(comparator);

      expect(sorted[0].price).toBe(800);
      expect(sorted[1].price).toBe(1580);
    });

    it("sorts numbers in descending order (1580 before 800)", () => {
      const testData = [
        {
          id: 1,
          title: "Test1",
          price: 800,
          priceFormatted: "800",
          status: "active" as const,
          endDate: "2025-08-28T10:00:00Z",
        },
        {
          id: 2,
          title: "Test2",
          price: 1580,
          priceFormatted: "1,580",
          status: "active" as const,
          endDate: "2025-08-28T09:00:00Z",
        },
      ];

      const comparator = createNumericComparator(
        (item: TestItem) => item.price
      );
      const sorted = testData.sort((a, b) => -comparator(a, b)); // Reverse for desc

      expect(sorted[0].price).toBe(1580);
      expect(sorted[1].price).toBe(800);
    });
  });

  describe("Grouping Invariant", () => {
    it("maintains active-before-ended grouping", () => {
      const grouped = createGroupedSort(
        (item: TestItem) => item.status,
        createNumericComparator((item: TestItem) => item.price)
      );

      const sorted = grouped(mockData, "asc");

      // Find first ended item
      const firstEndedIndex = sorted.findIndex(
        (item) => item.status === "ended"
      );

      // All items before first ended should be active
      for (let i = 0; i < firstEndedIndex; i++) {
        expect(sorted[i].status).toBe("active");
      }

      // All items from first ended onwards should be ended
      for (let i = firstEndedIndex; i < sorted.length; i++) {
        expect(sorted[i].status).toBe("ended");
      }
    });
  });

  describe("Edge Cases", () => {
    it("handles empty data gracefully", () => {
      render(<SmartTable rows={[]} columns={columns} />);
      expect(
        screen.getByText("לא נמצאו תוצאות העומדות בתנאי הסינון")
      ).toBeDefined();
    });

    it("handles null/undefined numeric values", () => {
      type TestItemWithNulls = {
        id: number;
        title: string;
        price: number | null;
        priceFormatted: string;
        status: "active" | "ended";
        endDate: string;
      };

      const testData: TestItemWithNulls[] = [
        {
          id: 1,
          title: "Valid",
          price: 100,
          priceFormatted: "100",
          status: "active",
          endDate: "2025-08-28T10:00:00Z",
        },
        {
          id: 2,
          title: "Null",
          price: null,
          priceFormatted: "N/A",
          status: "active",
          endDate: "2025-08-28T09:00:00Z",
        },
      ];

      const comparator = createNumericComparator(
        (item: TestItemWithNulls) => item.price
      );
      const sorted = testData.sort(comparator);

      // Null/undefined should sort to end (as -Infinity)
      expect(sorted[0].price).toBe(100);
      expect(sorted[1].price).toBeNull();
    });
  });
});
