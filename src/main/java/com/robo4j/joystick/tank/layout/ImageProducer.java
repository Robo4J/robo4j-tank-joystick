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

package com.robo4j.joystick.tank.layout;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ImageProducer implements Runnable {
	private static final String DEFAULT_COMMAND_OPT = "quality=100&brightness=80&contras=80";
	private static final String METHOD_GET = "GET";

	private final String link;
	private volatile ImageView imageView;
	private AtomicReference<String> command;

	public ImageProducer(final String link, ImageView imageView, final AtomicReference<String> command) {
		this.link = link;
		this.imageView = imageView;
		this.command = command;
		System.out.println("ImageProducer Started...");
	}

	@Override
	public void run() {
		try {
			System.out.println("Image Producer exchangedOptions = " + command.get());
			String tmpLink = command.get().isEmpty() || command.get().equals(DEFAULT_COMMAND_OPT) ? link
					: link.concat("&").concat(command.get());
			URL url = new URL(tmpLink);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(METHOD_GET);
			try (final BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
					final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

				int imageCh;
				while ((imageCh = in.read()) != -1) {
					baos.write(imageCh);
				}
				conn.disconnect();
				imageView.setImage(new Image(new ByteArrayInputStream(baos.toByteArray())));
			}
			System.out.println("Image Producer Done");
		} catch (IOException e) {
			throw new JoystickException("producer issue e:", e);
		}

	}
}
