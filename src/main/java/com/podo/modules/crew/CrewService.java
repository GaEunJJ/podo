package com.podo.modules.crew;

import com.podo.modules.account.Account;
import com.podo.modules.crew.event.CrewCreatedEvent;
import com.podo.modules.crew.event.CrewUpdateEvent;
import com.podo.modules.crew.form.CrewDescriptionForm;
import com.podo.modules.tag.Tag;
import com.podo.modules.tag.TagRepository;
import com.podo.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static com.podo.modules.crew.form.CrewForm.VALID_PATH_PATTERN;

@Service
@Transactional
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository repository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Crew createNewCrew(Crew crew, Account account) {
        Crew newCrew = repository.save(crew);
        newCrew.addManager(account);
        eventPublisher.publishEvent(new CrewCreatedEvent(newCrew));
        return newCrew;
    }

    public Crew getCrew(String path) {
        Crew crew = this.repository.findByPath(path);
        if(crew == null){
            throw new IllegalArgumentException(path + "에 해당하는 모임이 없습니다.");
        }
        return crew;
    }

    private void checkIfManager(Account account, Crew crew) {
        if (!crew.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public Crew getCrewToUpdate(Account account, String path) {
        Crew crew = this.getCrew(path);
        checkIfManager(account, crew);
        return crew;
    }

    public void updateCrewDescription(Crew crew, CrewDescriptionForm crewDescriptionForm) {
        modelMapper.map(crewDescriptionForm, crew);
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "모임 소개를 수정했습니다."));
    }

    public void updateCrewImage(Crew crew, String image) {
        crew.setImage(image);
    }

    public void enableCrewBanner(Crew crew) {
        crew.setUseBanner(true);
    }

    public void disableCrewBanner(Crew crew) {
        crew.setUseBanner(false);
    }

    public Crew getCrewToUpdateTag(Account account, String path) {
        Crew crew = repository.findCrewWithTagsByPath(path);
        checkIfExistingCrew(path, crew);
        checkIfManager(account, crew);
        return crew;
    }

    private void checkIfExistingCrew(String path, Crew crew) {
        if (crew == null) {
            throw new IllegalArgumentException(path + "에 해당하는 모임이 없습니다.");
        }
    }

    public void publish(Crew crew) {
        crew.publish();
        this.eventPublisher.publishEvent(new CrewCreatedEvent(crew));
    }

    public void close(Crew crew) {
        crew.close();
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "모임을 종료했습니다."));
    }

    public void startRecruit(Crew crew) {
        crew.startRecruit();
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "팀원 모집을 시작합니다."));
    }

    public void stopRecruit(Crew crew) {
        crew.stopRecruit();
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "팀원 모집을 중단했습니다."));
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }

        return !repository.existsByPath(newPath);
    }

    public void updateCrewPath(Crew crew, String newPath) {
        crew.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateCrewTitle(Crew crew, String newTitle) {
        crew.setTitle(newTitle);
    }

    public void remove(Crew crew) {
        if (crew.isRemovable()) {
            repository.delete(crew);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Crew crew, Account account) {
        crew.addMember(account);
    }

    public void removeMember(Crew crew, Account account) {
        crew.removeMember(account);
    }

    public Crew getCrewToEnroll(String path) {
        Crew crew = repository.findCrewOnlyByPath(path);
        checkIfExistingCrew(path, crew);
        return crew;
    }


    public void addTag(Crew crew, Tag tag) {
        crew.getTags().add(tag);
    }

    public void removeTag(Crew crew, Tag tag) {
        crew.getTags().remove(tag);
    }

    public void addZone(Crew crew, Zone zone) {
        crew.getZones().add(zone);
    }

    public void removeZone(Crew crew, Zone zone) {
        crew.getZones().remove(zone);
    }

    public Crew getCrewToUpdateZone(Account account, String path) {
        Crew crew = repository.findCrewWithZonesByPath(path);
        checkIfExistingCrew(path, crew);
        checkIfManager(account, crew);
        return crew;
    }

    public Crew getCrewToUpdateStatus(Account account, String path) {
        Crew crew = repository.findCrewWithManagersByPath(path);
        checkIfExistingCrew(path, crew);
        checkIfManager(account, crew);
        return crew;
    }
}
