export interface UserBidSummaryItem {
  auctionId: number;
  auctionTitle: string;
  currentPrice: number;
  yourMax: number;
  endDate: string; // ISO string from server
  leading: boolean;
  status: string; // "active" or "ended"
}

export interface MyBidsResponse {
  bids: UserBidSummaryItem[];
}
