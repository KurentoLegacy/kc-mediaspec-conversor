package com.kurento.commons.media.format.conversor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kurento.commons.media.format.exceptions.ArgumentNotSetException;
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

			Set<String> keys = rtp.getParametersKeys();
			HashMap<String, String> map = new HashMap<String, String>();

			for (String key : keys) {
				map.put(key, rtp.getParemeterValue(key));
			}

			tPayload.setRtp(tRtp);
		} catch (ArgumentNotSetException e) {
		}
		return tPayload;
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
