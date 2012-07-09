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

import gov.nist.javax.sdp.fields.SDPField;

import javax.sdp.SdpConstants;

import com.kurento.mediaspec.ArgumentNotSetException;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;

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
