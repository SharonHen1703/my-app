export interface AuctionListItem {
  id: number;
  title: string;
  description: string;
  minPrice: number;
  bidIncrement: number;
  currentBidAmount: number | null;
  bidsCount: number;
  minBidToPlace: number;
  endDate: string; // ISO string מהשרת
  imageUrls: string[]; // מערך כל התמונות
  firstImageUrl?: string | null;
}

export interface AuctionsListResponse {
  total: number;
  size: number;
  totalPages: number;
  page: number;
  items: AuctionListItem[];
}

export interface PlaceBidRequest {
  bidderId: number;
  maxBid: number;
}

export interface PlaceBidResponse {
  auctionId: number;
  highestUserId: number | null;
  highestMaxBid: number | null;
  currentPrice: number;
  bidsCount: number;
  minNextBid: number;
  youAreLeading: boolean;
  endsAt: string; // ISO
}
