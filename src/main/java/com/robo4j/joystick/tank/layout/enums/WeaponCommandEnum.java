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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum WeaponCommandEnum {

	//@formatter:off
    STOP        (0, "stop"),
    FORWARD     (1, "forward"),
    BACKWARD    (2, "backward"),
    ;

    //@formatter:on

	private static volatile Map<Integer, WeaponCommandEnum> internMapByType;
	private Integer type;
	private String name;

	WeaponCommandEnum(int type, String name) {
		this.type = type;
		this.name = name;
	}

	//@formatter:off
    private static Map<Integer, WeaponCommandEnum> initMapping() {
        return Stream.of(values())
                .collect(Collectors.toMap(WeaponCommandEnum::getType, e -> e));
    }

    public static WeaponCommandEnum getByName(String name) {
        if (internMapByType == null)
            internMapByType = initMapping();
        return internMapByType.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
    //@formatter:on

	public Integer getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Set<String> commandNames() {
		//@formatter:off
        return Stream.of(values())
                .map(WeaponCommandEnum::getName)
                .collect(Collectors.toSet());
        //@formatter:on
	}

	@Override
	public String toString() {
		return "WeaponCommandEnum{" + "type=" + type + ", name='" + name + '\'' + '}';
	}
}
