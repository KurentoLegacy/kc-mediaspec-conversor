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

	private static String sdp5 = "v=0\r\n" +
			"o=- 2913691721 2 IN IP4 127.0.0.1\r\n" +
			"s=\r\n" +
			"t=0 0\r\n" +
			"a=group:BUNDLE audio video\r\n" +
			"m=audio 43758 RTP/SAVPF 103 104 0 8 106 105 13 126\r\n" +
			"c=IN IP4 193.147.51.16\r\n" +
			"a=rtcp:1 IN IP4 0.0.0.0\r\n" +
			"a=candidate:2023387037 1 udp 2130714367 193.147.51.16 43339 typ host generation 0\r\n" +
			"a=candidate:4191316265 1 udp 1912610559 193.147.51.16 43758 typ srflx generation 0\r\n" +
			"a=candidate:907645805 1 tcp 1694506751 193.147.51.16 50782 typ host generation 0\r\n" +
			"a=ice-ufrag:Xv/1NJ8ftUdyQP3/\r\n" +
			"a=ice-pwd:fdl4twuPPWbw1O6B7RviQl8j\r\n" +
			"a=sendrecv\r\n" +
			"a=mid:audio\r\n" +
			"a=rtcp-mux\r\n" +
			"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:bBrIQP9mEN1ieWXIK0SWf2LxACcXAtdVJJi6++EL\r\n" +
			"a=rtpmap:103 ISAC/16000\r\n" +
			"a=rtpmap:104 ISAC/32000\r\n" +
			"a=rtpmap:0 PCMU/8000\r\n" +
			"a=rtpmap:8 PCMA/8000\r\n" +
			"a=rtpmap:106 CN/32000\r\n" +
			"a=rtpmap:105 CN/16000\r\n" +
			"a=rtpmap:13 CN/8000\r\n" +
			"a=rtpmap:126 telephone-event/8000\r\n" +
			"a=ssrc:3689591462 cname:6feIHETBbPgUvli9\r\n" +
			"a=ssrc:3689591462 mslabel:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V\r\n" +
			"a=ssrc:3689591462 label:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V00\r\n" +
			"m=video 43758 RTP/SAVPF 100 101 102\r\n" +
			"c=IN IP4 193.147.51.16\r\n" +
			"a=rtcp:1 IN IP4 0.0.0.0\r\n" +
			"a=candidate:2023387037 1 udp 2130714367 193.147.51.16 43339 typ host generation 0\r\n" +
			"a=candidate:4191316265 1 udp 1912610559 193.147.51.16 43758 typ srflx generation 0\r\n" +
			"a=candidate:907645805 1 tcp 1694506751 193.147.51.16 50782 typ host generation 0\r\n" +
			"a=ice-ufrag:Xv/1NJ8ftUdyQP3/\r\n" +
			"a=ice-pwd:fdl4twuPPWbw1O6B7RviQl8j\r\n" +
			"a=sendrecv\r\n" +
			"a=mid:video\r\n" +
			"a=rtcp-mux\r\n" +
			"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:bBrIQP9mEN1ieWXIK0SWf2LxACcXAtdVJJi6++EL\r\n" +
			"a=rtpmap:100 VP8/90000\r\n" +
			"a=rtpmap:101 red/90000\r\n" +
			"a=rtpmap:102 ulpfec/90000\r\n" +
			"a=ssrc:118467033 cname:6feIHETBbPgUvli9\r\n" +
			"a=ssrc:118467033 mslabel:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V\r\n" +
			"a=ssrc:118467033 label:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V10\r\n";

	private static String sdp6 = "v=0\r\n" +
			"o=- 2913691721 2 IN IP4 127.0.0.1\r\n" +
			"s=\r\n" +
			"t=0 0\r\n" +
			"a=group:BUNDLE audio video\r\n" +
			"m=audio 43758 RTP/SAVPF 103 104 0 8 106 105 13 126\r\n" +
			"c=IN IP4 193.147.51.16\r\n" +
			"a=rtcp:1 IN IP4 0.0.0.0\r\n" +
			"a=candidate:2023387037 1 udp 2130714367 193.147.51.16 43339 typ host generation 0\r\n" +
			"a=candidate:4191316265 1 udp 1912610559 193.147.51.16 43758 typ srflx generation 0\r\n" +
			"a=candidate:907645805 1 tcp 1694506751 193.147.51.16 50782 typ host generation 0\r\n" +
			"a=ice-ufrag:Xv/1NJ8ftUdyQP3/\r\n" +
			"a=ice-pwd:1O6B7RviQl8jfdl4twuPPWbw\r\n" +
			"a=sendrecv\r\n" +
			"a=mid:audio\r\n" +
			"a=rtcp-mux\r\n" +
			"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:bBrIQP9mEN1ieWXIK0SWf2LxACcXAtdVJJi6++EL\r\n" +
			"a=rtpmap:103 ISAC/16000\r\n" +
			"a=rtpmap:104 ISAC/32000\r\n" +
			"a=rtpmap:0 PCMU/8000\r\n" +
			"a=rtpmap:8 PCMA/8000\r\n" +
			"a=rtpmap:106 CN/32000\r\n" +
			"a=rtpmap:105 CN/16000\r\n" +
			"a=rtpmap:13 CN/8000\r\n" +
			"a=rtpmap:126 telephone-event/8000\r\n" +
			"a=ssrc:3689591462 cname:6feIHETBbPgUvli9\r\n" +
			"a=ssrc:3689591462 mslabel:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V\r\n" +
			"a=ssrc:3689591462 label:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V00\r\n" +
			"m=video 43758 RTP/SAVPF 100 101 102\r\n" +
			"c=IN IP4 193.147.51.19\r\n" +
			"a=rtcp:1 IN IP4 0.0.0.0\r\n" +
			"a=candidate:2023387037 1 udp 2130714367 193.147.51.19 43339 typ host generation 0\r\n" +
			"a=candidate:4191316265 1 udp 1912610559 193.147.51.19 43758 typ srflx generation 0\r\n" +
			"a=candidate:907645805 1 tcp 1694506751 193.147.51.19 50782 typ host generation 0\r\n" +
			"a=ice-ufrag:Xv/1NJ8ftUdyQP3/\r\n" +
			"a=ice-pwd:fdl4twuPPWbw1O6B7RviQl8j\r\n" +
			"a=sendrecv\r\n" +
			"a=mid:video\r\n" +
			"a=rtcp-mux\r\n" +
			"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:bBrIQP9mEN1ieWXIK0SWf2LxACcXAtdVJJi6++EL\r\n" +
			"a=rtpmap:100 VP8/90000\r\n" +
			"a=rtpmap:101 red/90000\r\n" +
			"a=rtpmap:102 ulpfec/90000\r\n" +
			"a=ssrc:118467033 cname:6feIHETBbPgUvli9\r\n" +
			"a=ssrc:118467033 mslabel:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V\r\n" +
			"a=ssrc:118467033 label:3k46NcmVwsU29O6aV2AJyeej3BIPCl4pQ70V10\r\n";

	private static String sdp7 = "v=0\r\n" +
			"o=- 12345 12345 IN IP4 89.131.152.18\r\n" +
			"s=-\r\n" +
			"c=IN IP4 89.131.152.18\r\n" +
			"t=0 0\r\n" +
			"m=video 45723 RTP/AVP 96\r\n" +
			"a=rtpmap:96 MP4V-ES/90000\r\n" +
			"a=extra-attr:96 w=352;h=288;fr=15/1\r\n" +
			"a=sendrecv\r\n" +
			"b=AS:288\r\n" +
			"m=audio 41496 RTP/AVP 97\r\n" +
			"a=rtpmap:97 AMR/8000/1\r\n" +
			"a=FMTP:97 octet-align=1\r\n" +
			"a=sendrecv\r\n" +
			"b=AS:13\r\r";

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

			log.info("Sdp:\n" + sdp5);
			spec = SdpConversor.sdp2SessionSpec(sdp5);
			log.info("SessionSpec:\n" + spec);
			generated = SdpConversor.sessionSpec2Sdp(spec);
			log.info("Generated:\n" + generated);
			spec2 = SdpConversor.sdp2SessionSpec(generated);
			log.info("SessionSpec2:\n" + spec2);
			assertEquals(spec, spec2);

			log.info("Sdp:\n" + sdp6);
			spec = SdpConversor.sdp2SessionSpec(sdp6);
			log.info("SessionSpec:\n" + spec);
			generated = SdpConversor.sessionSpec2Sdp(spec);
			log.info("Generated:\n" + generated);
			spec2 = SdpConversor.sdp2SessionSpec(generated);
			log.info("SessionSpec2:\n" + spec2);
			assertEquals(spec, spec2);

			log.info("Sdp:\n" + sdp7);
			spec = SdpConversor.sdp2SessionSpec(sdp7);
			log.info("SessionSpec:\n" + spec);
			generated = SdpConversor.sessionSpec2Sdp(spec);
			log.info("Generated:\n" + generated);
			spec2 = SdpConversor.sdp2SessionSpec(generated);
			log.info("SessionSpec2:\n" + spec2);
			assertEquals(spec, spec2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
