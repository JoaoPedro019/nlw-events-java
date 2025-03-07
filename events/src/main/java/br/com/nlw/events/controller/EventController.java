package br.com.nlw.events.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.nlw.events.model.Event;
import br.com.nlw.events.service.EventService;

@RestController
public class EventController {
	
	@Autowired
	private EventService service;
	
	@GetMapping("/events")
	public List<Event> getAllEvents() {
		return service.getAllEvents();
	}
	
	@GetMapping("/events/{prettyName}")
	public ResponseEntity<Event> getByPrettyName(@PathVariable String prettyName) {
		Event evt = service.getByPrettyName(prettyName);
		if (Objects.nonNull(evt)) {
			return ResponseEntity.ok().body(evt);
		}
		return ResponseEntity.notFound().build();
	}
	
	@PostMapping("/events")
	public Event addNewEvent(@RequestBody Event newEvent) {
		return service.addNewEvent(newEvent);
	}
}
