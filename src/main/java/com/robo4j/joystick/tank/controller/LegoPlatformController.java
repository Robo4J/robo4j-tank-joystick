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


import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.joystick.tank.codec.LegoButtonPlateCodec;
import com.robo4j.joystick.tank.layout.enums.JoystickCommandEnum;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.StringConstants;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@BlockingTrait
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
        int clientPort = configuration.getInteger("clientPort", 8025);
        client = tmpClient + ":" + clientPort;
        clientUri = configuration.getString("clientUri", StringConstants.EMPTY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(JoystickCommandEnum message) {
        processJoystickMessage(message);
    }


    // Private Methods
    private void sendClientMessage(RoboContext ctx, String message) {
        ctx.getReference(targetOut).sendMessage(message);
    }

    private void processJoystickMessage(JoystickCommandEnum message) {
        sendClientMessage(getContext(), RoboHttpUtils
                .createRequest(HttpMethod.POST, client, clientUri, codec.encode(message.getName())));
    }

}
