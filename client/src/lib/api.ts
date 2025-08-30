export type AuctionListItem = {
  id: number;
  title: string;
  description: string;
  condition: string;
  minPrice: number;
  bidIncrement: number;
  currentBidAmount: number | null;
  bidsCount: number;
  minBidToPlace: number;
  endDate: string;
  imageUrls: string[];
};

export type AuctionDetail = AuctionListItem & {
  imageUrls: string[];
};

export type UserAuctionItem = {
  auctionId: number;
  title: string;
  description: string;
  startingPrice: number;
  currentPrice: number;
  status: "ACTIVE" | "ENDED";
  bidCount: number;
  startDate: string;
  endDate: string;
};

export async function listAuctions(page: number = 0, size: number = 20) {
  const r = await fetch(`/api/auctions?page=${page}&size=${size}`);
  if (!r.ok) throw new Error("failed to list auctions");
  return (await r.json()) as {
    page: number;
    size: number;
    total: number;
    totalPages: number;
    items: AuctionListItem[];
  };
}

export async function getAuction(id: number) {
  const r = await fetch(`/api/auctions/${id}`);
  if (!r.ok) throw new Error("auction not found");
  return (await r.json()) as AuctionDetail;
}

export async function placeBid(
  auctionId: number,
  userId: number,
  maxBid: number
) {
  const r = await fetch(`/api/auctions/${auctionId}/bids`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId, maxBid }),
  });
  if (!r.ok) {
    const text = await r.text();
    throw new Error(text || "failed to place bid");
  }
  return await r.json(); // חוזר AuctionListItem מעודכן
}

export async function getUserAuctions(
  userId: number
): Promise<UserAuctionItem[]> {
  const r = await fetch(`/api/users/${userId}/auctions`);
  if (!r.ok) throw new Error("failed to get user auctions");
  return await r.json();
}
