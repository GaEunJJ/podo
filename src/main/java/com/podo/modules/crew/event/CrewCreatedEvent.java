package com.podo.modules.crew.event;

import com.podo.modules.crew.Crew;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CrewCreatedEvent{
    private final Crew crew;
}
