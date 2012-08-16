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

import com.kurento.mediaspec.Fraction;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;

class FormatParametersConversor {

	public static final String EXTRA_ATTRIBUTES = "extra-attr";

	protected static void parseFormatParameters(Payload payload, String value) {
		if (!payload.isSetRtp())
			return;

		PayloadRtp rtp = payload.getRtp();

		if (rtp.getCodecName().equalsIgnoreCase("AMR")) {
			int idx = value.indexOf(" ");
			String paramsStr = value.substring(idx + 1);
			String[] params = paramsStr.split(",");
			for (String param : params) {
				String[] pk = param.split("=");
				if (pk.length != 2) {
					continue;
				}
				if (pk[0].equalsIgnoreCase("octet-align")) {
					rtp.putToExtraParams(pk[0], pk[1]);
				}
			}
		}
	}

	protected static String getFormatParameters(PayloadRtp rtp) {
		StringBuilder sb = new StringBuilder();

		if (rtp.getCodecName().equalsIgnoreCase("AMR")) {
			if (rtp.isSetExtraParams()) {
				String value = rtp.getExtraParams().get("octet-align");
				if (value != null)
					sb.append("octet-align=" + value);
			}
		}

		if (sb.length() != 0)
			return SDPField.ATTRIBUTE_FIELD + SdpConstants.FMTP + ":"
					+ rtp.getId() + " " + sb.toString() + "\r\n";
		else
			return "";
	}

	protected static void parseExtraAttributes(Payload payload, String value) {
		if (!payload.isSetRtp())
			return;

		PayloadRtp rtp = payload.getRtp();

		int idx = value.indexOf(" ");
		String paramsStr = value.substring(idx + 1);
		String[] params = paramsStr.split(";");
		for (String param : params) {
			String[] pk = param.split("=");
			if (pk.length != 2) {
				continue;
			}
			if (pk[0].equalsIgnoreCase("w"))
				rtp.setWidth(Integer.parseInt(pk[1]));
			else if (pk[0].equalsIgnoreCase("h"))
				rtp.setHeight(Integer.parseInt(pk[1]));
			else if (pk[0].equalsIgnoreCase("fr")) {
				String[] frac = pk[1].split("/");
				rtp.setFramerate(new Fraction(Integer.parseInt(frac[0]),
						Integer.parseInt(frac[1])));
			} else
				rtp.putToExtraParams(pk[0], pk[1]);
		}
	}

	protected static String getExtraAttributes(PayloadRtp rtp) {
		StringBuilder sb = new StringBuilder();
		if (rtp.isSetWidth())
			sb.append("w=" + rtp.getWidth() + ";");
		if (rtp.isSetHeight())
			sb.append("h=" + rtp.getHeight() + ";");
		if (rtp.isSetFramerate())
			sb.append("fr=" + rtp.getFramerate().getNum() + "/"
					+ rtp.getFramerate().getDenom() + ";");
		if (rtp.isSetExtraParams()) {
			for (String k : rtp.getExtraParams().keySet()) {
				if (!k.equalsIgnoreCase("octet-align")) {
					String value = rtp.getExtraParams().get(k);
					if (value != null)
						sb.append(k + "=" + value + ";");
				}
			}
		}

		if (sb.length() != 0) {
			sb.deleteCharAt(sb.length() - 1);
			return SDPField.ATTRIBUTE_FIELD + EXTRA_ATTRIBUTES + ":"
					+ rtp.getId() + " " + sb.toString() + "\r\n";
		} else
			return "";
	}
}
