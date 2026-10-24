# CampusOverflow 统一语言术语表 v1.0

> 团队：CampusOverflow 开发小组
> 来源：Lab3 事件风暴工作坊（第5周 / Sprint 1）
> 用途：全团队在需求、设计、代码、评审中统一使用同一术语，消除歧义。

## 领域通用术语

| 术语 | 英文 | 含义 |
|------|------|------|
| 用户 | User | 平台的参与者，包括学生、老师、管理员 |
| 学号 | Student Number | 唯一标识用户的身份凭证 |
| 认证 | Authentication | 校验用户身份的过程 |
| 教师认证 | Teacher Verification | 将教师账号标记为可信的过程 |
| 课程 | Course | 校园课程，问题与成员的组织维度 |
| 课程成员 | Course Member | 一门课程内的参与成员（学生/老师/助教） |

## 问答术语

| 术语 | 英文 | 含义 |
|------|------|------|
| 问题 | Question | 用户提出的待解答内容（含标题与正文） |
| 标题 | Title | 问题的简短描述 |
| 正文 | Body | 问题的详细描述（Markdown） |
| 回答 | Answer | 对某问题的解答内容 |
| 采纳答案 | Accepted Answer | 提问者确认的最佳回答 |
| 投票 | Vote | 对回答的 +1/-1 评价 |
| 点踩/反对 | Downvote | 对回答投 -1 |
| 评论 | Comment | 对问题或回答的追问 |
| 标签 | Tag | 用于分类检索问题的关键字 |
| 提问者 | Asker | 发起问题的用户 |
| 回答者 | Answerer | 提交回答的用户 |

## 声誉术语

| 术语 | 英文 | 含义 |
|------|------|------|
| 声誉积分 | Reputation | 用户在平台的可信度得分 |
| 账本 | Ledger | 声誉增减的审计记录 |
| 徽章 | Badge | 达成条件授予的荣誉标识 |
| 悬赏 | Bounty | 提问者用积分吸引回答的奖励 |
| 排行榜 | Leaderboard | 按声誉排列的贡献榜 |
| 规则引擎 | Rulebook | 定义积分与徽章规则的核心 |

## 发现与基础设施术语

| 术语 | 英文 | 含义 |
|------|------|------|
| 搜索 | Search | 按关键词检索问题 |
| 索引 | Search Index | 全文检索的数据结构 |
| 相关推荐 | Related Questions | 相似问题推荐 |
| 通知 | Notification | 事件触发的用户提醒 |
| 提及 | Mention | 通过 @ 通知某人 |
| 领域事件 | Domain Event | 聚合内发生的重要事实 |
| 事务性发件箱 | Outbox | 保证事件可靠发布的模式 |
| 限界上下文 | Bounded Context | 领域模型与语言的边界 |
| 聚合 | Aggregate | 强一致的实体与值对象边界 |
| 读模型 | Read Model | 面向查询的投影视图 |

## 事件术语（领域事件名）

| 事件 | 英文事件名 |
|------|------------|
| 用户已注册 | UserRegistered |
| 问题已发布 | QuestionPosted |
| 回答已提交 | AnswerSubmitted |
| 回答已采纳 | AnswerAccepted |
| 投票已投 | VoteCast |
| 评论已发布 | CommentPosted |
| 声誉已变更 | ReputationChanged |
| 徽章已授予 | BadgeAwarded |
| 悬赏已开启 | BountyOpened |
| 通知已发送 | NotificationSent |

> 术语表共 45 项，满足"至少 30 个术语"要求。团队全员认同后，后续代码、文档、PR 统一使用本表术语。
