package com.kurento.commons.media.format.conversor;

import gov.nist.javax.sdp.fields.AttributeField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.kurento.commons.media.format.payload.PayloadRtp;
import com.kurento.commons.media.format.transport.TransportRtp;

public class SdpConversor {

	public static SessionSpec sessionSpecFromSDP(String sdp)
			throws SdpException {
		return sessionSpecFromSDP(SdpFactory.getInstance().createSessionDescription(sdp));
	}

	public static SessionSpec sessionSpecFromSDP(SessionDescription sdp)
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
				MediaSpec ms = mediaSpecFromSDP(media, sdp);
				medias.add(ms);
			} catch (SdpException ex) {

			}
		}

		SessionSpec spec = new SessionSpec(medias, sdp.getOrigin().getSessionId() + "");
		return spec;
	}

	public static MediaSpec mediaSpecFromSDP(MediaDescription md,
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
				Payload payload = PayloadFromSDP(format, md);
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
							getPayloadFromString(field.getValue()));

					/* If already exists create a new one with data */
					if (payload != null)
						payloads.remove(payload);

					payload = PayloadFromSDP(field.getValue(), md);
					payloads.add(payload);
				} else if (SdpConstants.FMTP.equalsIgnoreCase(name)) {
					payload = getPayloadById(payloads,
							getPayloadFromString(field.getValue()));
					if (payload != null) {
						// TODO: Set format parameters
						System.out.println("TODO: Set format parameters");
						// payload.setFormatParams(PayloadSpec
						// .removePayloadFromString(field.getValue()));
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

	private static Payload PayloadFromSDP(String format, MediaDescription md)
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
			PayloadRtp rtp = payload.getRtp();
			if (rtp == null)
				continue;

			if (rtp.getId() == id)
				return payload;
		}

		return null;
	}

	private static int getPayloadFromString(String str) {
		String[] tokens = str.split(" ");
		return Integer.parseInt(tokens[0]);
	}

	public static PayloadRtp getDefaultRtpPayload(int id, MediaType type)
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
}
