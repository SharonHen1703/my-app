import React from "react";
import type { AuctionListItem } from "../../utils/types";
import { fetchAuctions, fetchCategoriesMap } from "../../api";
import styles from "./index.module.css";
import AuctionCard from "../AuctionCard";
import FilterSidebar from "../FilterSidebar";

export default function AuctionsList() {
  // הפעל scrollRestoration של הדפדפן למניעת גלילה אוטומטית לראש הדף
  React.useEffect(() => {
    if ("scrollRestoration" in window.history) {
      window.history.scrollRestoration = "manual";
    }
    return () => {
      if ("scrollRestoration" in window.history) {
        window.history.scrollRestoration = "auto";
      }
    };
  }, []);
  // אין צורך ב-allItems
  const [error, setError] = React.useState<string | null>(null);
  const [loading, setLoading] = React.useState(false);
  const [currentPage, setCurrentPage] = React.useState(1);
  const [totalPages, setTotalPages] = React.useState(1);
  const [showScrollTop, setShowScrollTop] = React.useState(false);
  const [selectedCategory, setSelectedCategory] = React.useState<string>("");
  const [categories, setCategories] = React.useState<Record<string, string>>(
    {}
  );
  const [categoriesLoading, setCategoriesLoading] = React.useState(true);
  const [minPrice, setMinPrice] = React.useState<number | null>(null);
  const [maxPrice, setMaxPrice] = React.useState<number | null>(null);
  const [conditions, setConditions] = React.useState<string[]>([]);
  const [searchText, setSearchText] = React.useState<string>("");

  // הצג תמיד את כל 20 המכרזים של העמוד בבת אחת
  const [itemsInCurrentPage, setItemsInCurrentPage] = React.useState<
    AuctionListItem[]
  >([]);
  const ITEMS_PER_PAGE = 20; // 20 פריטים לכל עמוד

  // Use refs to avoid dependency issues
  const loadingRef = React.useRef(false);

  // שמירת מיקום גלילה לכל עמוד
  const pageScrollPositions = React.useRef<Map<number, number>>(new Map());

  // פונקציה לשמירת מיקום הגלילה של העמוד הנוכחי
  const saveCurrentPageScrollPosition = React.useCallback(() => {
    const scrollPosition = window.scrollY;
    pageScrollPositions.current.set(currentPage, scrollPosition);
    // שמור גם ב-sessionStorage תמיד
    sessionStorage.setItem(
      "auctions_scroll_position",
      JSON.stringify({
        page: currentPage,
        position: scrollPosition,
      })
    );
  }, [currentPage]);

  // פונקציה לשחזור מיקום הגלילה של עמוד מסוים
  const restorePageScrollPosition = React.useCallback(
    (page: number, fromSession = false) => {
      let savedPosition: number | undefined;
      if (fromSession) {
        // נסה לשחזר מה-sessionStorage
        const session = sessionStorage.getItem("auctions_scroll_position");
        if (session) {
          try {
            const parsed = JSON.parse(session);
            if (parsed.page === page && typeof parsed.position === "number") {
              savedPosition = parsed.position;
            }
          } catch {
            // ignore JSON parse errors
          }
        }
      } else {
        savedPosition = pageScrollPositions.current.get(page);
      }
      if (savedPosition !== undefined) {
        window.scrollTo({ top: savedPosition, behavior: "auto" });
        setTimeout(() => {
          window.scrollTo({ top: savedPosition, behavior: "auto" });
        }, 50);
        setTimeout(() => {
          window.scrollTo({ top: savedPosition, behavior: "auto" });
        }, 200);
        setTimeout(() => {
          window.scrollTo({ top: savedPosition, behavior: "auto" });
        }, 500);
      }
    },
    []
  );
  // בשחזור ראשוני (mount) - אם יש sessionStorage, טען עם restorePosition=true
  // דגל לזיהוי האם צריך לשחזר מיקום מה-sessionStorage
  const [shouldRestoreScroll, setShouldRestoreScroll] = React.useState<{
    page: number;
    position: number;
  } | null>(null);

  React.useEffect(() => {
    const session = sessionStorage.getItem("auctions_scroll_position");
    let restored = false;
    if (session) {
      try {
        const parsed = JSON.parse(session);
        if (
          typeof parsed.page === "number" &&
          typeof parsed.position === "number"
        ) {
          // רק שחזור scroll position, fetchAuctionsData יקרא בuseEffect אחר
          setShouldRestoreScroll({
            page: parsed.page,
            position: parsed.position,
          });
          restored = true;
        }
      } catch {
        /* empty */
      }
    }
    if (!restored) {
      // רק עדכון URL וstate, fetchAuctionsData יקרא בuseEffect אחר
      const urlParams = new URLSearchParams(window.location.search);
      const pageFromUrl = urlParams.get("page");
      if (pageFromUrl) {
        const pageNumber = parseInt(pageFromUrl, 10);
        if (pageNumber > 0) {
          window.history.replaceState(
            { page: pageNumber },
            "",
            window.location.href
          );
          return;
        }
      }
      const currentUrl = new URL(window.location.href);
      currentUrl.searchParams.set("page", "1");
      window.history.replaceState({ page: 1 }, "", currentUrl.toString());
    }
  }, []);

  // טעינת קטגוריות
  React.useEffect(() => {
    const loadCategories = async () => {
      try {
        setCategoriesLoading(true);
        const categoriesData = await fetchCategoriesMap();
        setCategories(categoriesData);
      } catch (err) {
        console.error("Failed to load categories:", err);
      } finally {
        setCategoriesLoading(false);
      }
    };

    loadCategories();
  }, []);

  // אפקט שמבצע את השחזור בפועל רק אחרי שהפריטים נטענו
  React.useEffect(() => {
    if (
      shouldRestoreScroll &&
      currentPage === shouldRestoreScroll.page &&
      itemsInCurrentPage.length > 0
    ) {
      // נמתין שכל התמונות ייטענו
      if (!shouldRestoreScroll) return;
      const images = Array.from(
        document.querySelectorAll("." + styles.list + " img")
      ) as HTMLImageElement[];
      let loadedCount = 0;
      let done = false;
      function tryRestore() {
        if (done || !shouldRestoreScroll) return;
        let attempts = 0;
        const maxAttempts = 100;
        const target = shouldRestoreScroll.position;
        function scrollLoop() {
          const pageHeight = document.documentElement.scrollHeight;
          const viewportHeight = window.innerHeight;
          if (pageHeight - target < viewportHeight && attempts < maxAttempts) {
            attempts++;
            requestAnimationFrame(scrollLoop);
            return;
          }
          window.scrollTo({ top: target, behavior: "auto" });
          attempts++;
          if (
            Math.abs(window.scrollY - target) < 2 ||
            attempts >= maxAttempts
          ) {
            setShouldRestoreScroll(null);
            done = true;
            return;
          }
          requestAnimationFrame(scrollLoop);
        }
        scrollLoop();
      }
      if (images.length === 0) {
        tryRestore();
      } else {
        images.forEach((img) => {
          if ((img as HTMLImageElement).complete) {
            loadedCount++;
          } else {
            img.addEventListener("load", () => {
              loadedCount++;
              if (loadedCount === images.length) {
                tryRestore();
              }
            });
            img.addEventListener("error", () => {
              loadedCount++;
              if (loadedCount === images.length) {
                tryRestore();
              }
            });
          }
        });
        if (loadedCount === images.length) {
          tryRestore();
        }
      }
    }
  }, [shouldRestoreScroll, currentPage, itemsInCurrentPage.length]);

  // שמור את מיקום הגלילה ב-sessionStorage רק ב-beforeunload (רענון/סגירה)
  React.useEffect(() => {
    const handleBeforeUnload = () => {
      sessionStorage.setItem(
        "auctions_scroll_position",
        JSON.stringify({
          page: currentPage,
          position: window.scrollY,
        })
      );
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [currentPage]);

  // מחק את sessionStorage רק כאשר יש מעבר עמוד (ולא ב-unmount)

  const fetchAuctionsData = React.useCallback(
    async (
      page: number,
      restorePosition = false,
      overrideConditions?: string[],
      overrideMinPrice?: number | null,
      overrideMaxPrice?: number | null,
      overrideCategory?: string,
      overrideSearchText?: string
    ) => {
      if (loadingRef.current) return;

      loadingRef.current = true;
      setLoading(true);

      try {
        const effectiveConditions =
          overrideConditions !== undefined ? overrideConditions : conditions;
        const effectiveMinPrice =
          overrideMinPrice !== undefined ? overrideMinPrice : minPrice;
        const effectiveMaxPrice =
          overrideMaxPrice !== undefined ? overrideMaxPrice : maxPrice;
        const effectiveCategory =
          overrideCategory !== undefined ? overrideCategory : selectedCategory;
        const effectiveSearchText =
          overrideSearchText !== undefined ? overrideSearchText : searchText;

        console.log("Calling fetchAuctions with parameters:", {
          page,
          category: effectiveCategory,
          minPrice: effectiveMinPrice,
          maxPrice: effectiveMaxPrice,
          conditions: effectiveConditions,
          searchText: effectiveSearchText,
        });

        const data = await fetchAuctions(
          page,
          ITEMS_PER_PAGE,
          effectiveCategory || undefined,
          effectiveMinPrice,
          effectiveMaxPrice,
          effectiveConditions,
          effectiveSearchText
        );

        // אין צורך ב-allItems
        setCurrentPage(page + 1); // API מתחיל מ-0, UI מתחיל מ-1
        setTotalPages(data.totalPages);

        // טען את כל 20 הפריטים של העמוד בבת אחת
        setItemsInCurrentPage(data.items);

        // גלול לראש העמוד או שחזר מיקום קודם
        if (restorePosition) {
          setTimeout(() => {
            restorePageScrollPosition(page + 1, true);
          }, 0);
          setTimeout(() => {
            restorePageScrollPosition(page + 1, true);
          }, 100);
        } else {
          setTimeout(() => {
            window.scrollTo({ top: 0, behavior: "smooth" });
          }, 100);
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : String(e));
      } finally {
        loadingRef.current = false;
        setLoading(false);
      }
    },
    [
      ITEMS_PER_PAGE,
      restorePageScrollPosition,
      selectedCategory,
      minPrice,
      maxPrice,
      conditions,
      searchText,
    ]
  );

  // אין טעינה אינסופית - כל הפריטים נטענים בבת אחת

  const handlePlaceBid = React.useCallback((auction: AuctionListItem) => {
    // פתח בטאב חדש
    window.open(`/auction/${auction.id}`, "_blank");
  }, []);

  const handleCategoryChange = React.useCallback(
    (category: string) => {
      setSelectedCategory(category);
      setCurrentPage(1); // איפוס לעמוד ראשון
      // נקה את sessionStorage כי שינינו סינון
      sessionStorage.removeItem("auctions_scroll_position");
      pageScrollPositions.current.clear();

      // עדכן את ה-URL
      const currentUrl = new URL(window.location.href);
      currentUrl.searchParams.set("page", "1");
      if (category) currentUrl.searchParams.set("category", category);
      else currentUrl.searchParams.delete("category");
      if (minPrice != null)
        currentUrl.searchParams.set("minPrice", String(minPrice));
      else currentUrl.searchParams.delete("minPrice");
      if (maxPrice != null)
        currentUrl.searchParams.set("maxPrice", String(maxPrice));
      else currentUrl.searchParams.delete("maxPrice");
      currentUrl.searchParams.delete("condition");
      for (const c of conditions)
        currentUrl.searchParams.append("condition", c);
      window.history.pushState(
        { page: 1, category, minPrice, maxPrice, condition: conditions },
        "",
        currentUrl.toString()
      );

      // טען נתונים חדשים
      setTimeout(() => {
        fetchAuctionsData(0, false);
      }, 0);
    },
    [fetchAuctionsData, conditions, minPrice, maxPrice]
  );

  const goToPage = React.useCallback(
    (page: number) => {
      // שמור את המיקום הנוכחי לפני מעבר
      saveCurrentPageScrollPosition();

      // הוסף entry לhistory של הדפדפן
      const currentUrl = new URL(window.location.href);
      currentUrl.searchParams.set("page", page.toString());
      if (selectedCategory)
        currentUrl.searchParams.set("category", selectedCategory);
      else currentUrl.searchParams.delete("category");
      if (minPrice != null)
        currentUrl.searchParams.set("minPrice", String(minPrice));
      else currentUrl.searchParams.delete("minPrice");
      if (maxPrice != null)
        currentUrl.searchParams.set("maxPrice", String(maxPrice));
      else currentUrl.searchParams.delete("maxPrice");
      currentUrl.searchParams.delete("condition");
      for (const c of conditions)
        currentUrl.searchParams.append("condition", c);
      window.history.pushState(
        {
          page,
          category: selectedCategory,
          minPrice,
          maxPrice,
          condition: conditions,
        },
        "",
        currentUrl.toString()
      );

      // טען את העמוד החדש תמיד מהראש (ללא שחזור מיקום)
      fetchAuctionsData(page - 1, false); // ממיר מ-UI (1-based) ל-API (0-based)
    },
    [
      fetchAuctionsData,
      saveCurrentPageScrollPosition,
      selectedCategory,
      minPrice,
      maxPrice,
      conditions,
    ]
  );

  // טעינה ראשונית - קריאת URL פרמטרים וטעינת נתונים
  React.useEffect(() => {
    // בדוק אם יש פרמטרים ב-URL
    const urlParams = new URLSearchParams(window.location.search);
    const pageFromUrl = urlParams.get("page");
    const categoryFromUrl = urlParams.get("category");
    const minFromUrl = urlParams.get("minPrice");
    const maxFromUrl = urlParams.get("maxPrice");
    const condsFromUrl = urlParams.getAll("condition");
    const searchFromUrl = urlParams.get("search");

    console.log("URL Parameters parsed:", {
      categoryFromUrl,
      pageFromUrl,
      minFromUrl,
      maxFromUrl,
      condsFromUrl,
      searchFromUrl,
      fullUrl: window.location.href,
    });

    // קבע את הקטגוריה מה-URL
    if (categoryFromUrl) setSelectedCategory(categoryFromUrl);
    setMinPrice(minFromUrl ? Number(minFromUrl) : null);
    setMaxPrice(maxFromUrl ? Number(maxFromUrl) : null);
    setConditions(condsFromUrl);
    setSearchText(searchFromUrl || "");

    // קבע את הדף
    let targetPage = 1;
    if (pageFromUrl) {
      const pageNumber = parseInt(pageFromUrl, 10);
      if (pageNumber > 0) {
        targetPage = pageNumber;
        setCurrentPage(pageNumber);
        window.history.replaceState(
          { page: pageNumber, category: categoryFromUrl },
          "",
          window.location.href
        );
      }
    } else {
      // התחל מעמוד 1
      setCurrentPage(1);
      const currentUrl = new URL(window.location.href);
      currentUrl.searchParams.set("page", "1");
      if (categoryFromUrl)
        currentUrl.searchParams.set("category", categoryFromUrl);
      if (minFromUrl) currentUrl.searchParams.set("minPrice", minFromUrl);
      if (maxFromUrl) currentUrl.searchParams.set("maxPrice", maxFromUrl);
      for (const c of condsFromUrl)
        currentUrl.searchParams.append("condition", c);
      window.history.replaceState(
        {
          page: 1,
          category: categoryFromUrl,
          minPrice: minFromUrl,
          maxPrice: maxFromUrl,
          condition: condsFromUrl,
        },
        "",
        currentUrl.toString()
      );
    }

    // עכשיו טען את הנתונים עם הפרמטרים המעודכנים
    const session = sessionStorage.getItem("auctions_scroll_position");
    let shouldRestore = false;
    if (session) {
      try {
        const parsed = JSON.parse(session);
        if (
          typeof parsed.page === "number" &&
          typeof parsed.position === "number"
        ) {
          shouldRestore = true;
          fetchAuctionsData(
            parsed.page - 1,
            true,
            condsFromUrl,
            minFromUrl ? Number(minFromUrl) : null,
            maxFromUrl ? Number(maxFromUrl) : null,
            categoryFromUrl || undefined,
            searchFromUrl || undefined
          );
          return;
        }
      } catch {
        /* empty */
      }
    }

    if (!shouldRestore) {
      fetchAuctionsData(
        targetPage - 1,
        false,
        condsFromUrl,
        minFromUrl ? Number(minFromUrl) : null,
        maxFromUrl ? Number(maxFromUrl) : null,
        categoryFromUrl || undefined,
        searchFromUrl || undefined
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // ריק - רק פעם אחת בטעינה

  // טעינת נתונים כאשר הפילטרים משתנים
  React.useEffect(() => {
    fetchAuctionsData(currentPage - 1);
  }, [fetchAuctionsData, currentPage]);

  // טיפול בניווט של הדפדפן (Back/Forward)
  React.useEffect(() => {
    const handlePopState = (event: PopStateEvent) => {
      if (event.state && event.state.page) {
        // שמור את המיקום הנוכחי מיידית לפני מעבר
        const currentScrollPosition = window.scrollY;
        pageScrollPositions.current.set(currentPage, currentScrollPosition);

        const pageFromHistory = event.state.page;
        const categoryFromHistory = event.state.category || "";
        const minFromHistory = event.state.minPrice ?? null;
        const maxFromHistory = event.state.maxPrice ?? null;
        const condsFromHistory: string[] = event.state.condition ?? [];

        // עדכן את הקטגוריה הנבחרת
        setSelectedCategory(categoryFromHistory);
        setMinPrice(minFromHistory);
        setMaxPrice(maxFromHistory);
        setConditions(Array.isArray(condsFromHistory) ? condsFromHistory : []);

        const hasStoredPosition =
          pageScrollPositions.current.has(pageFromHistory);

        // טען את העמוד עם שחזור מיקום אם יש
        fetchAuctionsData(pageFromHistory - 1, hasStoredPosition);
      }
    };

    // שמור מיקום לפני יציאה מהדף (לטיפול בניווט דפדפן)
    const handleBeforeUnload = () => {
      saveCurrentPageScrollPosition();
    };

    // שמור מיקום כאשר החלון מאבד פוקוס (עובר לטאב אחר)
    const handleVisibilityChange = () => {
      if (document.visibilityState === "hidden") {
        saveCurrentPageScrollPosition();
      }
    };

    window.addEventListener("popstate", handlePopState);
    window.addEventListener("beforeunload", handleBeforeUnload);
    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      window.removeEventListener("popstate", handlePopState);
      window.removeEventListener("beforeunload", handleBeforeUnload);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [fetchAuctionsData, saveCurrentPageScrollPosition, currentPage]);

  // מעקב אחר גלילה רק לכפתור חזרה לראש ולשמירת מיקום
  React.useEffect(() => {
    let saveScrollTimeout: number;
    let scrollEndTimeout: number;

    const handleScroll = () => {
      setShowScrollTop(window.scrollY > 300);

      // שמור את המיקום הנוכחי בעיכוב קצר יותר (debounce)
      clearTimeout(saveScrollTimeout);
      saveScrollTimeout = setTimeout(() => {
        saveCurrentPageScrollPosition();
      }, 50);

      // שמור גם כאשר הגלילה מסתיימת
      clearTimeout(scrollEndTimeout);
      scrollEndTimeout = setTimeout(() => {
        saveCurrentPageScrollPosition();
      }, 150);
    };

    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => {
      clearTimeout(saveScrollTimeout);
      clearTimeout(scrollEndTimeout);
      window.removeEventListener("scroll", handleScroll);
    };
  }, [saveCurrentPageScrollPosition]);

  // פונקציה לחזרה לראש העמוד
  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  if (error) {
    return <div className={styles.error}>שגיאה: {error}</div>;
  }

  const pageNumbers = [];
  for (let i = 1; i <= totalPages; i++) {
    pageNumbers.push(i);
  }

  return (
    <div className={styles.container}>
      <div className={styles.headerSection}>
        <h1 className={styles.title}>מכרזים פעילים</h1>
        <div className={styles.headerActions}>
          <a
            href="/my-auctions-smart"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.myAuctionsLink}
          >
            המכרזים שלי
          </a>
          <a
            href="/my-bids"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.myBidsLink}
          >
            ההצעות שלי
          </a>
        </div>
      </div>

      <div className={styles.mainContent}>
        <FilterSidebar
          categories={categories}
          selectedCategory={selectedCategory}
          onCategoryChange={handleCategoryChange}
          minPrice={minPrice}
          maxPrice={maxPrice}
          searchText={searchText}
          onSearchChange={(text) => {
            setSearchText(text);
            setCurrentPage(1);
            sessionStorage.removeItem("auctions_scroll_position");
            pageScrollPositions.current.clear();
            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.set("page", "1");
            if (selectedCategory)
              currentUrl.searchParams.set("category", selectedCategory);
            else currentUrl.searchParams.delete("category");
            if (minPrice != null)
              currentUrl.searchParams.set("minPrice", String(minPrice));
            else currentUrl.searchParams.delete("minPrice");
            if (maxPrice != null)
              currentUrl.searchParams.set("maxPrice", String(maxPrice));
            else currentUrl.searchParams.delete("maxPrice");
            currentUrl.searchParams.delete("condition");
            for (const c of conditions)
              currentUrl.searchParams.append("condition", c);
            if (text && text.trim())
              currentUrl.searchParams.set("search", text.trim());
            else currentUrl.searchParams.delete("search");
            window.history.pushState(
              {
                page: 1,
                category: selectedCategory,
                minPrice,
                maxPrice,
                condition: conditions,
                search: text,
              },
              "",
              currentUrl.toString()
            );
            fetchAuctionsData(
              0,
              false,
              undefined,
              undefined,
              undefined,
              undefined,
              text
            );
          }}
          onPriceApply={(min, max) => {
            setMinPrice(min);
            setMaxPrice(max);
            setCurrentPage(1);
            sessionStorage.removeItem("auctions_scroll_position");
            pageScrollPositions.current.clear();
            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.set("page", "1");
            if (selectedCategory)
              currentUrl.searchParams.set("category", selectedCategory);
            else currentUrl.searchParams.delete("category");
            if (min != null)
              currentUrl.searchParams.set("minPrice", String(min));
            else currentUrl.searchParams.delete("minPrice");
            if (max != null)
              currentUrl.searchParams.set("maxPrice", String(max));
            else currentUrl.searchParams.delete("maxPrice");
            currentUrl.searchParams.delete("condition");
            for (const c of conditions)
              currentUrl.searchParams.append("condition", c);
            window.history.pushState(
              {
                page: 1,
                category: selectedCategory,
                minPrice: min,
                maxPrice: max,
                condition: conditions,
              },
              "",
              currentUrl.toString()
            );
            fetchAuctionsData(0, false, undefined, min, max);
          }}
          selectedConditions={conditions}
          onConditionsChange={(next) => {
            setConditions(next);
            setCurrentPage(1);
            sessionStorage.removeItem("auctions_scroll_position");
            pageScrollPositions.current.clear();
            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.set("page", "1");
            if (selectedCategory)
              currentUrl.searchParams.set("category", selectedCategory);
            else currentUrl.searchParams.delete("category");
            if (minPrice != null)
              currentUrl.searchParams.set("minPrice", String(minPrice));
            else currentUrl.searchParams.delete("minPrice");
            if (maxPrice != null)
              currentUrl.searchParams.set("maxPrice", String(maxPrice));
            else currentUrl.searchParams.delete("maxPrice");
            currentUrl.searchParams.delete("condition");
            for (const c of next)
              currentUrl.searchParams.append("condition", c);
            window.history.pushState(
              {
                page: 1,
                category: selectedCategory,
                minPrice,
                maxPrice,
                condition: next,
              },
              "",
              currentUrl.toString()
            );
            fetchAuctionsData(0, false, next);
          }}
          loading={categoriesLoading}
        />

        <div className={styles.contentArea}>
          {loading && itemsInCurrentPage.length === 0 && (
            <div className={styles.loading}>
              <div className={styles.spinner}></div>
              <p>טוען מכרזים...</p>
            </div>
          )}

          <div className={styles.list}>
            {itemsInCurrentPage.map((auction) => (
              <AuctionCard
                key={auction.id}
                item={auction}
                onClick={handlePlaceBid}
              />
            ))}
          </div>

          {showScrollTop && (
            <button
              className={styles.scrollToTop}
              onClick={scrollToTop}
              title="חזרה לראש הדף"
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <circle cx="12" cy="12" r="12" fill="white" />
                <path d="M12 8L8 12H11V16H13V12H16L12 8Z" fill="#2563eb" />
              </svg>
            </button>
          )}

          <div className={styles.pagination}>
            <button
              onClick={() => goToPage(currentPage - 1)}
              disabled={currentPage === 1}
              className={styles.pageButton}
            >
              הקודם
            </button>
            {pageNumbers.map((pageNum) => (
              <button
                key={pageNum}
                onClick={() => goToPage(pageNum)}
                className={`${styles.pageButton} ${
                  pageNum === currentPage ? styles.activePage : ""
                }`}
              >
                {pageNum}
              </button>
            ))}
            <button
              onClick={() => goToPage(currentPage + 1)}
              disabled={currentPage === totalPages}
              className={styles.pageButton}
            >
              הבא
            </button>
          </div>

          <div className={styles.count}>
            {itemsInCurrentPage.length} מכרזים פעילים בדף
          </div>
        </div>
      </div>
    </div>
  );
}
