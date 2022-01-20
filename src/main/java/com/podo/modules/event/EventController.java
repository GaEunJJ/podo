package com.podo.modules.event;

import com.podo.modules.account.CurrentUser;
import com.podo.modules.crew.CrewRepository;
import com.podo.modules.crew.CrewService;
import com.podo.modules.account.Account;
import com.podo.modules.crew.Crew;
import com.podo.modules.event.form.EventForm;
import com.podo.modules.event.validator.EventValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/crew/{path}")
@RequiredArgsConstructor
public class EventController {

    private final CrewService crewService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final CrewRepository crewRepository;
    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        model.addAttribute(crew);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(crew);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), crew, account);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event,
                           Model model) {
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(crewRepository.findCrewWithManagersByPath(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewCrewEvents(@CurrentUser Account account, @PathVariable String path, Model model) {
        Crew crew = crewService.getCrew(path);
        model.addAttribute(account);
        model.addAttribute(crew);

        List<Event> events = eventRepository.findByCrewOrderByStartDateTime(crew);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "crew/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account,
                                  @PathVariable String path, @PathVariable("id") Event event, Model model) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        model.addAttribute(crew);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable("id") Event event, @Valid EventForm eventForm, Errors errors,
                                    Model model) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(crew);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/crew/" + crew.getEncodedPath() +  "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Crew crew = crewService.getCrewToUpdateStatus(account, path);
        eventService.deleteEvent(event);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account,
                                @PathVariable String path, @PathVariable("id") Event event) {
        Crew crew = crewService.getCrewToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account,
                                   @PathVariable String path, @PathVariable("id") Event event) {
        Crew crew = crewService.getCrewToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentUser Account account, @PathVariable String path,
                                          @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Crew crew = crewService.getCrewToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/crew/" + crew.getEncodedPath() + "/events/" + event.getId();
    }
}
