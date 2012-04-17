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
import com.kurento.commons.media.format.payload.PayloadRtp;
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
				rtp.setChannels(thriftRtp.getWidth());

			if (thriftRtp.isSetHeight())
				rtp.setChannels(thriftRtp.getHeight());

			if (thriftRtp.isSetBitrate())
				rtp.setChannels(thriftRtp.getBitrate());

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

	private static Transport thrift2Transport(
			com.kurento.commons.mediaspec.Transport thriftTransport) {
		Transport transport = new Transport();
		if (thriftTransport.isSetRtp()) {
			TransportRtp rtp = new TransportRtp(thriftTransport.getRtp()
					.getAddress(), thriftTransport.getRtp().getPort());
			transport.setRtp(rtp);
		}
		return transport;
	}
}
