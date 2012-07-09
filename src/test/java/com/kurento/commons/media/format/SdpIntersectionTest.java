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

package com.kurento.commons.media.format;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.mediaspec.SessionSpec;

public class SdpIntersectionTest extends TestCase {

	static Logger log = LoggerFactory.getLogger(TestCase.class);

	private static String sdp3 = "v=0\r\n" +
		"o=- 12345 12345 IN IP4 193.147.51.19\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.19\r\n" +
		"t=0 0\r\n" +
		"m=video 46250 RTP/AVP 96\r\n" +
		"a=rtpmap:96 MP4V-ES/90000\r\n" +
		"a=sendrecv\r\n" +
		"b=AS:500\r\n";

	private static String sdp4 = "v=0\r\n" +
		"o=- 12345 12345 IN IP4 95.126.68.104\r\n" +
		"s=-\r\n" +
		"c=IN IP4 95.126.68.104\r\n" +
		"t=0 0\r\n" +
		"m=video 45716 RTP/AVP 96\r\n" +
		"b=AS:186\r\n" +
		"a=rtpmap:96 MP4V-ES/90000\r\n" +
		"a=sendrecv\r\n";

	private static String sdp1 = "v=0\r\n" +
		"o=- 123 654321 IN IP4 193.147.51.18\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.18\r\n" +
		"t=0 0\r\n" +
		"m=video 2323 RTP/AVP 96\r\n" +
		"a=rtpmap:96 H263-1998/90000\r\n" +
		"a=sendonly\r\n" +
		"m=audio 5555 RTP/AVP 100\r\n" +
		"a=rtpmap:100 AMR/8000/1\r\n" +
		"a=FMTP:100 octet-align=1\r\n" +
		"a=sendonly\r\n";

	private static String generatedSdp1 = "v=0\r\n" +
		"o=" + SdpConversor.DEFAULT_NAME + " 123 "
			+ SdpConversor.DEFAULT_VERSION + " IN IP4 193.147.51.18\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.18\r\n" +
		"t=0 0\r\n" +
		"m=video 2323 RTP/AVP 96\r\n" +
		"a=rtpmap:96 H263-1998/90000\r\n" +
		"a=sendonly\r\n" +
		"m=audio 5555 RTP/AVP 100\r\n" +
		"a=rtpmap:100 AMR/8000/1\r\n" +
		"a=FMTP:100 octet-align=1\r\n" +
		"a=sendonly\r\n";

	private static String sdp2 = "v=0\r\n" +
		"o=- 456 654321 IN IP4 193.147.51.44\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.44\r\n" +
		"t=0 0\r\n" +
		"m=video 3434 RTP/AVP 96\r\n" +
		"a=rtpmap:96 H263-1998/90000\r\n" +
		"a=sendrecv\r\n" +
		"m=audio 7777 RTP/AVP 100\r\n" +
		"a=rtpmap:100 AMR/8000/1\r\n" +
		"a=FMTP:100 octet-align=1\r\n" +
		"a=sendonly\r\n";

	private static String generatedSdp2 = "v=0\r\n" +
		"o=" + SdpConversor.DEFAULT_NAME + " 456 "
			+ SdpConversor.DEFAULT_VERSION + " IN IP4 193.147.51.44\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.44\r\n" +
		"t=0 0\r\n" +
		"m=video 3434 RTP/AVP 96\r\n" +
		"a=rtpmap:96 H263-1998/90000\r\n" +
		"a=sendrecv\r\n" +
		"m=audio 7777 RTP/AVP 100\r\n" +
		"a=rtpmap:100 AMR/8000/1\r\n" +
		"a=FMTP:100 octet-align=1\r\n" +
		"a=sendonly\r\n";

	private static String sdpIntersectLocal = "v=0\r\n" +
		"o=" + SdpConversor.DEFAULT_NAME + " 456 "
			+ SdpConversor.DEFAULT_VERSION + " IN IP4 193.147.51.18\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.18\r\n" +
		"t=0 0\r\n" +
		"m=video 2323 RTP/AVP 96\r\n" +
		"a=rtpmap:96 H263-1998/90000\r\n" +
		"a=sendonly\r\n" +
		"m=audio 5555 RTP/AVP 100\r\n" +
		"a=rtpmap:100 AMR/8000/1\r\n" +
		"a=FMTP:100 octet-align=1\r\n" +
		"a=inactive\r\n";

	private static String sdpIntersectRemote = "v=0\r\n" +
		"o=" + SdpConversor.DEFAULT_NAME + " 456 "
			+ SdpConversor.DEFAULT_VERSION + " IN IP4 193.147.51.44\r\n" +
		"s=-\r\n" +
		"c=IN IP4 193.147.51.44\r\n" +
		"t=0 0\r\n" +
		"m=video 3434 RTP/AVP 96\r\n" +
		"a=rtpmap:96 H263-1998/90000\r\n" +
		"a=recvonly\r\n" +
		"m=audio 7777 RTP/AVP 100\r\n" +
		"a=rtpmap:100 AMR/8000/1\r\n" +
		"a=FMTP:100 octet-align=1\r\n" +
		"a=inactive\r\n";

	public void testInit() {
		try {
			SessionSpec spec1 = SdpConversor.sdp2SessionSpec(sdp1);
			SessionSpec spec2 = SdpConversor.sdp2SessionSpec(sdp2);

			log.info("---------------------------");
			log.info("SDP1:\n" + sdp1);
			log.info("SessionSpec1:\n" + spec1);
			log.info("Generated SDP1:\n" + SdpConversor.sessionSpec2Sdp(spec1));
			assertEquals(generatedSdp1, SdpConversor.sessionSpec2Sdp(spec1));
			log.info("---------------------------");

			log.info("---------------------------");
			log.info("SDP2:\n" + sdp2);
			log.info("SessionSpec2:\n" + spec2);
			log.info("Generated SDP2:\n" + SdpConversor.sessionSpec2Sdp(spec2));
			assertEquals(generatedSdp2, SdpConversor.sessionSpec2Sdp(spec2));
			log.info("---------------------------");

			SessionSpec[] intersection = SessionSpec.intersect(spec1, spec2);

			log.info("---------------------------");
			log.info("Spected local spec:\n" + sdpIntersectLocal);
			log.info("Intersect local spec:\n" + intersection[0]);
			log.info("Intersect local SDP:\n"
					+ SdpConversor.sessionSpec2Sdp(intersection[0]));
			assertEquals(sdpIntersectLocal,
					SdpConversor.sessionSpec2Sdp(intersection[0]));

			log.info("Spected remote spec:\n" + sdpIntersectRemote);
			log.info("Intersect remote spec:\n" + intersection[1]);
			log.info("Intersect remote SDP:\n"
					+ SdpConversor.sessionSpec2Sdp(intersection[1]));
			assertEquals(sdpIntersectRemote,
					SdpConversor.sessionSpec2Sdp(intersection[1]));
			log.info("---------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
