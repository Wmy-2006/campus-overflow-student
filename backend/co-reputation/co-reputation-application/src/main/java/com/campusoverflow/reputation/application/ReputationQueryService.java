package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReputationQueryService {

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;
    private final BadgeRepository badges;
    private final IdentityApi identity;

    public ReputationQueryService(ReputationAccountRepository accounts, LedgerRepository ledger,
                                  BadgeRepository badges, IdentityApi identity) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.badges = badges;
        this.identity = identity;
    }

    public ReputationProfileView profile(long userId, Actor viewerOrNull) {
        ReputationAccount account = accounts.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("RE-404", "声誉账户不存在"));
        UserSummary user = identity.findUser(userId)
                .orElseThrow(() -> new NotFoundException("ID-404", "用户不存在"));
        boolean self = viewerOrNull != null && viewerOrNull.userId() == userId;
        List<BadgeAward> awards = badges.findByUser(userId);
        List<ReputationProfileView.BadgeView> badgeViews = awards.stream()
                .map(a -> new ReputationProfileView.BadgeView(
                        a.type().name(), a.type().displayName(), a.type().description(), a.awardedAt()))
                .toList();
        return new ReputationProfileView(
                userId, user.displayName(), account.reputation(),
                self ? account.availablePoints() : null,
                self ? account.frozenPoints() : null,
                badgeViews);
    }

    /** 声誉流水属于个人数据：仅本人与管理员可查看。 */
    public PageResult<LedgerEntryView> ledger(long userId, Actor viewer, PageRequest page) {
        if (viewer.userId() != userId && !viewer.isAdmin()) {
            throw new ForbiddenException("RE-403", "只能查看自己的声誉流水");
        }
        accounts.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("RE-404", "声誉账户不存在"));
        PageResult<LedgerEntry> result = ledger.findByUser(userId, page);
        List<LedgerEntryView> views = result.items().stream()
                .map(e -> new LedgerEntryView(e.id(), e.reason().name(), e.reason().label(),
                        e.delta(), e.refType(), e.refId(), e.createdAt()))
                .toList();
        return new PageResult<>(views, result.total(), result.page(), result.size());
    }
}
