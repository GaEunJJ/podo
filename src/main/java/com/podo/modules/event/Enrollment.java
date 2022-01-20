package com.podo.modules.event;

import com.podo.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

// 누가 언제 신청했는데 승낙되었는지 안되었는지.

@NamedEntityGraph(
        name = "Enrollment.withEventAndCrew",
        attributeNodes = {
                @NamedAttributeNode(value = "event", subgraph = "crew")
        },
        subgraphs = @NamedSubgraph(name = "crew", attributeNodes = @NamedAttributeNode("crew"))
)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    // 누가 신청
    @ManyToOne
    private Account account;

    // 참가 신청 일시 (선착순의 경우 중요)
    private LocalDateTime enrolledAt;

    // 참가 상태 (확정/미확정)
    private boolean accepted;

    // 실제로 참석했는지
    private boolean attended;

}
