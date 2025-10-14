package hexlet.code.spring.specification;

import hexlet.code.spring.dto.task.TaskParamsDTO;
import hexlet.code.spring.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(final TaskParamsDTO params) {
        return withTitleCont(params.getTitleCont())
                .and(withAssegneeId(params.getAssigneeId()))
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    private Specification<Task> withTitleCont(final String titleCont) {
        return (root, query, cb) -> titleCont == null
                ? cb.conjunction() : cb.like(root.get("name"), "%" + titleCont + "%");
    }

    private Specification<Task> withAssegneeId(final Long assegneeId) {
        return (root, query, cb) -> assegneeId == null
                ? cb.conjunction() : cb.equal(root.join("assignee").get("id"), assegneeId);
    }

    private Specification<Task> withStatus(final String status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction() : cb.equal(root.join("taskStatus").get("slug"), status);
    }

    private Specification<Task> withLabelId(final Long labelId) {
        return (root, query, cb) -> labelId == null
                ? cb.conjunction() : cb.equal(root.join("labels").get("id"), labelId);
    }
}
