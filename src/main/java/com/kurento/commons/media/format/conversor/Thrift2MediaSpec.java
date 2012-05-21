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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.Payload;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.media.format.Transport;
import com.kurento.commons.media.format.enums.MediaType;
import com.kurento.commons.media.format.enums.Mode;
import com.kurento.commons.media.format.payload.Fraction;
import com.kurento.commons.media.format.payload.PayloadRtp;
import com.kurento.commons.media.format.transport.TransportRtmp;
import com.kurento.commons.media.format.transport.TransportRtp;

public class Thrift2MediaSpec {

	public static SessionSpec thrift2SessionSpec(
			com.kurento.commons.mediaspec.SessionSpec thriftSpec) {
		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		for (com.kurento.commons.mediaspec.MediaSpec thriftMedia : thriftSpec
				.getMedias()) {
			MediaSpec media = thrift2MediaSpec(thriftMedia);
			medias.add(media);
		}

		SessionSpec spec = new SessionSpec(medias, thriftSpec.getId());
		if (thriftSpec.isSetVersion())
			spec.setVersion(thriftSpec.getVersion());

		return spec;
	}

	private static MediaSpec thrift2MediaSpec(
			com.kurento.commons.mediaspec.MediaSpec thriftMedia) {
		Collection<Payload> payloads = new ArrayList<Payload>();
		for (com.kurento.commons.mediaspec.Payload thriftPayload : thriftMedia
				.getPayloads()) {
			Payload payload = thrift2Payload(thriftPayload);
			payloads.add(payload);
		}

		Collection<MediaType> types = new ArrayList<MediaType>();
		for (com.kurento.commons.mediaspec.MediaType thriftType : thriftMedia
				.getType()) {
			types.add(MediaType.getInstance(thriftType.toString()));
		}

		Transport transport = thrift2Transport(thriftMedia.getTransport());
		Mode mode = Mode.getInstance(thriftMedia.getDirection().toString());

		MediaSpec media = new MediaSpec(payloads, types, transport, mode);

		return media;
	}

	private static Payload thrift2Payload(
			com.kurento.commons.mediaspec.Payload thriftPayload) {
		Payload payload = new Payload();
		if (thriftPayload.isSetRtp()) {
			com.kurento.commons.mediaspec.PayloadRtp thriftRtp = thriftPayload
					.getRtp();
			PayloadRtp rtp = new PayloadRtp(thriftRtp.getId(),
					thriftRtp.getCodecName(), thriftRtp.getClockRate());

			if (thriftRtp.isSetChannels())
				rtp.setChannels(thriftRtp.getChannels());

			if (thriftRtp.isSetWidth())
				rtp.setWidth(thriftRtp.getWidth());

			if (thriftRtp.isSetHeight())
				rtp.setHeight(thriftRtp.getHeight());

			if (thriftRtp.isSetBitrate())
				rtp.setBitrate(thriftRtp.getBitrate());

			if (thriftRtp.isSetFramerate()) {
				try {
					rtp.setFramerate(thrift2Fraction(thriftRtp.getFramerate()));
				} catch (IllegalArgumentException ex) {
				}
			}

			if (thriftRtp.isSetExtraParams()) {
				Map<String, String> params = thriftRtp.getExtraParams();
				for (String key : params.keySet()) {
					rtp.setParameterValue(key, params.get(key));
				}
			}

			payload.setRtp(rtp);
		}
		return payload;
	}

	private static Fraction thrift2Fraction(
			com.kurento.commons.mediaspec.Fraction thriftFraction) {
		return new Fraction(thriftFraction.num, thriftFraction.denom);
	}

	private static Transport thrift2Transport(
			com.kurento.commons.mediaspec.Transport thriftTransport) {
		Transport transport = new Transport();
		if (thriftTransport.isSetRtp()) {
			TransportRtp rtp = new TransportRtp(thriftTransport.getRtp()
					.getAddress(), thriftTransport.getRtp().getPort());
			transport.setRtp(rtp);
		}

		if (thriftTransport.isSetRtmp()) {
			com.kurento.commons.mediaspec.TransportRtmp thriftRtmp = thriftTransport
					.getRtmp();
			TransportRtmp rtmp = new TransportRtmp();

			if (thriftRtmp.isSetUrl())
				rtmp.setUrl(thriftRtmp.getUrl());

			if (thriftRtmp.isSetPlay())
				rtmp.setPlay(thriftRtmp.getPlay());

			if (thriftRtmp.isSetPublish())
				rtmp.setPublish(thriftRtmp.getPublish());

			transport.setRtmp(rtmp);
		}

		return transport;
	}
}
