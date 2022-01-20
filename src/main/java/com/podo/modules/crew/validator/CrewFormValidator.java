package com.podo.modules.crew.validator;

import com.podo.modules.crew.CrewRepository;
import com.podo.modules.crew.form.CrewForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CrewFormValidator implements Validator {

    private final CrewRepository crewRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CrewForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CrewForm crewForm = (CrewForm)target;
        if (crewRepository.existsByPath(crewForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "해당 모임 경로값은 사용할 수 없습니다.");
        }
    }
}
