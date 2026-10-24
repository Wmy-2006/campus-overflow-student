# CampusOverflow 领域事件清单 v1.0（Event Storming · Step 1）

> 团队：CampusOverflow 开发小组
> 来源：Lab3 事件风暴工作坊（第5周 / Sprint 1）
> 说明：以下为橙色"领域事件"卡（过去时命名），按子域与时间线排列，作为工作坊白板底稿。团队成员可在此基础上增补。

## 认证（Identity）

- 用户已注册 UserRegistered
- 用户已登录 UserLoggedIn
- 教师认证已通过 TeacherVerified

## 问答（Q&A）

- 问题已发布 QuestionPosted
- 问题已编辑 QuestionEdited
- 回答已提交 AnswerSubmitted
- 回答已采纳 AnswerAccepted
- 投票已投 VoteCast
- 评论已发布 CommentPosted
- 内容已删除 ContentDeleted

## 声誉（Reputation）

- 声誉已变更 ReputationChanged
- 徽章已授予 BadgeAwarded
- 悬赏已开启 BountyOpened
- 悬赏已发放 BountyAwarded
- 悬赏已关闭 BountyClosed

## 发现（Discovery）

- 搜索已执行 SearchExecuted
- 通知已发送 NotificationSent
- 相关问题已生成 RelatedQuestionsSuggested

## 主时间线（事件顺序）

注册 → 登录 → 发布问题 → 编辑问题 → 回答提交 → 投票/评论 → 采纳答案 → 声誉变更/徽章授予 → 悬赏开启/发放 → 通知发送/索引更新 → 内容删除

## 边界事件 / 热点（Hot Spot）

- 采纳 → 声誉联动（跨上下文一致性）
- 悬赏到期自动发放（防重复/防错误）
- 发布问题时相似问题推荐（防重复提问）
