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

package com.robo4j.joystick.tank.layout.event;

import com.robo4j.joystick.tank.layout.enums.JoystickEventEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickLevelEnum;
import com.robo4j.joystick.tank.layout.enums.QuadrantEnum;

import javafx.event.EventTarget;
import javafx.scene.input.InputEvent;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @author Choustoulakis Nikolaos (@eppnikos)
 */
public class JoystickEvent extends InputEvent {
	private static final long serialVersionUID = 2231266847422883646L;

	private transient double x;
	private transient double y;
	private transient QuadrantEnum quadrant;
	private transient JoystickLevelEnum joystickLevel;

	public JoystickEvent(Object source, EventTarget target, JoystickEventEnum eventType, double x, double y,
			QuadrantEnum quadrant, JoystickLevelEnum joystickLevel) {
		super(source, target, eventType.getEventType());
		this.x = x;
		this.y = y;
		this.quadrant = quadrant;
		this.joystickLevel = joystickLevel;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}

	public final QuadrantEnum getQuadrant() {
		return quadrant;
	}

	public final JoystickLevelEnum getJoystickLevel() {
		return joystickLevel;
	}

	@Override
	public String toString() {
		return "JoystickEvent{" + "x=" + x + ", y=" + y + ", quadrant=" + quadrant + ", joystickLevel=" + joystickLevel
				+ '}';
	}
}
