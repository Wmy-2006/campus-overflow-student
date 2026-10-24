# CampusOverflow 聚合识别清单 v1.0

> 团队：CampusOverflow 开发小组
> 来源：Lab3 事件风暴工作坊（第5周 / Sprint 1）
> 聚合原则：按"命令-事件聚类"识别，一个聚合内强一致，聚合间通过领域事件最终一致。

## 聚合清单

| 聚合 | 所属限界上下文 | 根实体 | 关键不变式 |
|------|----------------|--------|------------|
| **User 用户** | Identity 认证 | User | 学号全局唯一；密码只存哈希；登录失败需节流限速 |
| **Course 课程** | Identity 认证 | Course | 课程成员角色唯一（一人一角色）；成员只能由管理员/老师添加 |
| **Question 问题** | Q&A 问答 | Question | 标题/正文非空；采纳的答案必须属于本问题；每问题仅一个采纳；已采纳后作者/提问者不可删除；删除需满足规则 |
| **Answer 回答** | Q&A 问答 | Answer | 回答正文非空；只能由作者或管理员编辑/删除；问题被删除后不可再回答 |
| **Vote 投票** | Q&A 问答 | Vote | 票值仅允许 +1/-1；同一用户对同一目标仅一票；不能给自己投票；撤销即删除 |
| **Comment 评论** | Q&A 问答 | Comment | 评论仅一层（不支持评论的评论）；正文非空 |
| **Tag 标签** | Q&A 问答 | Tag | 标签名规范化、去重；问题标签有上限 |
| **ReputationAccount 声誉账户** | Reputation 声誉 | ReputationAccount | 声誉增减必须写入 Ledger 账本（可审计）；积分规则由 ReputationRulebook 统一定义 |
| **LedgerEntry 账本条目** | Reputation 声誉 | LedgerEntry | 一次声誉变更对应一条账目，不可篡改 |
| **Bounty 悬赏** | Reputation 声誉 | Bounty | 悬赏额度必须来自提问者账户且有上限；到期后由系统自动发放且只发放一次 |
| **BadgeAward 徽章授予** | Reputation 声誉 | BadgeAward | 徽章按 BadgePolicy 规则触发；同一徽章不重复授予 |
| **Notification 通知** | Discovery 发现 | Notification | 同一事件对同一用户只产生一条通知（去重）；@提及可解析 |
| **SearchIndex 检索索引** | Discovery 发现 | SearchDocument | 读模型由领域事件投影维护，可异步最终一致 |

## 一致性边界说明

- **强一致（聚合内）**：Question 与其采纳状态、Vote 的一人一票、Bounty 的发放一次。
- **最终一致（聚合间）**：Q&A 的"回答已采纳"事件 → Reputation 计分；Q&A 的"问题已发布"事件 → Discovery 索引与通知。跨上下文通过领域事件（事务性发件箱）异步传递。

## 读模型（Read Model）

- QuestionDetailView：问题详情（正文 + 回答列表）——由 Q&A 提供
- LeaderboardView / ReputationProfileView：排行与声誉——由 Reputation 提供
- SearchResultView / RelatedQuestionView / NotificationView：检索、相关推荐、通知——由 Discovery 提供
