import styles from "./App.module.css";
import AuctionsList from "./features/auctions/components/AuctionsList";

function App() {
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>מכרזים פעילים</h1>
      <AuctionsList />
    </div>
  );
}
export default App;
