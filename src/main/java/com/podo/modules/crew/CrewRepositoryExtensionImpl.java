package com.podo.modules.crew;

import com.podo.modules.crew.QCrew;
import com.podo.modules.tag.QTag;
import com.podo.modules.tag.Tag;
import com.podo.modules.zone.QZone;
import com.podo.modules.zone.Zone;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

class CrewRepositoryExtensionImpl extends QuerydslRepositorySupport implements CrewRepositoryExtension {

    public CrewRepositoryExtensionImpl() {
        super(Crew.class);
    }

    @Override
    public Page<Crew> findByKeyword(String keyword, Pageable pageable) {
        QCrew crew = QCrew.crew;
        JPQLQuery<Crew> query = from(crew).where(crew.published.isTrue()
                        .and(crew.title.containsIgnoreCase(keyword))
                        .or(crew.tags.any().title.containsIgnoreCase(keyword))
                        .or(crew.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(crew.tags, QTag.tag).fetchJoin()
                .leftJoin(crew.zones, QZone.zone).fetchJoin()
                // 중복데이터 해결
                .distinct();
        JPQLQuery<Crew> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Crew> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<Crew> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QCrew crew = QCrew.crew;
        JPQLQuery<Crew> query = from(crew).where(crew.published.isTrue()
                        .and(crew.closed.isFalse())
                        .and(crew.tags.any().in(tags))
                        .and(crew.zones.any().in(zones)))
                .leftJoin(crew.tags, QTag.tag).fetchJoin()
                .leftJoin(crew.zones, QZone.zone).fetchJoin()
                .orderBy(crew.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
