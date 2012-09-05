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

import javax.sdp.SdpException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mediaspec.SessionSpecUtils;

public class Sdp2SessionSpecTest extends TestCase {

	static Logger log = LoggerFactory.getLogger(Sdp2SessionSpecTest.class);
	private static Object assertObject = new Object();

	private static String sdp = "v=0\r\n" +
			"o=- 123456 0 IN IP4 193.147.51.16\r\n" +
			"s=TestSession\r\n" +
			"c=IN IP4 193.147.51.16\r\n" +
			"t=0 0\r\n" +
			"m=video 32954 RTP/AVP 96\r\n" +
			"a=rtpmap:96 MP4V-ES/90000\r\n" +
			"a=sendrecv\r\n" +
			"m=audio 47523 RTP/AVP 14\r\n" +
			"a=sendrecv\r\n";

	private static String sdp2 = "v=0\r\n" +
			"o=alice 2890844526 2890844526 IN IP4 host.atlanta.example.com\r\n" +
			"s=\r\n" +
			"c=IN IP4 host.atlanta.example.com\r\n" +
			"t=0 0\r\n" +
			"m=audio 49170 RTP/AVP 0 8 97\r\n" +
			"a=rtpmap:0 PCMU/8000\r\n" +
			"a=rtpmap:8 PCMA/8000\r\n" +
			"a=rtpmap:97 iLBC/8000\r\n" +
			"m=video 51372 RTP/AVP 31 32 106\r\n" +
			"a=rtpmap:106 H263-1998/90000\r\n" +
			"a=rtpmap:31 H261/90000\r\n" +
			"a=fmtp:31 QCIF=1;CIF=1\r\n" +
			"a=rtpmap:32 MPV/90000\r\n";

	private static String sdp3 = "v=0\r\n" +
			"o=jcadenlin 123456 654321 IN IP4 193.147.51.16\r\n" +
			"s=A conversation\r\n" +
			"c=IN IP4 193.147.51.16\r\n" +
			"t=0 0\r\n" +
			"m=audio 7078 RTP/AVP 8 101\r\n" +
			"b=AS:256\r\n" +
			"a=rtpmap:8 PCMA/8000/1\r\n" +
			"a=rtpmap:101 telephone-event/8000/1\r\n" +
			"a=fmtp:101 0-11\r\n" +
			"m=video 9078 RTP/AVP 98\r\n" +
			"a=rtpmap:98 H263-1998/90000\r\n" +
			"a=fmtp:98 CIF=1;QCIF=1\r\n";

	private static String sdp4 = "v=0\r\n" +
			"o=jdoe 2890844526 2890842807 IN IP4 10.0.1.1\r\n" +
			"s=\r\n" +
			"c=IN IP4 192.0.2.3\r\n" +
			"t=0 0\r\n" +
			"a=ice-pwd:asd88fgpdd777uzjYhagZg\r\n" +
			"a=ice-ufrag:8hhY\r\n" +
			"m=audio 45664 RTP/AVP 0\r\n" +
			"b=RS:0\r\n" +
			"b=RR:0\r\n" +
			"a=rtpmap:0 PCMU/8000\r\n" +
			"a=candidate:1 1 UDP 2130706431 10.0.1.1 8998 typ host\r\n" +
			"a=candidate:2 1 UDP 1694498815 192.0.2.3 45664 typ srflx " +
			"raddr 10.0.1.1 rport 8998\r\n";

	public void testInit() {
		try {
			SessionSpec spec = SdpConversor.sdp2SessionSpec(sdp);
			spec = SdpConversor.sdp2SessionSpec(sdp);
			log.info("Sdp:\n" + sdp);
			log.info("SessionSpec:\n" + spec);
			log.info("Generated:\n" + SdpConversor.sessionSpec2Sdp(spec));
			log.info("---------------------------");
			spec = SdpConversor.sdp2SessionSpec(sdp2);
			log.info("Sdp:\n" + sdp2);
			log.info("SessionSpec:\n" + spec);
			log.info("Generated:\n" + SdpConversor.sessionSpec2Sdp(spec));
			log.info("---------------------------");
			spec = SdpConversor.sdp2SessionSpec(sdp3);
			log.info("Sdp:\n" + sdp3);
			log.info("SessionSpec:\n" + spec);
			log.info("Generated:\n" + SdpConversor.sessionSpec2Sdp(spec));

			SessionSpec spec0 = SdpConversor.sdp2SessionSpec(sdp2);
			SessionSpec spec1 = spec;
			SessionSpec[] merge = SessionSpecUtils.intersect(spec0, spec1);
			log.info("Session0:\n" + spec0);
			log.info("Session1:\n" + spec1 + "\n\n");

			log.info("Merge0:\n" + merge[0]);
			log.info("Merge1:\n" + merge[1]);

			try {
				log.info("Merge0SDP:\n"
						+ SdpConversor.sessionSpec2Sdp(merge[0]));
			} catch (SdpException e) {
				log.info("Merge0SDP:\nerror");
			}
			try {
				log.info("Merge1SDP:\n"
						+ SdpConversor.sessionSpec2Sdp(merge[1]));
			} catch (SdpException e) {
				log.info("Merge1SDP:\nerror");
			}

			spec = SdpConversor.sdp2SessionSpec(sdp4);
			String generated;
			log.info("Sdp:\n" + sdp4);
			log.info("SessionSpec:\n" + spec);
			generated = SdpConversor.sessionSpec2Sdp(spec);
			log.info("Generated:\n" + generated);
			SessionSpec spec2 = SdpConversor.sdp2SessionSpec(generated);
			log.info("SessionSpec2:\n" + spec2);
			assertEquals(spec, spec2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
