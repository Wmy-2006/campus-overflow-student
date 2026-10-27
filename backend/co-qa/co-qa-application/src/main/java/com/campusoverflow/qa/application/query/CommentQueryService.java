package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.QaReadModel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 评论查询用例（读路径）：按问题聚合全部评论（含二级回复）。 */
@Service
@Transactional(readOnly = true)
public class CommentQueryService {

    private final QaReadModel readModel;
    private final IdentityApi identity;

    public CommentQueryService(QaReadModel readModel, IdentityApi identity) {
        this.readModel = readModel;
        this.identity = identity;
    }

    public List<CommentView> list(long questionId) {
        List<CommentRow> rows = readModel.findComments(questionId);
        Set<Long> authorIds = new HashSet<>();
        rows.forEach(r -> authorIds.add(r.authorId()));
        Map<Long, UserSummary> users = identity.findUsers(authorIds);
        return rows.stream()
                .map(r -> new CommentView(r.id(), AuthorView.of(r.authorId(), users.get(r.authorId())),
                        r.parentId(), r.body(), r.createdAt()))
                .toList();
    }
}
