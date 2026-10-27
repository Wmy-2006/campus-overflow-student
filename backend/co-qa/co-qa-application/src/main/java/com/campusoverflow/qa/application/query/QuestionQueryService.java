package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.port.ViewCounter;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 问答查询用例（读路径）。 */
@Service
@Transactional(readOnly = true)
public class QuestionQueryService {

    private static final int EXCERPT_LENGTH = 160;

    private final QaReadModel readModel;
    private final IdentityApi identity;
    private final ContentRenderer renderer;
    private final ViewCounter viewCounter;
    private final QuestionListCache cache;

    public QuestionQueryService(QaReadModel readModel, IdentityApi identity, ContentRenderer renderer,
                                ViewCounter viewCounter, QuestionListCache cache) {
        this.readModel = readModel;
        this.identity = identity;
        this.renderer = renderer;
        this.viewCounter = viewCounter;
        this.cache = cache;
    }

    public PageResult<QuestionSummaryView> list(Long courseId, String tag, PageRequest page) {
        // 按标签筛选时不走缓存（缓存键只含 courseId/page，避免串数据）
        if (tag == null || tag.isBlank()) {
            var cached = cache.get(courseId, page);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        PageResult<QuestionRow> rows = readModel.listQuestions(courseId, tag, page);
        Set<Long> authorIds = new HashSet<>();
        rows.items().forEach(r -> authorIds.add(r.authorId()));
        Map<Long, UserSummary> users = identity.findUsers(authorIds);

        List<QuestionSummaryView> items = rows.items().stream()
                .map(r -> new QuestionSummaryView(
                        r.id(), r.title(),
                        renderer.toPlainText(r.body(), EXCERPT_LENGTH),
                        r.tags(), r.courseId(),
                        AuthorView.of(r.authorId(), users.get(r.authorId())),
                        r.score(), r.answerCount(), r.viewCount(),
                        r.acceptedAnswerId() != null, r.createdAt()))
                .toList();

        PageResult<QuestionSummaryView> result =
                new PageResult<>(items, rows.total(), rows.page(), rows.size());
        if (tag == null || tag.isBlank()) {
            cache.put(courseId, page, result);
        }
        return result;
    }

    public QuestionDetailView detail(long questionId, Actor actorOrNull) {
        QuestionRow question = readModel.findQuestion(questionId)
                .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在或已删除"));
        viewCounter.increment(questionId);

        List<AnswerRow> answerRows = readModel.findAnswers(questionId);
        List<CommentRow> commentRows = readModel.findComments(questionId);

        Long uid = actorOrNull == null ? null : actorOrNull.userId();
        List<Long> answerIds = answerRows.stream().map(AnswerRow::id).toList();
        Map<String, Integer> myVotes = uid == null
                ? Map.of()
                : readModel.findMyVotes(uid, questionId, answerIds);

        Set<Long> userIds = new HashSet<>();
        userIds.add(question.authorId());
        answerRows.forEach(a -> {
            userIds.add(a.authorId());
            if (a.endorsedBy() != null) {
                userIds.add(a.endorsedBy());
            }
        });
        commentRows.forEach(c -> userIds.add(c.authorId()));
        Map<Long, UserSummary> users = identity.findUsers(userIds);

        Map<String, List<CommentView>> commentsByTarget = new HashMap<>();
        for (CommentRow c : commentRows) {
            commentsByTarget
                    .computeIfAbsent(c.targetType() + ":" + c.targetId(), k -> new ArrayList<>())
                    .add(new CommentView(c.id(), AuthorView.of(c.authorId(), users.get(c.authorId())),
                            c.parentId(), c.body(), c.createdAt()));
        }
        commentsByTarget.values().forEach(list -> list.sort(Comparator.comparing(CommentView::createdAt)));

        boolean canManage = uid != null && identity.canManageCourse(uid, question.courseId());
        boolean isAuthor = uid != null && uid == question.authorId();
        PermissionsView permissions = uid == null
                ? PermissionsView.NONE
                : new PermissionsView(isAuthor || canManage, isAuthor || canManage,
                        (isAuthor || canManage) && question.acceptedAnswerId() == null, canManage);

        CourseRefView course = null;
        if (question.courseId() != null) {
            course = identity.findCourse(question.courseId())
                    .map(c -> new CourseRefView(c.id(), c.code(), c.name()))
                    .orElse(null);
        }

        Long acceptedId = question.acceptedAnswerId();
        List<AnswerView> answers = answerRows.stream()
                .sorted(Comparator
                        .comparing((AnswerRow a) -> acceptedId != null && acceptedId == a.id()).reversed()
                        .thenComparing(AnswerRow::score, Comparator.reverseOrder())
                        .thenComparing(AnswerRow::createdAt))
                .map(a -> new AnswerView(
                        a.id(), a.body(), renderer.toSafeHtml(a.body()),
                        AuthorView.of(a.authorId(), users.get(a.authorId())),
                        a.score(), acceptedId != null && acceptedId == a.id(),
                        a.endorsedBy() == null ? null : AuthorView.of(a.endorsedBy(), users.get(a.endorsedBy())),
                        myVotes.getOrDefault("ANSWER:" + a.id(), 0),
                        uid != null && (uid == a.authorId() || canManage),
                        a.createdAt(), a.updatedAt(),
                        commentsByTarget.getOrDefault("ANSWER:" + a.id(), List.of())))
                .toList();

        return new QuestionDetailView(
                question.id(), question.title(), question.body(),
                renderer.toSafeHtml(question.body()), question.tags(), course,
                AuthorView.of(question.authorId(), users.get(question.authorId())),
                question.score(), question.answerCount(), question.viewCount(),
                acceptedId, myVotes.getOrDefault("QUESTION:" + questionId, 0),
                question.createdAt(), question.updatedAt(), permissions,
                commentsByTarget.getOrDefault("QUESTION:" + questionId, List.of()), answers);
    }

    public List<TagView> popularTags(int limit) {
        return readModel.popularTags(limit);
    }
}
