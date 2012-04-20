package com.kurento.commons.media.format.conversor;

import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.fields.SDPFieldNames;
import gov.nist.javax.sdp.fields.SDPKeywords;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.sdp.BandWidth;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.Payload;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.media.format.Transport;
import com.kurento.commons.media.format.enums.MediaType;
import com.kurento.commons.media.format.enums.Mode;
import com.kurento.commons.media.format.exceptions.ArgumentNotSetException;
import com.kurento.commons.media.format.payload.PayloadRtp;
import com.kurento.commons.media.format.transport.TransportRtp;

public class SdpConversor {

	private static final String ENDLINE = "\r\n";
	private static final String DEFAULT_SDP_VERSION = "0";
	public static final String DEFAULT_VERSION = "12345";
	public static final String DEFAULT_NAME = "-";
	private static final String DEFAULT_SESSION_NAME = "-"; // Needed for
															// Linphone (e.g.)

	public static SessionSpec sdp2SessionSpec(String sdp)
			throws SdpException {
		return sdp2SessionSpec(SdpFactory.getInstance().createSessionDescription(sdp));
	}

	public static SessionSpec sdp2SessionSpec(SessionDescription sdp)
			throws SdpException {
		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		@SuppressWarnings("unchecked")
		Vector<MediaDescription> sdpMedias = sdp.getMediaDescriptions(true);
		if (sdpMedias == null)
			throw new SdpException("No medias found");
		int bandWidth = sdp.getBandwidth(BandWidth.AS);
		for (MediaDescription media : sdpMedias) {
			try {
				int mediaBandWidth = media.getBandwidth(BandWidth.AS);
				if (bandWidth >= 0
						&& (mediaBandWidth <= 0 || mediaBandWidth > bandWidth))
					media.setBandwidth(BandWidth.AS, bandWidth);
				MediaSpec ms = sdp2MediaSpec(media, sdp);
				medias.add(ms);
			} catch (SdpException ex) {

			}
		}

		SessionSpec spec = new SessionSpec(medias, sdp.getOrigin().getSessionId() + "");
		return spec;
	}

	private static MediaSpec sdp2MediaSpec(MediaDescription md,
			SessionDescription sdp) throws SdpException {
		Mode mediaTypeMode = null;
		Media media = md.getMedia();
		if (media == null)
			throw new SdpException("Media can not be null");

		ArrayList<Payload> payloads = new ArrayList<Payload>();

		@SuppressWarnings("unchecked")
		Vector<String> formats = media.getMediaFormats(true);
		if (formats == null)
			throw new SdpException("No formats found");
		for (String format : formats) {
			try {
				Payload payload = sdp2Payload(format, md);
				payloads.add(payload);
			} catch (NumberFormatException ex) {

			} catch (SdpException ex) {

			}
		}

		@SuppressWarnings("unchecked")
		Vector<AttributeField> atributeList = md.getAttributes(false);
		if (atributeList != null && !atributeList.isEmpty()) {
			for (AttributeField field : atributeList) {
				String name = field.getName();
				Payload payload = null;
				Mode mode = Mode.getInstance(name);
				if (SdpConstants.RTPMAP.equalsIgnoreCase(name)) {
					payload = getPayloadById(payloads,
							getPayloadIdFromString(field.getValue()));

					/* If already exists create a new one with data */
					if (payload != null)
						payloads.remove(payload);

					payload = sdp2Payload(field.getValue(), md);
					payloads.add(payload);
				} else if (SdpConstants.FMTP.equalsIgnoreCase(name)) {
					payload = getPayloadById(payloads,
							getPayloadIdFromString(field.getValue()));
					if (payload != null) {
						// TODO: Set format parameters
					}
				} else if (mode != null) {
					mediaTypeMode = mode;
					// } else {
					// log.debug("Media attribute ingnored: " +
					// field.getName());
				}
			}
		}

		HashSet<MediaType> types = new HashSet<MediaType>();
		types.add(MediaType.getInstance(media.getMediaType()));

		Transport transport = new Transport();
		TransportRtp transportRtp = new TransportRtp(sdp.getConnection()
				.getAddress(), media.getMediaPort());
		transport.setRtp(transportRtp);

		if (mediaTypeMode == null)
			mediaTypeMode = Mode.SENDRECV;

		MediaSpec ms = new MediaSpec(payloads, types, transport, mediaTypeMode);
		return ms;
	}

