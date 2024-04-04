package com.luckraw.passin.services;

import com.luckraw.passin.domain.attendee.Attendee;
import com.luckraw.passin.domain.event.Event;
import com.luckraw.passin.domain.event.exceptions.EventFullException;
import com.luckraw.passin.domain.event.exceptions.EventNotFoundException;
import com.luckraw.passin.dto.attendee.AttendeeIdDTO;
import com.luckraw.passin.dto.attendee.AttendeeRequestDTO;
import com.luckraw.passin.dto.event.EventIdDTO;
import com.luckraw.passin.dto.event.EventRequestDTO;
import com.luckraw.passin.dto.event.EventResponseDTO;
import com.luckraw.passin.repositories.AttendeeRepository;
import com.luckraw.passin.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AttendeeService attendeeService;

    public EventResponseDTO getEventDetail(String eventId) {
        Event event = this.getEventById(eventId);
        List<Attendee> attendeeList = this.attendeeService.getAllAttendeesFromEvent(eventId);
        return new EventResponseDTO(event, attendeeList.size());
    }

    public EventIdDTO createEvent(EventRequestDTO requestDTO) {
        Event event = new Event();
        event.setTitle(requestDTO.title());
        event.setDetails(requestDTO.details());
        event.setMaximumAttendees(requestDTO.maximumAttendees());
        event.setSlug(this.createSlug(requestDTO.title()));

        this.eventRepository.save(event);

        return new EventIdDTO(event.getId());
    }

    public AttendeeIdDTO registerAttendeeOnEvent(String eventId, AttendeeRequestDTO attendeeRequestDTO) {
        this.attendeeService.verifyAttendeeSubscription(attendeeRequestDTO.email(), eventId);

        Event event = this.getEventById(eventId);
        List<Attendee> attendeeList = this.attendeeService.getAllAttendeesFromEvent(eventId);

        if (event.getMaximumAttendees() <= attendeeList.size()) throw new EventFullException("Event is full!");

        Attendee newAttendee = new Attendee();
        newAttendee.setName(attendeeRequestDTO.name());
        newAttendee.setName(attendeeRequestDTO.email());
        newAttendee.setEvent(event);
        newAttendee.setCreatedAt(LocalDateTime.now());
        this.attendeeService.registerAttendee(newAttendee);

        return new AttendeeIdDTO(newAttendee.getId());
    }

    private Event getEventById(String eventId) {
       return this.eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event not found with ID:" + eventId));

    }

    private String createSlug(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCOMBINING_DIACRITICAL_MARKS}]", "")
                .replaceAll("[^\\w\\s]", "")
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }
}
