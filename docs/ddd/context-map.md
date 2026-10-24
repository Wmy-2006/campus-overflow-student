# CampusOverflow 上下文映射图 v1.0（Context Map）

> 团队：CampusOverflow 开发小组
> 来源：Lab3 事件风暴工作坊（第5周 / Sprint 1）
> 可视化：课堂白板/Miro 绘制上下文关系圈后截图留存；本文件为结构化描述。

## 限界上下文（Bounded Contexts）

| 上下文 | 职责 | 关键聚合 |
|--------|------|----------|
| **Identity（身份）** | 学号认证、登录、教师认证、课程与成员管理 | User、Course |
| **Q&A（问答）** | 问题/回答/投票/评论/标签，问答主链路 | Question、Answer、Vote、Comment、Tag |
| **Reputation（声誉）** | 声誉积分、徽章、悬赏、排行榜 | ReputationAccount、Bounty、BadgeAward |
| **Discovery（发现）** | 全文检索、相关推荐、通知 | Notification、SearchIndex |
| **Shared Kernel（共享内核）** | 审计、异常、领域事件基建、分页、角色常量 | 跨上下文共享 |

## 上下文间关系

```
                    领域事件(最终一致)
  Identity ────────→ Q&A ────────→ Reputation
      │ 提供作者/认证      │ 问题已发布      │ 消费事件→计分/发徽章
      │                    │               │
      │ 通知用户           ▼               ▼
      └──────────────── Discovery ←──────┘
                              │
                         检索/相关/通知
```

| 关系 | 上下文 A | 上下文 B | 类型 | 说明 |
|------|----------|----------|------|------|
| A → B | Identity | Q&A | **客户-供应商** | Q&A 依赖 Identity 提供的用户身份与认证 |
| A → B | Q&A | Reputation | **客户-供应商（事件驱动）** | Reputation 消费 Q&A 的领域事件计分 |
| A → B | Reputation | Q&A | 客户-供应商 | Q&A 展示 Reputation 的声誉/排行读模型 |
| A → B | Q&A | Discovery | 事件发布 | QuestionPosted 等事件驱动 Discovery 索引/推荐/通知 |
| A → B | Identity | Discovery | 事件发布 | 用户事件驱动通知 |
| 共享 | 各上下文 | Shared Kernel | **共享内核** | 审计、异常、事件基建共享，需保持稳定 |

## 设计决策（对应 ADR）

- 模块化单体（ADR-001），上下文物理分包
- 事务性发件箱保证跨上下文事件可靠（ADR-002）
- 服务端会话（ADR-003）、声誉账本（ADR-004）、中文全文检索（ADR-005）
