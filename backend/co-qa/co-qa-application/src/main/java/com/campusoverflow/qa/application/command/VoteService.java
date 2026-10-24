package com.campusoverflow.qa.application.command;

import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Vote;
import com.campusoverflow.qa.domain.VoteRepository;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用例：投票 / 改投 / 撤销（value ∈ {-1, 0, 1}，0 表示撤销）。
 * 发布的事件同时携带旧值与新值，下游据此计算声誉增量。
 */
@Service
public class VoteService {

    private final VoteRepository votes;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public VoteService(VoteRepository votes, QuestionRepository questions, AnswerRepository answers,
                       IntegrationEventPublisher events, Clock clock) {
        this.votes = votes;
        this.questions = questions;
        this.answers = answers;
        this.events = events;
        this.clock = clock;
    }

    public record VoteResult(int myVote, int scoreDelta) {
    }

    private record Target(long authorId, long questionId) {
    }

    @Transactional
    public VoteResult cast(Actor actor, TargetType type, long targetId, int value) {
        if (value != -1 && value != 0 && value != 1) {
            throw new BusinessRuleException("QA-1014", "投票值只能是 -1 / 0 / 1");
        }
        Instant now = clock.instant();
        Target target = resolve(type, targetId);
        Optional<Vote> existing = votes.find(actor.userId(), type, targetId);
        int oldValue = existing.map(Vote::value).orElse(0);

        // 幂等：目标值与当前值相同，不产生任何变化。
        if (value == oldValue) {
            return new VoteResult(oldValue, 0);
        }

        int newValue;
        if (existing.isEmpty()) {
            // value == 0 且本无投票：无操作。
            if (value == 0) {
                return new VoteResult(0, 0);
            }
            // 领域工厂负责“不能给自己投票”与取值合法性。
            Vote vote = Vote.cast(actor.userId(), target.authorId(), type, targetId, value, now);
            votes.save(vote);
            newValue = value;
        } else {
            Vote vote = existing.get();
            if (value == 0) {
                votes.delete(vote);
            } else {
                vote.change(value, now);
                votes.save(vote);
            }
            newValue = value;
        }

        int delta = newValue - oldValue;
        if (type == TargetType.QUESTION) {
            questions.adjustScore(targetId, delta);
        } else {
            answers.adjustScore(targetId, delta);
        }
        events.publish(new VoteCastEvent(EventIds.next(), now, type.name(), targetId, target.authorId(),
                actor.userId(), target.questionId(), oldValue, newValue));
        return new VoteResult(newValue, delta);
    }

    private Target resolve(TargetType type, long targetId) {
        if (type == TargetType.QUESTION) {
            Question question = questions.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在或已删除"));
            return new Target(question.authorId(), targetId);
        }
        Answer answer = answers.findById(targetId)
                .orElseThrow(() -> new NotFoundException("QA-4002", "回答不存在或已删除"));
        if (answer.isDeleted()) {
            throw new NotFoundException("QA-4002", "回答不存在或已删除");
        }
        return new Target(answer.authorId(), answer.questionId());
    }
}
