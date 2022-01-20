package com.podo.modules.crew;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.podo.modules.account.CurrentUser;
import com.podo.modules.crew.form.CrewDescriptionForm;
import com.podo.modules.crew.CrewRepository;
import com.podo.modules.crew.CrewService;
import com.podo.modules.account.Account;
import com.podo.modules.crew.Crew;
import com.podo.modules.tag.Tag;
import com.podo.modules.zone.Zone;
import com.podo.modules.tag.TagForm;
import com.podo.modules.zone.ZoneForm;
import com.podo.modules.tag.TagRepository;
import com.podo.modules.tag.TagService;
import com.podo.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/crew/{path}/settings")
@RequiredArgsConstructor
public class CrewSettingsController {

    private final CrewRepository crewRepository;
    private final CrewService crewService;
    private final TagService tagService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/description")
    public String viewCrewSetting(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(crew);
        model.addAttribute(modelMapper.map(crew, CrewDescriptionForm.class));
        return "crew/settings/description";
    }

    @PostMapping("/description")
    public String updateCrewInfo(@CurrentUser Account account, @PathVariable String path,
                                 @Valid CrewDescriptionForm crewDescriptionForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(crew);
            return "crew/settings/description";
        }

        crewService.updateCrewDescription(crew, crewDescriptionForm);
        attributes.addFlashAttribute("message", "모임 소개를 수정했습니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/description";
    }

    @GetMapping("/banner")
    public String crewImageForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/settings/banner";
    }

    @PostMapping("/banner")
    public String crewImageSubmit(@CurrentUser Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        crewService.updateCrewImage(crew, image);
        attributes.addFlashAttribute("message", "모임 이미지를 수정했습니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableCrewBanner(@CurrentUser Account account, @PathVariable String path) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        crewService.enableCrewBanner(crew);
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableCrewBanner(@CurrentUser Account account, @PathVariable String path) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        crewService.disableCrewBanner(crew);
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/banner";
    }

    @GetMapping("/tags")
    public String crewTagsForm(@CurrentUser Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Crew crew = crewService.getCrewToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(crew);

        model.addAttribute("tags", crew.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitles = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "crew/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path,
                                 @RequestBody TagForm tagForm) {
        Crew crew = crewService.getCrewToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        crewService.addTag(crew, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm) {
        Crew crew = crewService.getCrewToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        crewService.removeTag(crew, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String crewZonesForm(@CurrentUser Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Crew crew = crewService.getCrewToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(crew);
        model.addAttribute("zones", crew.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return "crew/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Crew crew = crewService.getCrewToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        crewService.addZone(crew, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm) {
        Crew crew = crewService.getCrewToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        crewService.removeZone(crew, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/crew")
    public String crewSettingForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/settings/crew";
    }

    @PostMapping("/crew/publish")
    public String publishCrew(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        crewService.publish(crew);
        attributes.addFlashAttribute("message", "모임을 공개했습니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
    }

    @PostMapping("/crew/close")
    public String closeCrew(@CurrentUser Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        crewService.close(crew);
        attributes.addFlashAttribute("message", "모임을 종료했습니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path, Model model,
                               RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        if (!crew.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
        }

        crewService.startRecruit(crew);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        if (!crew.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
        }

        crewService.stopRecruit(crew);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
    }

    @PostMapping("/crew/path")
    public String updateCrewPath(@CurrentUser Account account, @PathVariable String path, String newPath,
                                  Model model, RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        if (!crewService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(crew);
            model.addAttribute("studyPathError", "해당 모임 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "crew/settings/crew";
        }

        crewService.updateCrewPath(crew, newPath);
        attributes.addFlashAttribute("message", "모임 경로를 수정했습니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
    }

    @PostMapping("/crew/title")
    public String updateCrewTitle(@CurrentUser Account account, @PathVariable String path, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        if (!crewService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(crew);
            model.addAttribute("crewTitleError", "모임 이름을 다시 입력하세요.");
            return "crew/settings/crew";
        }

        crewService.updateCrewTitle(crew, newTitle);
        attributes.addFlashAttribute("message", "모임 이름을 수정했습니다.");
        return "redirect:/crew/" + crew.getEncodedPath() + "/settings/crew";
    }

    @PostMapping("/crew/remove")
    public String removeCrew(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        crewService.remove(crew);
        return "redirect:/";
    }

}
