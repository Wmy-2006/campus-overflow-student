package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.event.ProcessedEventStore;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 排行榜：周/月/学期榜来自 Redis ZSET，总榜直接读账户表。 */
@Service
public class LeaderboardService {

    private final LeaderboardStore store;
    private final ReputationAccountRepository accounts;
    private final IdentityApi identity;
    private final ProcessedEventStore processed;
    private final Clock clock;

    public LeaderboardService(LeaderboardStore store, ReputationAccountRepository accounts, IdentityApi identity,
                              ProcessedEventStore processed, Clock clock) {
        this.store = store;
        this.accounts = accounts;
        this.identity = identity;
        this.processed = processed;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void on(ReputationChangedEvent e) {
        store.increment(e.userId(), e.delta(), Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryView> top(LeaderboardPeriod period, int limit) {
        if (period == LeaderboardPeriod.ALL) {
            List<ReputationAccount> top = accounts.topByReputation(limit);
            List<Long> userIds = top.stream().map(ReputationAccount::userId).toList();
            Map<Long, UserSummary> users = identity.findUsers(userIds);
            List<LeaderboardEntryView> result = new ArrayList<>();
            for (int i = 0; i < top.size(); i++) {
                ReputationAccount acc = top.get(i);
                UserSummary u = users.get(acc.userId());
                result.add(new LeaderboardEntryView(i + 1, acc.userId(),
                        u != null ? u.displayName() : "用户" + acc.userId(),
                        u != null && u.verified(),
                        acc.reputation()));
            }
            return result;
        }
        Instant now = Instant.now(clock);
        List<LeaderboardStore.Entry> entries = store.top(period, now, limit);
        List<Long> userIds = entries.stream().map(LeaderboardStore.Entry::userId).toList();
        Map<Long, UserSummary> users = identity.findUsers(userIds);
        List<LeaderboardEntryView> result = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardStore.Entry e = entries.get(i);
            UserSummary u = users.get(e.userId());
            result.add(new LeaderboardEntryView(i + 1, e.userId(),
                    u != null ? u.displayName() : "用户" + e.userId(),
                    u != null && u.verified(),
                    e.score()));
        }
        return result;
    }
}
