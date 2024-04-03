package com.luckraw.passin.services;

import com.luckraw.passin.domain.attendee.Attendee;
import com.luckraw.passin.domain.event.Event;
import com.luckraw.passin.dto.event.EventIdDTO;
import com.luckraw.passin.dto.event.EventRequestDTO;
import com.luckraw.passin.dto.event.EventResponseDTO;
import com.luckraw.passin.repositories.AttendeeRepository;
import com.luckraw.passin.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;

    public EventResponseDTO getEventDetail(String eventId) {
        Event event = this.eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found with ID:" + eventId));
        List<Attendee> attendeeList = this.attendeeRepository.findByEventId(eventId);
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

    private String createSlug(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCOMBINING_DIACRITICAL_MARKS}]", "")
                .replaceAll("[^\\w\\s]", "")
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }
}
