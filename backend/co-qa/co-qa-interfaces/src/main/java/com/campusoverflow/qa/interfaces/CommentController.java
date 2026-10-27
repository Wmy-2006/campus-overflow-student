package com.campusoverflow.qa.interfaces;

import com.campusoverflow.qa.application.command.CommentService;
import com.campusoverflow.qa.application.command.PostCommentCommand;
import com.campusoverflow.qa.application.query.CommentQueryService;
import com.campusoverflow.qa.application.query.CommentView;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.shared.security.Actor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论 Comment")
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService comments;
    private final CommentQueryService query;

    public CommentController(CommentService comments, CommentQueryService query) {
        this.comments = comments;
        this.query = query;
    }

    @Operation(summary = "发表评论或二级回复（支持 @昵称 提醒，Markdown 正文）")
    @PostMapping
    public ResponseEntity<Map<String, Long>> post(@Parameter(hidden = true) Actor actor,
                                                  @Valid @RequestBody CommentRequest req) {
        long id = comments.post(actor, new PostCommentCommand(req.targetType(), req.targetId(), req.parentId(),
                req.body()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "按问题查询评论列表（含二级回复）")
    @GetMapping
    public List<CommentView> list(@Parameter(description = "问题 ID") @RequestParam long questionId) {
        return query.list(questionId);
    }

    public record CommentRequest(@NotNull TargetType targetType, @NotNull Long targetId, Long parentId,
                                 @NotBlank @Size(max = 600) String body) {
    }
}
