export interface AuctionListItem {
  id: number;
  title: string;
  description: string;
  condition: string;
  categories: string;
  minPrice: number;
  bidIncrement: number;
  currentBidAmount: number | null;
  bidsCount: number;
  minBidToPlace: number;
  endDate: string; // ISO string מהשרת
  imageUrls: string[]; // מערך כל התמונות
  firstImageUrl?: string | null;
  sellerId?: number; // Optional for backward compatibility
  status?: string; // Optional for backward compatibility
}

// Types for user's own auctions management
export interface UserAuctionItem {
  id: number;
  title: string;
  currentPrice: number;
  auctionStatus: string; // 'active', 'ended', etc.
  bidsCount: number;
  endDate: string; // ISO date string
}

export interface AuctionsListResponse {
  total: number;
  size: number;
  totalPages: number;
  page: number;
  items: AuctionListItem[];
}

export interface PlaceBidRequest {
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

// Add Item Form Types
export interface CreateAuctionForm {
  title: string;
  description: string;
  condition: string;
  categories: string[];
  minPrice: string;
  bidIncrement: string;
  startDate: string;
  endDate: string;
}

export interface CreateAuctionRequest {
  title: string;
  description: string;
  condition: string;
  categories: string[];
  minPrice: number;
  bidIncrement: number;
  startDate: string; // ISO string
  endDate: string; // ISO string
  status: string;
  bidsCount: number;
}

export interface CreateAuctionResponse {
  id: number;
  title: string;
  message?: string;
}
