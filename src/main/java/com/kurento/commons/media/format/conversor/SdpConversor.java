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

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kurento.mediaspec.Direction;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mediaspec.Transport;
import com.kurento.mediaspec.TransportIce;
import com.kurento.mediaspec.TransportIceCandidate;
import com.kurento.mediaspec.TransportIceCandidateTransport;
import com.kurento.mediaspec.TransportIceCandidateTransportUtils;
import com.kurento.mediaspec.TransportIceCandidateType;
import com.kurento.mediaspec.TransportIceCandidateTypeUtils;
import com.kurento.mediaspec.TransportRtp;

public class SdpConversor {

	private static final Logger log = LoggerFactory
			.getLogger(SdpConversor.class);

	private static final String ENDLINE = "\r\n";
	private static final String DEFAULT_SDP_VERSION = "0";
	public static final String DEFAULT_VERSION = "12345";
	public static final String DEFAULT_NAME = "-";
	private static final String DEFAULT_SESSION_NAME = "-"; // Needed for
															// Linphone (e.g.)

	private static final int FOUNDATION_MAX_LEN = 32;
	private static final String ICE_PWD = "ice-pwd";
	private static final String ICE_UFRAG = "ice-ufrag";
	private static final String ICE_CANDIDATE = "candidate";

	public static SessionSpec sdp2SessionSpec(String sdp)
			throws SdpException {
		if (sdp == null || sdp.equals(""))
			return null;
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
		String icePassword = null, iceUser = null;

		@SuppressWarnings("unchecked")
		Vector<AttributeField> attributeList = sdp.getAttributes(false);
		if (attributeList != null) {
			for (AttributeField att : attributeList) {
				if (att.getName().equalsIgnoreCase(ICE_PWD)) {
					icePassword = att.getValue();
				} else if (att.getName().equalsIgnoreCase(ICE_UFRAG)) {
					iceUser = att.getValue();
				} else {
					log.warn("Unknown atribute: " + att.getName() + ", value: "
							+ att.getValue());
				}
			}
		}

		for (MediaDescription media : sdpMedias) {
			try {
				int mediaBandWidth = media.getBandwidth(BandWidth.AS);
				if (bandWidth >= 0
						&& (mediaBandWidth <= 0 || mediaBandWidth > bandWidth))
					media.setBandwidth(BandWidth.AS, bandWidth);
				MediaSpec ms = sdp2MediaSpec(media, sdp);
				setIceUserPasswd(ms, iceUser, icePassword);
				medias.add(ms);
			} catch (SdpException ex) {

			}
		}

		SessionSpec spec = new SessionSpec(medias, sdp.getOrigin().getSessionId() + "");
		return spec;
	}

	private static void setIceUserPasswd(MediaSpec ms, String iceUser,
			String icePassword) {
		if (iceUser == null || icePassword == null || ms == null)
			return;

		if (!ms.isSetTransport() || !ms.getTransport().isSetIce())
			return;

		TransportIce ice = ms.getTransport().getIce();

		if (!ice.isSetCandidates())
			return;

		for (TransportIceCandidate cand : ice.getCandidates()) {
			if (!cand.isSetPassword())
				cand.setPassword(icePassword);

			if (!cand.isSetUsername())
				cand.setUsername(iceUser);
		}
	}

