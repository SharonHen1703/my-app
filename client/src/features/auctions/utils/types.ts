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
}
