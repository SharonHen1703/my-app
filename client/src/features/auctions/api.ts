import type {
  AuctionsListResponse,
  PlaceBidRequest,
  PlaceBidResponse,
} from "./utils/types";

const BASE = "/api";

export async function fetchAuctions(
  page = 0,
  size = 12
): Promise<AuctionsListResponse> {
  const res = await fetch(`${BASE}/auctions?page=${page}&size=${size}`);
  if (!res.ok) throw new Error(`Failed to load auctions (${res.status})`);
  return res.json();
}

export async function placeBid(
  auctionId: number,
  body: PlaceBidRequest
): Promise<PlaceBidResponse> {
  const res = await fetch(`${BASE}/auctions/${auctionId}/bids`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    if (res.status === 403) {
      throw new Error("מוכר לא יכול להציע על המוצר שלו. נסה user ID אחר.");
    }
    const text = await res.text().catch(() => "");
    throw new Error(text || `Bid failed (${res.status})`);
  }
  return res.json();
}