	private static MediaSpec sdp2MediaSpec(MediaDescription md,
			SessionDescription sdp) throws SdpException {
		Direction mediaTypeMode = null;
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

		Transport transport = new Transport();
		if (md.getConnection() != null) {
			TransportRtp transportRtp = new TransportRtp(md.getConnection()
					.getAddress(), media.getMediaPort());
			transport.setRtp(transportRtp);
		} else if (sdp.getConnection() != null) {
			TransportRtp transportRtp = new TransportRtp(sdp.getConnection()
					.getAddress(), media.getMediaPort());
			transport.setRtp(transportRtp);
		} else {
			throw new SdpException("Invalid connection");
		}

		@SuppressWarnings("unchecked")
		Vector<AttributeField> atributeList = md.getAttributes(false);
		String password = null;
		String user = null;

		if (atributeList != null && !atributeList.isEmpty()) {
			for (AttributeField field : atributeList) {
				String name = field.getName();
				Payload payload = null;
				Direction mode;
				try {
					mode = Direction.valueOf(name.toUpperCase());
				} catch (IllegalArgumentException ex) {
					mode = null;
				}
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
						FormatParametersConversor.parseFormatParameters(payload,
								field.getValue());
					}
				} else if (FormatParametersConversor.EXTRA_ATTRIBUTES
						.equalsIgnoreCase(name)) {
					payload = getPayloadById(payloads,
							getPayloadIdFromString(field.getValue()));
					if (payload != null) {
						FormatParametersConversor.parseExtraAttributes(
								payload, field.getValue());
					}
				} else if (ICE_CANDIDATE.equalsIgnoreCase(name)) {
					String candStr = field.getValue();
					String[] tokens = candStr.split(" ");
					TransportIceCandidate cand = new TransportIceCandidate();

					if (tokens.length < 8) {
						log.warn("Ignoring candidate with invalid number of tokens: "
								+ candStr);
						continue;
					}

					TransportIceCandidateType type;
					TransportIceCandidateTransport iceTransport;
					int port;
					int componentId;
					int priority;

					int i = find_token(tokens, "typ");
					if (i > 0 && tokens.length >= i + 1) {
						type = TransportIceCandidateTypeUtils
								.getFromString(tokens[i + 1]);
						if (type == null)
							continue;

						cand.setType(type);
					} else {
						continue;
					}

					iceTransport = TransportIceCandidateTransportUtils
							.getFromString(tokens[2]);
					if (iceTransport == null)
						continue;
					cand.setTransport(iceTransport);

					try {
						port = Integer.parseInt(tokens[5]);
						cand.setPort(port);
					} catch (NumberFormatException e) {
						continue;
					}

					try {
						componentId = Integer.parseInt(tokens[1]);
						cand.setComponentId(componentId);
					} catch (NumberFormatException e) {
						continue;
					}

					cand.setAddress(tokens[4]);

					try {
						priority = Integer.parseInt(tokens[3]);
						cand.setPriority(priority);
					} catch (NumberFormatException e) {
						continue;
					}

					if (tokens[0].length() <= FOUNDATION_MAX_LEN)
						cand.setFoundation(tokens[0]);
					else
						continue;

					i = find_token(tokens, "raddr");
					if (i > 0 && tokens.length >= i + 1)
						cand.setBaseAddress(tokens[i + 1]);

					i = find_token(tokens, "rport");
					if (i > 0 && tokens.length >= i + 1) {
						try {
							int basePort;
							basePort = Integer.parseInt(tokens[5]);
							cand.setBasePort(basePort);
						} catch (NumberFormatException e) {
							continue;
						}
					}

					if (!transport.isSetIce())
						transport.setIce(new TransportIce());

					transport.getIce().addToCandidates(cand);
				} else if (name.equalsIgnoreCase(ICE_PWD)) {
					password = field.getValue();
				} else if (name.equalsIgnoreCase(ICE_UFRAG)) {
					user = field.getValue();
				} else if (mode != null) {
					mediaTypeMode = mode;
				} else {
					log.warn("Ignored field name: " + field.getName());
				}
			}
		}

		HashSet<MediaType> types = new HashSet<MediaType>();
		types.add(MediaType.valueOf(media.getMediaType().toUpperCase()));

		if (mediaTypeMode == null)
			mediaTypeMode = Direction.SENDRECV;

		MediaSpec ms = new MediaSpec(payloads, types, transport, mediaTypeMode);

		setIceUserPasswd(ms, user, password);
		return ms;
	}

	private static int find_token(String[] tokens, String str) {
		for (int i =0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.equalsIgnoreCase(str))
				return i;
		}

		return -1;
	}

	private static Payload sdp2Payload(String format, MediaDescription md)
			throws SdpException {
		Payload payload = new Payload();

		String[] tokens = format.split(" ");
		int id = Integer.parseInt(tokens[0]);

		PayloadRtp rtp;
		if (tokens.length <= 1) {
			rtp = getDefaultRtpPayload(id,
					MediaType.valueOf(md.getMedia().getMediaType()
							.toUpperCase()));
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

		int bitrate = md.getBandwidth(BandWidth.AS);
		if (bitrate > 0)
			rtp.setBitrate(bitrate);

		payload.setRtp(rtp);
		return payload;
	}

	private static Payload getPayloadById(List<Payload> payloads, int id) {
		for (Payload payload : payloads) {
			if (payload.isSetRtp()) {
				PayloadRtp rtp = payload.getRtp();
				if (rtp.getId() == id)
					return payload;
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
		if (spec == null)
			return null;
		return SdpFactory.getInstance().createSessionDescription(
				sessionSpec2Sdp(spec));
	}

	public static String sessionSpec2Sdp(SessionSpec spec)
			throws SdpException {

		if (spec == null)
			return "";

		try {
			spec.validate();
		} catch (TException e) {
			throw new SdpException(e);
		}

		StringBuilder sb = new StringBuilder();

		String address = getAddress(spec);
		String addressOrigin;

		if (address == null)
			addressOrigin = "127.0.0.1";
		else
			addressOrigin = address;

		IceUserPasswordContainer iceUserPassw = getIceUserPassword(spec);

		sb.append(SDPFieldNames.PROTO_VERSION_FIELD + DEFAULT_SDP_VERSION
				+ ENDLINE);
		sb.append(SDPFieldNames.ORIGIN_FIELD + DEFAULT_NAME + " "
				+ spec.getId() + " " + DEFAULT_VERSION + " " + SDPKeywords.IN
				+ " " + SDPKeywords.IPV4 + " " + addressOrigin + ENDLINE);
		sb.append(SDPFieldNames.SESSION_NAME_FIELD + DEFAULT_SESSION_NAME
				+ ENDLINE);
		if (address != null)
			sb.append(SDPFieldNames.CONNECTION_FIELD + SDPKeywords.IN + " "
					+ SDPKeywords.IPV4 + " " + address + ENDLINE);
		sb.append(SDPFieldNames.TIME_FIELD + "0 0" + ENDLINE);

		if (iceUserPassw.bothSet()) {
			sb.append(SDPFieldNames.ATTRIBUTE_FIELD + ICE_PWD + ":"
					+ iceUserPassw.password + ENDLINE);
			sb.append(SDPFieldNames.ATTRIBUTE_FIELD + ICE_UFRAG + ":"
					+ iceUserPassw.user + ENDLINE);
		}

		for (MediaSpec media : spec.getMedias()) {
			sb.append(mediaSpec2Sdp(media, iceUserPassw, address));
		}

		return sb.toString();
	}

	private static String mediaSpec2Sdp(MediaSpec media,
			IceUserPasswordContainer iceUserPassw, String address)
			throws SdpException {
		StringBuilder sb = new StringBuilder();
		Set<MediaType> types = media.getType();
		TransportRtp transport;

		try {
			media.validate();
		} catch (TException e) {
			throw new SdpException(e);
		}

		if (!media.getTransport().isSetRtp())
			return "";

		transport = media.getTransport().getRtp();

		try {
			transport.validate();
		} catch (TException e) {
			throw new SdpException(e);
		}

		if (types.size() != 1) {
			return "";
		}

		sb.append(SDPFieldNames.MEDIA_FIELD
				+ types.iterator().next().toString().toLowerCase() + " ");
		if (media.getPayloads().size() == 0)
			sb.append(0);
		else
			sb.append(transport.getPort());
		sb.append(" " + SdpConstants.RTP_AVP);

		StringBuilder payloadString = new StringBuilder();
		int bitRate = -1;

		for (Payload payload : media.getPayloads()) {

			if (payload.isSetRtp()) {
				PayloadRtp rtp = payload.getRtp();
				sb.append(" ").append(rtp.getId());
				payloadString.append(payloadRtp2Sdp(rtp));
				if (rtp.isSetBitrate()) {
					int rtpBitrate = rtp.getBitrate();
					if (rtpBitrate != -1
							&& (rtpBitrate < bitRate || bitRate == -1)) {
						bitRate = rtpBitrate;
					}
				}
			}
		}
		sb.append(ENDLINE);

		if (!iceUserPassw.bothSet()) {
			IceUserPasswordContainer mediaIceUserPassw = getIceUserPassword(media);
			if (mediaIceUserPassw.bothSet()) {
				sb.append(SDPFieldNames.ATTRIBUTE_FIELD + ICE_PWD + ":"
						+ mediaIceUserPassw.password + ENDLINE);
				sb.append(SDPFieldNames.ATTRIBUTE_FIELD + ICE_UFRAG + ":"
						+ mediaIceUserPassw.user + ENDLINE);
			}
		}

		if (address == null) {
			String mediaAddress = getAddress(media);
			sb.append(SDPFieldNames.CONNECTION_FIELD + SDPKeywords.IN + " "
					+ SDPKeywords.IPV4 + " " + mediaAddress + ENDLINE);
		}

		sb.append(payloadString);

		sb.append(SDPFieldNames.ATTRIBUTE_FIELD);
		if (media.getPayloads().size() == 0)
			sb.append(Direction.INACTIVE.toString().toLowerCase());
		else
			sb.append(media.getDirection().toString().toLowerCase());
		sb.append(ENDLINE);

		if (media.getTransport().isSetIce()) {
			TransportIce ice = media.getTransport().getIce();

			try {
				ice.validate();
			} catch (TException e) {
				throw new SdpException(e);
			}

			if (ice.isSetCandidates()) {
				for (TransportIceCandidate cand : ice.getCandidates()) {
					try {
						cand.validate();
					} catch (TException e) {
						throw new SdpException(e);
					}

					TransportIceCandidateType type = cand.getType();

					sb.append(SDPFieldNames.ATTRIBUTE_FIELD + ICE_CANDIDATE
							+ ":");
					sb.append(cand.getFoundation() + " "
							+ cand.getComponentId() + " ");
					sb.append(TransportIceCandidateTransportUtils
							.toSdpString(cand.getTransport()) + " ");
					sb.append(cand.getPriority() + " ");
					sb.append(cand.getAddress() + " ");
					sb.append(cand.getPort() + " ");
					sb.append("typ "
							+ TransportIceCandidateTypeUtils.toSdpString(type));
					if (type != TransportIceCandidateType.HOST
							&& cand.isSetBaseAddress() && cand.isSetBasePort()) {
						sb.append(" raddr " + cand.getBaseAddress());
						sb.append(" rport " + cand.getBasePort());
					}
					sb.append(ENDLINE);
				}
			}
		}

		if (bitRate > 0)
			sb.append(SDPFieldNames.BANDWIDTH_FIELD + BandWidth.AS + ":"
					+ bitRate + ENDLINE);

		return sb.toString();
	}

	private static String payloadRtp2Sdp(PayloadRtp payload)
			throws SdpException {
		StringBuilder sb = new StringBuilder();

		try {
			payload.validate();
		} catch (TException e) {
			throw new SdpException(e);
		}

		sb.append(SDPFieldNames.ATTRIBUTE_FIELD + SdpConstants.RTPMAP + ":"
				+ payload.getId() + " " + payload.getCodecName() + "/"
				+ payload.getClockRate());
		if (payload.isSetChannels()) {
			sb.append("/" + payload.getChannels());
		}

		sb.append(ENDLINE);

		String ftmp = FormatParametersConversor.getFormatParameters(payload);
		sb.append(ftmp);

		String extraAttr = FormatParametersConversor
				.getExtraAttributes(payload);
		sb.append(extraAttr);

		return sb.toString();
	}

	private static String getAddress(SessionSpec spec) throws SdpException {
		String address = null;

		for (MediaSpec media : spec.getMedias()) {
			String mediaAddress = getAddress(media);

			if (mediaAddress == null)
				return null;
			else if (address == null)
				address = mediaAddress;
			else if (!address.equalsIgnoreCase(mediaAddress))
				return null;
		}

		return address;
	}

	private static String getAddress(MediaSpec media) throws SdpException {
		TransportRtp tr = null;
		if (!media.getTransport().isSetRtp())
			return null;

		tr = media.getTransport().getRtp();

		return tr.getAddress();
	}

	private static IceUserPasswordContainer getIceUserPassword(SessionSpec spec)
			throws SdpException {
		IceUserPasswordContainer ret = new IceUserPasswordContainer();

		for (MediaSpec media : spec.getMedias()) {
			IceUserPasswordContainer value = getIceUserPassword(media);

			if (ret.password == null) {
				ret.password = value.password;
			} else if (value.password != null
					&& !ret.password.equalsIgnoreCase(value.password)) {
				ret.password = null;
				ret.user = null;
				return ret;
			}

			if (ret.user == null) {
				ret.user = value.user;
			} else if (value.user != null
					&& !ret.user.equalsIgnoreCase(value.user)) {
				ret.password = null;
				ret.user = null;
				return ret;
			}
		}

		return ret;
	}

	private static IceUserPasswordContainer getIceUserPassword(MediaSpec media)
			throws SdpException {
		IceUserPasswordContainer ret = new IceUserPasswordContainer();

		try {
			media.validate();
		} catch (TException e) {
			throw new SdpException(e);
		}

		TransportIce tr = null;
		if (!media.getTransport().isSetIce())
			return ret;

		tr = media.getTransport().getIce();

		try {
			tr.validate();
		} catch (TException e) {
			throw new SdpException(e);
		}

		for (TransportIceCandidate cand : tr.getCandidates()) {

			try {
				cand.validate();
			} catch (TException e) {
				throw new SdpException(e);
			}

			if (ret.user == null)
				ret.user = cand.getUsername();
			else if (!ret.user.equalsIgnoreCase(cand.getUsername())) {
				throw new SdpException(
						"Ice user does not match on all candidates");
			}

			if (ret.password == null)
				ret.password = cand.getPassword();
			else if (!ret.password.equalsIgnoreCase(cand.getPassword())) {
				throw new SdpException(
						"Password does not match on all candidates");
			}
		}

		return ret;
	}

	private static class IceUserPasswordContainer {
		public String user = null;
		public String password = null;

		public boolean bothSet() {
			return user != null && password != null;
		}
	}
}