	private static Payload sdp2Payload(String format, MediaDescription md)
			throws SdpException {
		Payload payload = new Payload();

		String[] tokens = format.split(" ");
		int id = Integer.parseInt(tokens[0]);

		PayloadRtp rtp;
		if (tokens.length <= 1) {
			rtp = getDefaultRtpPayload(id,
					MediaType.getInstance(md.getMedia().getMediaType()));
		}else {
			String[] values = tokens[1].split("/");
			if (values.length != 2 && values.length != 3) {
				throw new SdpException("Format not spected encodingName/clockRate[/channels]");
			}

			String codecName = values[0];
			int clockRate = Integer.parseInt(values[1]);
			rtp = new PayloadRtp(id, codecName, clockRate);
			if (values.length == 3)
				rtp.setChannels(Integer.parseInt(values[2]));
		}
		rtp.setBitrate(md.getBandwidth(BandWidth.AS));

		payload.setRtp(rtp);
		return payload;
	}

	private static Payload getPayloadById(List<Payload> payloads, int id) {
		for (Payload payload : payloads) {
			try {
				PayloadRtp rtp = payload.getRtp();
				if (rtp.getId() == id)
					return payload;
			} catch (ArgumentNotSetException e) {
			}
		}

		return null;
	}

	private static int getPayloadIdFromString(String str) {
		String[] tokens = str.split(" ");
		return Integer.parseInt(tokens[0]);
	}

