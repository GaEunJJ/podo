package com.podo.modules.crew;

import com.podo.modules.account.Account;
import com.podo.modules.tag.Tag;
import com.podo.modules.zone.Zone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface CrewRepository extends JpaRepository<Crew, Long>, CrewRepositoryExtension{
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Crew findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Crew findCrewWithTagsByPath(String path);

    Crew findCrewOnlyByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"})
    Crew findCrewWithZonesByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Crew findCrewWithManagersByPath(String path);

    @EntityGraph(attributePaths = "members")
    Crew findCrewWithMembersByPath(String path);

    @EntityGraph(attributePaths = {"zones", "tags"})
    Crew findCrewWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"members", "managers"})
    Crew findCrewWithManagersAndMemebersById(Long id);

    List<Crew> findByAccount(Set<Tag> tags, Set<Zone> zones);

    List<Crew> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Crew> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    @EntityGraph(attributePaths = {"zones", "tags"})
    List<Crew> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

}
