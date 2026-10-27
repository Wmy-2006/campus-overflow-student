package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.event.ProcessedEventStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock private LeaderboardStore store;
    @Mock private ReputationAccountRepository accounts;
    @Mock private IdentityApi identity;
    @Mock private ProcessedEventStore processed;

    private LeaderboardService leaderboard;

    @BeforeEach
    void setUp() {
        leaderboard = new LeaderboardService(store, accounts, identity, processed, clock);
    }

    @Test
    @DisplayName("on：声誉变化时写入 Redis ZSET")
    void onWritesToLeaderboard() {
        ReputationChangedEvent e = new ReputationChangedEvent("evt-1", NOW, 42L, 15, 150, "ANSWER_ACCEPTED");
        leaderboard.on(e);
        verify(store).increment(42L, 15, NOW);
    }

    @Test
    @DisplayName("top ALL：直接读账户表并拼接用户名")
    void topAllReadsFromAccountTable() {
        ReputationAccount acc = ReputationAccount.reconstitute(42L, 150, 0, 0, 1L);
        when(accounts.topByReputation(10)).thenReturn(List.of(acc));
        when(identity.findUsers(List.of(42L))).thenReturn(Map.of(42L, new UserSummary(42L, "小明", com.campusoverflow.shared.security.Role.STUDENT, true, "CS")));

        List<LeaderboardEntryView> result = leaderboard.top(LeaderboardPeriod.ALL, 10);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).displayName()).isEqualTo("小明");
        assertThat(result.get(0).score()).isEqualTo(150);
        assertThat(result.get(0).verified()).isTrue();
    }

    @Test
    @DisplayName("top WEEK：从 Redis ZSET 读")
    void topWeekReadsFromRedis() {
        when(store.top(LeaderboardPeriod.WEEK, NOW, 10)).thenReturn(List.of(new LeaderboardStore.Entry(42L, 30L)));
        when(identity.findUsers(List.of(42L))).thenReturn(Map.of(42L, new UserSummary(42L, "小明", com.campusoverflow.shared.security.Role.STUDENT, false, "CS")));

        List<LeaderboardEntryView> result = leaderboard.top(LeaderboardPeriod.WEEK, 10);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).score()).isEqualTo(30);
        assertThat(result.get(0).verified()).isFalse();
    }
}