	private static PayloadRtp getDefaultRtpPayload(int id, MediaType type)
			throws SdpException {
		/*
	      According to RFC 3551

      PT   encoding    media type  clock rate   channels
            name                    (Hz)
       ___________________________________________________
       0    PCMU        A            8,000       1
       1    reserved    A
       2    reserved    A
       3    GSM         A            8,000       1
       4    G723        A            8,000       1
       5    DVI4        A            8,000       1
       6    DVI4        A           16,000       1
       7    LPC         A            8,000       1
       8    PCMA        A            8,000       1
       9    G722        A            8,000       1
       10   L16         A           44,100       2
       11   L16         A           44,100       1
       12   QCELP       A            8,000       1
       13   CN          A            8,000       1
       14   MPA         A           90,000       (see text)
       15   G728        A            8,000       1
       16   DVI4        A           11,025       1
       17   DVI4        A           22,050       1
       18   G729        A            8,000       1
       19   reserved    A
       20   unassigned  A
       21   unassigned  A
       22   unassigned  A
       23   unassigned  A
       dyn  G726-40     A            8,000       1
       dyn  G726-32     A            8,000       1
       dyn  G726-24     A            8,000       1
       dyn  G726-16     A            8,000       1
       dyn  G729D       A            8,000       1
       dyn  G729E       A            8,000       1
       dyn  GSM-EFR     A            8,000       1
       dyn  L8          A            var.        var.
       dyn  RED         A                        (see text)
       dyn  VDVI        A            var.        1

       Table 4: Payload types (PT) for audio encodings

       PT      encoding    media type  clock rate
               name                    (Hz)
       _____________________________________________
       24      unassigned  V
       25      CelB        V           90,000
       26      JPEG        V           90,000
       27      unassigned  V
       28      nv          V           90,000
       29      unassigned  V
       30      unassigned  V
       31      H261        V           90,000
       32      MPV         V           90,000
       33      MP2T        AV          90,000
       34      H263        V           90,000
       35-71   unassigned  ?
       72-76   reserved    N/A         N/A
       77-95   unassigned  ?
       96-127  dynamic     ?
       dyn     H263-1998   V           90,000

       Table 5: Payload types (PT) for video and combined
                encodings

	 */

		Integer clockRate;
		String encodingName;
		MediaType mediaType = null;
		Integer channels = null;

		switch (id) {
		case 0:
			clockRate = 8000;
			encodingName = "PCMU";
			mediaType = MediaType.AUDIO;
			break;
		case 3:
			clockRate = 8000;
			encodingName = "GSM";
			mediaType = MediaType.AUDIO;
			break;
		case 4:
			clockRate = 8000;
			encodingName = "G723";
			mediaType = MediaType.AUDIO;
			break;
		case 5:
			clockRate = 8000;
			encodingName = "DVI4";
			mediaType = MediaType.AUDIO;
			break;
		case 6:
			clockRate = 16000;
			encodingName = "DVI4";
			mediaType = MediaType.AUDIO;
			break;
		case 7:
			clockRate = 8000;
			encodingName = "LPC";
			mediaType = MediaType.AUDIO;
			break;
		case 8:
			clockRate = 8000;
			encodingName = "PCMA";
			mediaType = MediaType.AUDIO;
			break;
		case 9:
			clockRate = 8000;
			encodingName = "G722";
			mediaType = MediaType.AUDIO;
			break;
		case 10:
			clockRate = 44100;
			encodingName = "L16";
			mediaType = MediaType.AUDIO;
			channels = 2;
			break;
		case 11:
			clockRate = 44100;
			encodingName = "L16";
			mediaType = MediaType.AUDIO;
			break;
		case 12:
			clockRate = 8000;
			encodingName = "QCELP";
			mediaType = MediaType.AUDIO;
			break;
		case 13:
			clockRate = 8000;
			encodingName = "CN";
			mediaType = MediaType.AUDIO;
			break;
		case 14:
			clockRate = 90000;
			encodingName = "MPA";
			mediaType = MediaType.AUDIO;
			break;
		case 15:
			clockRate = 8000;
			encodingName = "G728";
			mediaType = MediaType.AUDIO;
			break;
		case 16:
			clockRate = 11025;
			encodingName = "DVI4";
			mediaType = MediaType.AUDIO;
			break;
		case 17:
			clockRate = 22050;
			encodingName = "DVI4";
			mediaType = MediaType.AUDIO;
			break;
		case 18:
			clockRate = 8000;
			encodingName = "G729";
			mediaType = MediaType.AUDIO;
			break;
		case 25:
			clockRate = 90000;
			encodingName = "CelB";
			mediaType = MediaType.VIDEO;
			break;
		case 26:
			clockRate = 90000;
			encodingName = "JPEG";
			mediaType = MediaType.VIDEO;
			break;
		case 28:
			clockRate = 90000;
			encodingName = "nv";
			mediaType = MediaType.VIDEO;
			break;
		case 31:
			clockRate = 90000;
			encodingName = "H261";
			mediaType = MediaType.VIDEO;
			break;
		case 32:
			clockRate = 90000;
			encodingName = "MPV";
			mediaType = MediaType.VIDEO;
			break;
		case 33:
			clockRate = 90000;
			encodingName = "MP2T";
			break;
		case 34:
			clockRate = 90000;
			encodingName = "H263";
			mediaType = MediaType.VIDEO;
			break;
		default:
			throw new SdpException("Invalid payload id");
		}

		if (mediaType != null && mediaType != type) {
			throw new SdpException("Invalid media type");
		}

		PayloadRtp rtp = new PayloadRtp(id, encodingName, clockRate);
		if (channels != null)
			rtp.setChannels(channels);

		return rtp;
	}

	public static SessionDescription sessionSpec2SessionDescription(
			SessionSpec spec) throws SdpException {
		return SdpFactory.getInstance().createSessionDescription(
				sessionSpec2Sdp(spec));
	}

