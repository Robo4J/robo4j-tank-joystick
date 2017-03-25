/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.joystick.tank.layout.enums;

import com.robo4j.joystick.tank.layout.event.JoystickEvent;

import javafx.event.EventType;
import javafx.scene.input.InputEvent;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @author Choustoulakis Nikolaos (@eppnikos)
 */
public enum JoystickEventEnum {

	//@formatter:off
	UNDEFINED           (1, new EventType<>(InputEvent.ANY, "POV")),
    QUADRANT_CHANGED    (2,	new EventType<>(InputEvent.ANY, "POV_CHANGE_QUADRANT")),
    LEVEL_CHANGED       (3,new EventType<>(InputEvent.ANY, "POV_LEVEL_CHANGED")),;
	// @formatter:on
	private final int id;
	private final EventType<JoystickEvent> eventType;

	JoystickEventEnum(int id, EventType<JoystickEvent> eventType) {
		this.id = id;
		this.eventType = eventType;
	}

	public int getId() {
		return id;
	}

	public EventType<JoystickEvent> getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return "JoystickEventEnum{" + "id=" + id + ", eventType=" + eventType + '}';
	}
}
