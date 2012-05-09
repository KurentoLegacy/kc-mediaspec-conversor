/*
 * kc-mediaspec-conversor: Media description conversor between various formats
 * Copyright (C) 2012 Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.commons.media.format.conversor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kurento.commons.media.format.exceptions.ArgumentNotSetException;
import com.kurento.commons.mediaspec.Fraction;
import com.kurento.commons.mediaspec.MediaSpec;
import com.kurento.commons.mediaspec.MediaType;
import com.kurento.commons.mediaspec.Payload;
import com.kurento.commons.mediaspec.PayloadRtp;
import com.kurento.commons.mediaspec.SessionSpec;
import com.kurento.commons.mediaspec.Transport;
import com.kurento.commons.mediaspec.TransportRtp;

public class MediaSpec2Thrift {

	public static SessionSpec sessionSpec2thrift(
			com.kurento.commons.media.format.SessionSpec spec) {
		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		for (com.kurento.commons.media.format.MediaSpec media : spec
				.getMediaSpecs()) {
			MediaSpec tMedia = mediaSpec2Thrift(media);
			medias.add(tMedia);
		}

		SessionSpec tspec = new SessionSpec(medias, spec.getId());

		try {
			tspec.setVersion(spec.getVersion());
		} catch (ArgumentNotSetException e) {
		}

		return tspec;
	}

	private static MediaSpec mediaSpec2Thrift(
			com.kurento.commons.media.format.MediaSpec media) {
		List<Payload> payloads = new ArrayList<Payload>();
		for (com.kurento.commons.media.format.Payload payload : media
				.getPayloads()) {
			Payload tPayload = payload2Thirft(payload);
			payloads.add(tPayload);
		}

		Set<MediaType> types = new HashSet<MediaType>();
		for (com.kurento.commons.media.format.enums.MediaType type : media
				.getTypes()) {
			types.add(MediaType.valueOf(type.toString().toUpperCase()));
		}

		Transport transport = transport2Thrift(media.getTransport());
		MediaSpec tmedia = new MediaSpec(payloads, types, transport,
				com.kurento.commons.mediaspec.Direction.valueOf(media.getMode()
						.toString().toUpperCase()));
		return tmedia;
	}

	private static Payload payload2Thirft(
			com.kurento.commons.media.format.Payload payload) {
		Payload tPayload = new Payload();
		try {
			com.kurento.commons.media.format.payload.PayloadRtp rtp = payload
					.getRtp();
			PayloadRtp tRtp = new PayloadRtp(rtp.getId(), rtp.getCodecName(),
					rtp.getClockRate());
			try {
				tRtp.setChannels(rtp.getChannels());
			} catch (ArgumentNotSetException e) {
			}

			try {
				tRtp.setWidth(rtp.getWidth());
			} catch (ArgumentNotSetException e) {
			}

			try {
				tRtp.setHeight(rtp.getHeight());
			} catch (ArgumentNotSetException e) {
			}

			try {
				tRtp.setBitrate(rtp.getBitrate());
			} catch (ArgumentNotSetException e) {
			}

			try {
				tRtp.setFramerate(fraction2Thrift(rtp.getFramerate()));
			} catch (ArgumentNotSetException e) {
			}

			Set<String> keys = rtp.getParametersKeys();
			HashMap<String, String> map = new HashMap<String, String>();

			for (String key : keys) {
				map.put(key, rtp.getParemeterValue(key));
			}
			tRtp.setExtraParams(map);

			tPayload.setRtp(tRtp);
		} catch (ArgumentNotSetException e) {
		}
		return tPayload;
	}

	private static Fraction fraction2Thrift(
			com.kurento.commons.media.format.payload.Fraction fraction) {
		if (fraction == null)
			return null;

		Fraction ret = new Fraction(fraction.getNum(), fraction.getDenom());
		return ret;
	}

	private static Transport transport2Thrift(
			com.kurento.commons.media.format.Transport transport) {
		Transport tTransport = new Transport();
		try {
			com.kurento.commons.media.format.transport.TransportRtp rtp = transport
					.getRtp();

			TransportRtp trtp = new TransportRtp(rtp.getAddress(),
					rtp.getPort());
			tTransport.setRtp(trtp);
		} catch (ArgumentNotSetException e) {
		}

		return tTransport;
	}

}