	public static String sessionSpec2Sdp(SessionSpec spec)
			throws SdpException {
		StringBuilder sb = new StringBuilder();

		String address = getAddress(spec);

		sb.append(SDPFieldNames.PROTO_VERSION_FIELD + DEFAULT_SDP_VERSION
				+ ENDLINE);
		sb.append(SDPFieldNames.ORIGIN_FIELD + DEFAULT_NAME + " "
				+ spec.getId() + " " + DEFAULT_VERSION + " " + SDPKeywords.IN
				+ " " + SDPKeywords.IPV4 + " " + address + ENDLINE);
		sb.append(SDPFieldNames.SESSION_NAME_FIELD + DEFAULT_SESSION_NAME
				+ ENDLINE);
		sb.append(SDPFieldNames.CONNECTION_FIELD + SDPKeywords.IN + " "
				+ SDPKeywords.IPV4 + " " + address + ENDLINE);
		sb.append(SDPFieldNames.TIME_FIELD + "0 0" + ENDLINE);

		for (MediaSpec media : spec.getMediaSpecs()) {
			sb.append(mediaSpec2Sdp(media));
		}

		return sb.toString();
	}

	private static String mediaSpec2Sdp(MediaSpec media) {
		StringBuilder sb = new StringBuilder();
		Set<MediaType> types = media.getTypes();
		TransportRtp transport;
		try {
			transport = media.getTransport().getRtp();
		} catch (ArgumentNotSetException e1) {
			return "";
		}

		if (types.size() != 1) {
			return "";
		}

		sb.append(SDPFieldNames.MEDIA_FIELD + types.iterator().next() + " ");
		if (media.getPayloads().size() == 0)
			sb.append(0);
		else
			sb.append(transport.getPort());
		sb.append(" " + SdpConstants.RTP_AVP);

		StringBuilder payloadString = new StringBuilder();
		int bitRate = -1;

		for (Payload payload : media.getPayloads()) {

			try {
				PayloadRtp rtp = payload.getRtp();
				int rtpBitrate = rtp.getBitrate();
				if (rtpBitrate != -1 && (rtpBitrate < bitRate || bitRate == -1)) {
					bitRate = rtpBitrate;
				}
				sb.append(" ").append(rtp.getId());
				payloadString.append(payloadRtp2Sdp(rtp));
			} catch (ArgumentNotSetException e1) {

			}
		}
		sb.append(ENDLINE);
		sb.append(payloadString);

		sb.append(SDPFieldNames.ATTRIBUTE_FIELD);
		if (media.getPayloads().size() == 0)
			sb.append(Mode.INACTIVE);
		else
			sb.append(media.getMode());
		sb.append(ENDLINE);
		if (bitRate > 0)
			sb.append(SDPFieldNames.BANDWIDTH_FIELD + BandWidth.AS + ":"
					+ bitRate + ENDLINE);

		return sb.toString();
	}

	private static String payloadRtp2Sdp(PayloadRtp payload) {
		StringBuilder sb = new StringBuilder();

		sb.append(SDPFieldNames.ATTRIBUTE_FIELD + SdpConstants.RTPMAP + ":"
				+ payload.getId() + " " + payload.getCodecName() + "/"
				+ payload.getClockRate());
		try {
			sb.append("/" + payload.getChannels());
		} catch (ArgumentNotSetException e) {
		}

		sb.append(ENDLINE);

		return sb.toString();
	}

	private static String getAddress(SessionSpec spec) throws SdpException {
		String address = null;
		for (MediaSpec media : spec.getMediaSpecs()) {
			TransportRtp tr = null;
			try {
				tr = media.getTransport().getRtp();
			} catch (ArgumentNotSetException e) {
				continue;
			}

			if (address == null)
				address = tr.getAddress();
			else if (!address.equalsIgnoreCase(tr.getAddress())) {
				throw new SdpException("Address does not match on all medias");
			}
		}
		if (address == null)
			throw new SdpException("Address not found");
		return address;
	}
}
