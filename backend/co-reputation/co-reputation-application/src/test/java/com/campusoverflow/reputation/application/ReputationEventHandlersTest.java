package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 事件联动用例：采纳/投票 → 声誉计分的编排与幂等。 */
@ExtendWith(MockitoExtension.class)
class ReputationEventHandlersTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private ReputationAccountRepository accounts;
    @Mock
    private com.campusoverflow.reputation.domain.LedgerRepository ledger;
    @Mock
    private ReputationLedgerService ledgerService;
    @Mock
    private BadgeService badges;
    @Mock
    private BountyService bounties;

    private ReputationEventHandlers handlers;

    @BeforeEach
    void setUp() {
        handlers = new ReputationEventHandlers(accounts, ledger, ledgerService, badges, bounties, clock);
    }

    @Test
    @DisplayName("注册开户：教师发放 1000 悬赏启动积分")
    void userRegisteredOpensAccountForTeacher() {
        when(accounts.findByUserId(9L)).thenReturn(Optional.empty());

        handlers.on(new UserRegisteredEvent("evt-reg", NOW, 9L, "王老师", com.campusoverflow.shared.security.Role.TEACHER));

        verify(accounts).save(argThat(a -> a.userId() == 9L && a.availablePoints() == 1000 && a.reputation() == 0));
    }

    @Test
    @DisplayName("注册开户：学生无悬赏启动积分")
    void userRegisteredOpensZeroPointsForStudent() {
        when(accounts.findByUserId(1L)).thenReturn(Optional.empty());

        handlers.on(new UserRegisteredEvent("evt-reg2", NOW, 1L, "小明", com.campusoverflow.shared.security.Role.STUDENT));

        verify(accounts).save(argThat(a -> a.availablePoints() == 0));
    }

    @Test
    @DisplayName("采纳：回答者 +15，自问自答不加分")
    void acceptedAnswersEarnFifteenExceptSelfAccepted() {
        handlers.on(new AnswerAcceptedEvent("evt-acc", NOW, 50L, 10L, 2L, 1L, 1L, "红黑树"));
        verify(ledgerService).record(eq(2L), eq("evt-acc"), eq(ReputationReason.ANSWER_ACCEPTED), eq(15),
                eq("ANSWER"), eq(50L));
        verify(bounties).settleOnAccept(10L, 50L, 2L, "evt-acc");

        handlers.on(new AnswerAcceptedEvent("evt-self", NOW, 51L, 11L, 1L, 1L, 1L, "自问自答"));
        verify(ledgerService).record(eq(1L), eq("evt-self"), eq(ReputationReason.ANSWER_ACCEPTED), eq(0),
                eq("ANSWER"), eq(51L));
    }

    @Test
    @DisplayName("投票：赞改踩按新效果-旧效果 -12 计分")
    void voteCastAppliesDeltaAndDailyCap() {
        when(ledger.sumPositiveVoteDeltaSince(eq(2L), any(Instant.class))).thenReturn(0);

        handlers.on(new VoteCastEvent("evt-vote", NOW, "ANSWER", 50L, 2L, 1L, 10L, 1, -1));

        verify(ledgerService).record(eq(2L), eq("evt-vote"), eq(ReputationReason.ANSWER_VOTE), eq(-12),
                eq("ANSWER"), eq(50L));
    }

    @Test
    @DisplayName("投票：正向增量受每日上限 200 约束")
    void votePositiveGainIsCappedDaily() {
        when(ledger.sumPositiveVoteDeltaSince(eq(2L), any(Instant.class))).thenReturn(195);

        handlers.on(new VoteCastEvent("evt-vote2", NOW, "ANSWER", 50L, 2L, 3L, 10L, -1, 1));

        verify(ledgerService).record(eq(2L), eq("evt-vote2"), eq(ReputationReason.ANSWER_VOTE), eq(5),
                eq("ANSWER"), eq(50L));
    }
}
