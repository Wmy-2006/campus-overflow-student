# Sprint 3 Backlog

> 团队：CampusOverflow 开发小组（PO：吴梦瑶 / SM：秦慧慧 / Dev：张丰舒、张馨月）
> Sprint 目标：完成「投票、采纳、评论、标签、声誉计算」核心激励闭环，打通「身份→声誉」跨模块领域事件，修复跨模块协作 BUG，达成 Sprint Review 可演示
> 版本日期：2026-10-26
> 前置：Sprint 2（Lab 5 前端工程 + 查询接口）已结案合并进 main

## 承诺故事

| 故事 | 描述 | SP | 状态 |
|------|------|:--:|------|
| US-08 | 给回答点赞/投票（每人一票、+1/-1、不可投自己） | 3 | To Do |
| US-09 | 评论追问（一层评论、Markdown） | 3 | To Do |
| US-12 | 声誉积分 + 徽章（提问/回答/被采纳计分） | 5 | To Do |
| US-06 | 采纳最佳答案端到端贯通（仅提问者/老师、只能一次） | 3 | To Do |
| 标签 | 问题标签体系完善（创建/展示/按标签筛选） | 2 | To Do |

估算 Velocity：单 Sprint 承诺 16 SP。

## 任务拆分与责任人

| 任务 | 说明 | 责任人 |
|------|------|--------|
| T1 | 后端：co-reputation 领域规则实现（ReputationRulebook 等 TODO(S3)） | 张丰舒 |
| T2 | 后端：投票/采纳/评论 → 声誉 领域事件联动（跨模块） | 张丰舒 |
| T3 | 后端：评论 API（一层限制 + Markdown） | 张丰舒 |
| T4 | 后端：投票 API 贯通 + 采纳权限校验贯通 | 张丰舒 |
| T5 | 后端：标签体系（问题标签 CRUD / 筛选） | 张丰舒 |
| T6 | 前端：投票 / 采纳 / 评论交互 UI | 张馨月 |
| T7 | 前端：声誉积分与徽章展示 | 张馨月 |
| T8 | 后端：修复 1-2 个跨模块协作 BUG（沉淀经验） | 张丰舒 |
| T9 | 文档：Sprint Review 演示视频/录屏 | 吴梦瑶 |
| T10 | 文档：Sprint Retrospective 纪要 docs/retro/sprint-3.md | 吴梦瑶 |
| T11 | 文档：声誉模块关键设计 ADR（至少 1 份） | 吴梦瑶 / 张馨月 |
| T12 | 流程：站会 / 看板 / PR 评审协调 | 秦慧慧（SM） |
| T13 | 测试验收：功能验收用例（投票/采纳/评论/声誉） | 张丰舒（兼 QA） |

## 完成定义（DoD）

- 每个 Task 先写单元测试，通过后再合入
- 所有新增后端代码通过 `mvn -pl co-qa/co-qa-domain,co-qa/co-qa-application -am clean test` 及 co-reputation 模块测试
- 前端 `pnpm typecheck` + `pnpm lint` 全绿
- Sprint 3 增量在 main 分支可端到端演示：提问 → 回答 → 投票 → 采纳 → 评论 → 声誉变化
- CI 由红转绿（co-reputation 测试补齐）

## 跨模块协作说明（关键设计点）

- 身份认证模块在「回答被采纳 / 收到投票」等事件发生后，通过**领域事件**驱动**声誉模块**计分
- 声誉规则集中在 co-reputation 领域层（ReputationRulebook），不散落到各模块

## 风险 / 依赖

- CI 当前红色根因即 co-reputation 的 TODO(S3)（ReputationRulebook.voteDelta / initialPoints / applyDailyCap、AcademicTerm.termKey、BadgePolicy.forAcceptedCount、Bounty.open、ReputationAccount.open）——T1 完成后 CI 转绿
- 声誉积分规则需与访谈提炼的激励诉求一致（避免盲目数值化）
