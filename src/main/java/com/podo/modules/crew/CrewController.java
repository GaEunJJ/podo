package com.podo.modules.crew;

import com.podo.modules.account.CurrentUser;
import com.podo.modules.crew.validator.CrewFormValidator;
import com.podo.modules.account.Account;
import com.podo.modules.crew.form.CrewForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;
    private final ModelMapper modelMapper;
    private final CrewFormValidator crewFormValidator;
    private final CrewRepository crewRepository;

    @InitBinder("crewForm")
    public void crewFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(crewFormValidator);
    }

    @GetMapping("/new-crew")
    public String newCrewForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new CrewForm());
        return "crew/form";
    }

    @PostMapping("/new-crew")
    public String newCrewSubmit(@CurrentUser Account account, @Valid CrewForm crewForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "crew/form";
        }
        Crew newCrew = crewService.createNewCrew(modelMapper.map(crewForm, Crew.class), account);
        return "redirect:/crew/" + URLEncoder.encode(newCrew.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/crew/{path}")
    public String viewCrew(@CurrentUser Account account, @PathVariable String path, Model model){
        Crew crew = crewService.getCrew(path);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/view";
    }

    @GetMapping("/crew/{path}/members")
    public String viewCrewMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrew(path);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/members";
    }

    @GetMapping("/crew/{path}/join")
    public String joinCrew(@CurrentUser Account account, @PathVariable String path) {
        Crew crew = crewRepository.findCrewWithMembersByPath(path);
        crewService.addMember(crew, account);
        return "redirect:/crew/" + crew.getEncodedPath() + "/members";
    }

    @GetMapping("/crew/{path}/leave")
    public String leaveCrew(@CurrentUser Account account, @PathVariable String path) {
        Crew crew = crewRepository.findCrewWithMembersByPath(path);
        crewService.removeMember(crew, account);
        return "redirect:/crew/" + crew.getEncodedPath() + "/members";
    }


}
