package com.campusoverflow.qa.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.CommentRepository;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 评论用例：发表/二级回复/发事件。一层限制的领域规则见 VoteAndCommentTest。 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final Actor hong = new Actor(2, "小红", Role.STUDENT);

    @Mock private CommentRepository comments;
    @Mock private QuestionRepository questions;
    @Mock private AnswerRepository answers;
    @Mock private IntegrationEventPublisher events;

    private CommentService service() {
        return new CommentService(comments, questions, answers, events, clock);
    }

    @Test
    void postOnQuestionSavesAndPublishesEvent() {
        when(questions.findById(10L)).thenReturn(Optional.of(anyQuestion()));
        when(comments.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.assignId(100L);
            return c;
        });

        long id = service().post(hong, new PostCommentCommand(TargetType.QUESTION, 10L, null, "请问为什么要变色？"));
        assertThat(id).isEqualTo(100L);

        ArgumentCaptor<CommentPostedEvent> captor = ArgumentCaptor.forClass(CommentPostedEvent.class);
        verify(events).publish(captor.capture());
        assertThat(captor.getValue().questionId()).isEqualTo(10L);
        assertThat(captor.getValue().authorId()).isEqualTo(2L);
    }

    @Test
    void replyOnAnswerResolvesQuestionFromAnswer() {
        when(answers.findById(50L)).thenReturn(Optional.of(anyAnswer()));
        when(comments.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.assignId(101L);
            return c;
        });

        service().post(hong, new PostCommentCommand(TargetType.ANSWER, 50L, null, "二级评论测试"));

        verify(events).publish(any(CommentPostedEvent.class));
    }

    @Test
    void missingParentRejected() {
        when(answers.findById(50L)).thenReturn(Optional.of(anyAnswer()));
        when(comments.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().post(hong,
                new PostCommentCommand(TargetType.ANSWER, 50L, 999L, "回复不存在的评论")))
                .isInstanceOf(NotFoundException.class);
    }

    private Question anyQuestion() {
        return Question.post(1L, 100L, new Title("红黑树为什么必须旋转？"),
                MarkdownBody.ofQuestion("正文"), Tags.of(java.util.List.of("数据结构")), NOW);
    }

    private Answer anyAnswer() {
        return Answer.reconstitute(50L, 10L, 2L, MarkdownBody.ofAnswer("回答正文"), 0, null, NOW, NOW, null, 0L);
    }
}
