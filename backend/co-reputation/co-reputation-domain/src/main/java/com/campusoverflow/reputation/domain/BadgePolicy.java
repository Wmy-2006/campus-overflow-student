package com.campusoverflow.reputation.domain;

import java.util.EnumSet;
import java.util.Set;

/** 徽章授予规则（纯函数，易于测试与扩展）。 */
public final class BadgePolicy {

    private BadgePolicy() {
    }

    /** 按累计被采纳回答数授予徽章：≥1 初露锋芒，≥10 解惑达人。 */
    public static Set<BadgeType> forAcceptedCount(long acceptedCount) {
        EnumSet<BadgeType> badges = EnumSet.noneOf(BadgeType.class);
        if (acceptedCount >= 1) {
            badges.add(BadgeType.FIRST_ACCEPTED);
        }
        if (acceptedCount >= 10) {
            badges.add(BadgeType.HELPER_10);
        }
        return badges;
    }

    /** 按累计声誉授予徽章：≥100 小有名气，≥1000 德高望重。 */
    public static Set<BadgeType> forReputation(int reputation) {
        EnumSet<BadgeType> badges = EnumSet.noneOf(BadgeType.class);
        if (reputation >= 100) {
            badges.add(BadgeType.REPUTATION_100);
        }
        if (reputation >= 1000) {
            badges.add(BadgeType.REPUTATION_1000);
        }
        return badges;
    }
}
