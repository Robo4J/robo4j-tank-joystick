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

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @author Choustoulakis Nikolaos (@eppnikos)
 */
public enum QuadrantEnum {
	//@formatter:off
	// name id, start angle, end angle
	NONE            (0, 0, 0),
    QUADRANT_I      (1, 315, 90),
    QUADRANT_II     (2, 45, 90),
    QUADRANT_III    (3, 135, 90),
    QUADRANT_IV     (4, 225, 90),
    ;
	//@formatter:on

	private int quadrant;
	private int startAngle;
	private int angleExtend;

	private QuadrantEnum(int q, int startAngle, int angleExtend) {
		quadrant = q;
		this.startAngle = startAngle;
		this.angleExtend = angleExtend;
	}

	public int getQuadrant() {
		return quadrant;
	}

	public int getAngleExtend() {
		return angleExtend;
	}

	public int getStartAngle() {
		return startAngle;
	}

	@Override
	public String toString() {
		return "QuadrantEnum{" + "quadrant=" + quadrant + ", startAngle=" + startAngle + ", angleExtend=" + angleExtend
				+ '}';
	}
}
