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

package com.robo4j.joystick.tank.controller;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.httpunit.Constants;
import com.robo4j.joystick.tank.codec.LegoButtonPlateCodec;
import com.robo4j.joystick.tank.layout.enums.JoystickCommandEnum;

import sun.net.util.IPAddressUtil;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LegoPlatformController extends RoboUnit<JoystickCommandEnum> {

	private final LegoButtonPlateCodec codec = new LegoButtonPlateCodec();
	private String target;
	private String targetOut;
	private String client;
	private String clientUri;

	public LegoPlatformController(RoboContext context, String id) {
		super(JoystickCommandEnum.class, context, id);
	}

	@Override
	public void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString("target", null);
		targetOut = configuration.getString("targetOut", null);
		String tmpClient = configuration.getString("client", null);

		if (target == null || tmpClient == null || targetOut == null) {
			throw ConfigurationException.createMissingConfigNameException("target, client");
		}

		if (IPAddressUtil.isIPv4LiteralAddress(tmpClient)) {
			String clientPort = configuration.getString("clientPort", null);
			client = clientPort == null ? tmpClient : tmpClient.concat(":").concat(clientPort);
			clientUri = configuration.getString("clientUri", Constants.EMPTY_STRING);
		} else {
			client = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(JoystickCommandEnum message) {
		processJoystickMessage(message);
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		setState(LifecycleState.STOPPED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		setState(LifecycleState.SHUTDOWN);
	}

	// Private Methods
	private void sendClientMessage(RoboContext ctx, String message) {
		ctx.getReference(targetOut).sendMessage(message);
	}

	private void processJoystickMessage(JoystickCommandEnum message) {
		sendClientMessage(getContext(),
				RoboHttpUtils.createPostRequest(client, clientUri, codec.encode(message.getName())));
	}

}
