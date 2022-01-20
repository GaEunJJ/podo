package com.podo.modules.crew;

import com.podo.modules.tag.Tag;
import com.podo.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface CrewRepositoryExtension {

    Page<Crew> findByKeyword(String keyword, Pageable pageable);

    List<Crew> findByAccount(Set<Tag> tags, Set<Zone> zones);

}
