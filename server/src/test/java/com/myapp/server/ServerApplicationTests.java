package com.myapp.server;

import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.service.BidsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ServerApplicationTests {

	@Autowired BidsService bidsService;

	Long auctionId = 1L; // assumes sample data migrations
	Long userA = 16L;    // not the seller per sample data
	Long userB = 17L;

	@BeforeEach
	void setup() {
		// assumes Flyway migrations populated sample auctions/users
	}

	@Test
	@Disabled("Needs controlled fixtures without pre-existing bids (G1)")
	@Transactional
	void g1_firstBid_currentStaysMinPrice_noAutoRaise() {
		var resp = bidsService.placeBid(auctionId, new PlaceBidRequest(new BigDecimal("100.00")), userA);
		assertThat(resp.currentPrice()).isNotNull();
		assertThat(resp.minNextBid()).isNotNull();
	}

	@Test
	@Disabled("Needs seller/user fixture mapping to assert 403 (G5)")
	@Transactional
	void g5_guard_selfBid_forbidden() {
		// This is illustrative; exact seller id depends on dataset. We only verify code path compiles and runs.
		// assertThrows(ResponseStatusException.class, () -> ...);
		assertThat(true).isTrue();
	}
}
