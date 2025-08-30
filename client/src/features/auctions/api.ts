import type {
  AuctionsListResponse,
  AuctionListItem,
  PlaceBidRequest,
  PlaceBidResponse,
  UserAuctionItem,
  CreateAuctionRequest,
  CreateAuctionResponse,
} from "./utils/types";
import type { UserBidSummaryItem } from "../bids/utils/types";

const BASE = "/api";

export async function fetchAuctions(
  page = 0,
  size = 12,
  category?: string,
  minPrice?: number | null,
  maxPrice?: number | null,
  conditions?: string[],
  searchText?: string
): Promise<AuctionsListResponse> {
  const params = new URLSearchParams();
  params.set("page", String(page));
  params.set("size", String(size));
  if (category && category.trim()) params.set("category", category.trim());
  if (minPrice != null) params.set("minPrice", String(minPrice));
  if (maxPrice != null) params.set("maxPrice", String(maxPrice));
  if (conditions && conditions.length) {
    console.log("Adding conditions to request:", conditions);
    for (const c of conditions) params.append("condition", c);
  }
  if (searchText && searchText.trim()) params.set("search", searchText.trim());

  const url = `${BASE}/auctions?${params.toString()}`;
  console.log("Fetching auctions with URL:", url);

  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to load auctions (${res.status})`);
  const data = await res.json();

  console.log("Received auction data:", {
    totalItems: data.totalElements,
    itemsInPage: data.items.length,
    conditions: conditions,
    firstFewItems: data.items.slice(0, 3).map((item: AuctionListItem) => ({
      id: item.id,
      title: item.title,
      condition: item.condition,
    })),
  });

  return data;
}

export async function fetchCategories(): Promise<string[]> {
  const res = await fetch(`${BASE}/auctions/categories`);
  if (!res.ok) throw new Error(`Failed to load categories (${res.status})`);
  return res.json();
}

export async function fetchCategoriesMap(): Promise<Record<string, string>> {
  const res = await fetch(`${BASE}/auctions/categories/map`);
  if (!res.ok) throw new Error(`Failed to load categories map (${res.status})`);
  return res.json();
}

export async function getAuctionDetail(id: number): Promise<AuctionListItem> {
  const res = await fetch(`${BASE}/auctions/${id}`);
  if (!res.ok) throw new Error(`Failed to load auction detail (${res.status})`);
  return res.json();
}

export async function placeBid(
  auctionId: number,
  body: PlaceBidRequest
): Promise<PlaceBidResponse> {
  const res = await fetch(`${BASE}/auctions/${auctionId}/bids`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // Include authentication cookies
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    if (res.status === 403) {
      // Hebrew error message from server
      const errorText = await res
        .text()
        .catch(() => "מוכר לא יכול להציע על המוצר שלו");
      throw new Error(errorText);
    }
    const text = await res.text().catch(() => "");
    throw new Error(text || `Bid failed (${res.status})`);
  }
  return res.json();
}

// (removed) getNextBidInfo: UI now uses auction.minBidToPlace directly

export interface BidHistoryItem {
  snapshotId: number;
  bidId: number;
  bidderId: number;
  displayedBid: number;
  snapshotTime: string; // ISO
  kind: string; // USER_BID, AUTO_RAISE, TIE_AUTO
}

export async function getBidHistory(
  auctionId: number
): Promise<BidHistoryItem[]> {
  const res = await fetch(`${BASE}/auctions/${auctionId}/bids/history`);
  if (!res.ok) throw new Error(`Failed to load bid history (${res.status})`);
  return res.json();
}

export async function getUserBidsSummary(): Promise<UserBidSummaryItem[]> {
  console.log("🔍 getUserBidsSummary - making request with credentials...");
  const res = await fetch(`${BASE}/users/me/bids/summary`, {
    credentials: "include", // Use authenticated user
  });
  console.log("📡 getUserBidsSummary - response status:", res.status, res.ok);
  if (!res.ok) {
    console.error("❌ getUserBidsSummary - failed with status:", res.status);
    throw new Error(`Failed to load user bids (${res.status})`);
  }
  const data = await res.json();
  console.log("✅ getUserBidsSummary - response data:", data);
  return data;
}

export async function getUserAuctions(): Promise<UserAuctionItem[]> {
  const res = await fetch(`${BASE}/users/me/auctions`, {
    credentials: "include", // Use authenticated user
  });
  if (!res.ok) throw new Error(`Failed to load user auctions (${res.status})`);
  return res.json();
}

export async function createAuction(
  auctionData: CreateAuctionRequest
): Promise<CreateAuctionResponse> {
  const res = await fetch(`${BASE}/auctions`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // Include authentication
    body: JSON.stringify(auctionData),
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `Failed to create auction (${res.status})`);
  }

  return res.json();
}
