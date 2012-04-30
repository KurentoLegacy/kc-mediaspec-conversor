package com.kurento.commons.media.format.conversor;

import gov.nist.javax.sdp.fields.SDPField;

import javax.sdp.SdpConstants;

import com.kurento.commons.media.format.Payload;
import com.kurento.commons.media.format.exceptions.ArgumentNotSetException;
import com.kurento.commons.media.format.payload.PayloadRtp;

class FormatParametersConversor {

	protected static void parseFormatParameters(Payload payload, String value) {
		PayloadRtp rtp;

		try {
			rtp = payload.getRtp();
		} catch (ArgumentNotSetException ex) {
			return;
		}

		if (rtp.getCodecName().equalsIgnoreCase("AMR")) {
			int idx = value.indexOf(" ");
			String paramsStr = value.substring(idx + 1);
			System.out.println(">" + paramsStr + "<");
			String[] params = paramsStr.split(",");
			for (String param : params) {
				String[] pk = param.split("=");
				if (pk.length != 2) {
					continue;
				}
				if (pk[0].equalsIgnoreCase("octet-align"))
					rtp.setParameterValue(pk[0], pk[1]);
			}
		}

		System.out.println("fmtp: " + value);
	}

	protected static String getFormatParameters(PayloadRtp rtp) {
		StringBuilder sb = new StringBuilder();

		if (rtp.getCodecName().equalsIgnoreCase("AMR")) {
			String value = rtp.getParemeterValue("octet-align");
			if (value != null)
				sb.append("octet-align=" + value);
		}

		if (sb.length() != 0)
			return SDPField.ATTRIBUTE_FIELD + SdpConstants.FMTP + ":"
					+ rtp.getId() + " " + sb.toString() + "\r\n";
		else
			return "";
	}
}
