package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReputationQueryServiceTest {

    @Mock private ReputationAccountRepository accounts;
    @Mock private LedgerRepository ledger;
    @Mock private BadgeRepository badges;
    @Mock private IdentityApi identity;

    private ReputationQueryService queries;
    private static final long USER_ID = 42L;
    private static final Actor SELF = new Actor(USER_ID, "小明", Role.STUDENT);
    private static final Actor OTHER = new Actor(99L, "别人", Role.STUDENT);
    private static final Actor ADMIN = new Actor(0L, "管理员", Role.ADMIN);

    @BeforeEach
    void setUp() {
        queries = new ReputationQueryService(accounts, ledger, badges, identity);
    }

    @Test
    @DisplayName("profile：本人可见积分，他人不可见")
    void profileShowsPointsOnlyToSelf() {
        ReputationAccount acc = ReputationAccount.reconstitute(USER_ID, 150, 500, 100, 1L);
        when(accounts.findByUserId(USER_ID)).thenReturn(Optional.of(acc));
        when(identity.findUser(USER_ID)).thenReturn(Optional.of(new UserSummary(USER_ID, "小明", Role.STUDENT, true, "CS")));
        when(badges.findByUser(USER_ID)).thenReturn(List.of(new BadgeAward(USER_ID, BadgeType.FIRST_ANSWER, Instant.now())));

        ReputationProfileView self = queries.profile(USER_ID, SELF);
        assertThat(self.reputation()).isEqualTo(150);
        assertThat(self.availablePoints()).isEqualTo(500);
        assertThat(self.frozenPoints()).isEqualTo(100);
        assertThat(self.badges()).hasSize(1);

        ReputationProfileView other = queries.profile(USER_ID, OTHER);
        assertThat(other.availablePoints()).isNull();
        assertThat(other.frozenPoints()).isNull();
    }

    @Test
    @DisplayName("ledger：仅本人或管理员可查")
    void ledgerForbiddenForOthers() {
        assertThatThrownBy(() -> queries.ledger(USER_ID, OTHER, new PageRequest(1, 20)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("ledger：本人可查并返回流水分页")
    void ledgerReturnsEntriesForSelf() {
        when(accounts.findByUserId(USER_ID)).thenReturn(Optional.of(ReputationAccount.open(USER_ID, 0)));
        LedgerEntry entry = new LedgerEntry(1L, USER_ID, "evt-1", ReputationReason.ANSWER_ACCEPTED, 15, "ANSWER", 7L, Instant.now());
        when(ledger.findByUser(USER_ID, new PageRequest(1, 20)))
                .thenReturn(new PageResult<>(List.of(entry), 1, 1, 20));

        PageResult<LedgerEntryView> result = queries.ledger(USER_ID, SELF, new PageRequest(1, 20));
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).reasonLabel()).isEqualTo("回答被采纳");
        assertThat(result.items().get(0).delta()).isEqualTo(15);
    }
}
