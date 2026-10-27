package com.campusoverflow.reputation.domain;

import com.campusoverflow.shared.security.Role;

/**
 * 声誉规则手册——把“游戏规则”集中在一处，便于课堂讨论与参数调整。
 * <ul>
 *   <li>回答被采纳 +15（自问自答被采纳不加分）；</li>
 *   <li>被赞 +10，被踩 −2；改投、撤销按“新效果 − 旧效果”计算增量；</li>
 *   <li>反作弊：每人每个自然日（UTC）因被投票获得的正向声誉最多 200。</li>
 * </ul>
 */
public final class ReputationRulebook {

    public static final int ACCEPTED = 15;
    public static final int UPVOTE = 10;
    public static final int DOWNVOTE = -2;
    public static final int DAILY_VOTE_CAP = 200;

    private ReputationRulebook() {
    }

    /** 单个投票值（>0 赞 / <0 踩）折算的声誉效果。 */
    public static int voteEffect(int value) {
        if (value > 0) {
            return UPVOTE;
        }
        if (value < 0) {
            return DOWNVOTE;
        }
        return 0;
    }

    /** 改投 / 撤销按“新效果 − 旧效果”计算增量。 */
    public static int voteDelta(int oldValue, int newValue) {
        return voteEffect(newValue) - voteEffect(oldValue);
    }

    public static int acceptedDelta(long answerAuthorId, long questionAuthorId) {
        return answerAuthorId == questionAuthorId ? 0 : ACCEPTED;
    }

    /** 对正向投票增量应用每日上限；负向增量不受限。 */
    public static int applyDailyCap(int delta, int positiveVoteGainToday) {
        if (delta <= 0) {
            return delta;
        }
        int remaining = DAILY_VOTE_CAP - positiveVoteGainToday;
        if (remaining <= 0) {
            return 0;
        }
        return Math.min(delta, remaining);
    }

    /** 开户时发放的悬赏积分（与声誉是两种不同的“货币”）。 */
    public static int initialPoints(Role role) {
        return role == Role.TEACHER ? 1000 : 0;
    }
}
