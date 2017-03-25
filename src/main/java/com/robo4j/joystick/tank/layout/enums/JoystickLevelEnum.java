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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @author Choustoulakis Nikolaos (@eppnikos)
 */
public enum JoystickLevelEnum {

	// @formatter:off
	NONE        (0),
    LEVEL_1     (1),
    LEVEL_2     (2),
    LEVEL_3     (3),
    ;
	private volatile static Map<Integer, JoystickLevelEnum> internalMap;
	// @formatter:on
	private int level;

	JoystickLevelEnum(int l) {
		this.level = l;
	}

	public static JoystickLevelEnum getJoystickLevelByCode(int code) {
		if (internalMap == null) {
			internalMap = initMapping();
		}

		return internalMap.get(code);
	}

	// Private Methods
	private static Map<Integer, JoystickLevelEnum> initMapping() {
		return Arrays.asList(values()).stream().map(e -> new Map.Entry<Integer, JoystickLevelEnum>() {
			@Override
			public Integer getKey() {
				return e.getLevel();
			}

			@Override
			public JoystickLevelEnum getValue() {
				return e;
			}

			@Override
			public JoystickLevelEnum setValue(JoystickLevelEnum value) {
				return null;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return "JoystickLevelEnum{" + "level=" + level + '}';
	}

}
