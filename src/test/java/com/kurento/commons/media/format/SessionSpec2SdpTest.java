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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sdp.SdpException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.mediaspec.Fraction;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mediaspec.Transport;
import com.kurento.mediaspec.TransportRtp;

public class SessionSpec2SdpTest extends TestCase {

	static Logger log = LoggerFactory.getLogger(SessionSpec2SdpTest.class);

	public void test1() throws SdpException {
		log.debug("--------------------------- init test1 ---------------------------");

		String sdpExpected = "v=0\r\n" + "o=- 1234 12345 IN IP4 localhost\r\n"
				+ "s=-\r\n" + "c=IN IP4 localhost\r\n" + "t=0 0\r\n"
				+ "m=video 2323 RTP/AVP 96\r\n"
				+ "a=rtpmap:96 MP4V-ES/90000\r\n"
				+ "a=extra-attr:96 w=352;h=288;fr=15/1\r\n" + "a=sendrecv\r\n"
				+ "b=AS:384\r\n";

		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		{
			List<Payload> payloads = new ArrayList<Payload>();
			Set<MediaType> types = new HashSet<MediaType>();
			Transport transport = new Transport();
			TransportRtp trtp = new TransportRtp("localhost", 2323);
			com.kurento.mediaspec.Direction mode = com.kurento.mediaspec.Direction.SENDRECV;

			Payload pay = new Payload();
			PayloadRtp prtp = new PayloadRtp(96, "MP4V-ES", 90000);
			prtp.setWidth(352);
			prtp.setHeight(288);
			prtp.setFramerate(new Fraction(15, 1));
			prtp.setBitrate(384);
			pay.setRtp(prtp);
			payloads.add(pay);

			types.add(MediaType.VIDEO);

			transport.setRtp(trtp);
			MediaSpec spec = new MediaSpec(payloads, types, transport, mode);
			medias.add(spec);
		}

		SessionSpec ssExpected = new SessionSpec(medias, "1234");

		String sdp = SdpConversor.sessionSpec2Sdp(ssExpected);
		log.info(sdp);
		assertEquals(sdpExpected, sdp);

		SessionSpec ss = SdpConversor.sdp2SessionSpec(sdp);
		log.info(ss.toString());
		assertEquals(ssExpected, ss);

		log.debug("--------------------------- finish test1 ---------------------------");
	}

	public void test2() throws SdpException {
		log.debug("--------------------------- init test2 ---------------------------");

		String sdpExpected = "v=0\r\n" + "o=- 1234 12345 IN IP4 localhost\r\n"
				+ "s=-\r\n" + "c=IN IP4 localhost\r\n" + "t=0 0\r\n"
				+ "m=video 2323 RTP/AVP 96\r\n"
				+ "a=rtpmap:96 MP4V-ES/90000\r\n"
				+ "a=extra-attr:96 w=352;h=288\r\n" + "a=sendrecv\r\n"
				+ "b=AS:384\r\n";

		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		{
			List<Payload> payloads = new ArrayList<Payload>();
			Set<MediaType> types = new HashSet<MediaType>();
			Transport transport = new Transport();
			TransportRtp trtp = new TransportRtp("localhost", 2323);
			com.kurento.mediaspec.Direction mode = com.kurento.mediaspec.Direction.SENDRECV;

			Payload pay = new Payload();
			PayloadRtp prtp = new PayloadRtp(96, "MP4V-ES", 90000);
			prtp.setWidth(352);
			prtp.setHeight(288);
			prtp.setBitrate(384);
			pay.setRtp(prtp);
			payloads.add(pay);

			types.add(MediaType.VIDEO);

			transport.setRtp(trtp);
			MediaSpec spec = new MediaSpec(payloads, types, transport, mode);
			medias.add(spec);
		}

		SessionSpec ssExpected = new SessionSpec(medias, "1234");

		String sdp = SdpConversor.sessionSpec2Sdp(ssExpected);
		log.info(sdp);
		assertEquals(sdpExpected, sdp);

		SessionSpec ss = SdpConversor.sdp2SessionSpec(sdp);
		log.info(ss.toString());
		assertEquals(ssExpected, ss);

		log.debug("--------------------------- finish test2 ---------------------------");
	}

	public void test3() throws SdpException {
		log.debug("--------------------------- init test3 ---------------------------");

		String sdpExpected = "v=0\r\n" + "o=- 1234 12345 IN IP4 localhost\r\n"
				+ "s=-\r\n" + "c=IN IP4 localhost\r\n" + "t=0 0\r\n"
				+ "m=video 2323 RTP/AVP 96\r\n"
				+ "a=rtpmap:96 MP4V-ES/90000\r\n" + "a=sendrecv\r\n"
				+ "m=audio 3434 RTP/AVP 14 97\r\n"
				+ "a=rtpmap:14 MPA/90000\r\n" + "a=rtpmap:97 AMR/8000\r\n"
				+ "a=FMTP:97 octet-align=1\r\n" + "a=sendrecv\r\n";

		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		{
			List<Payload> payloads = new ArrayList<Payload>();
			Set<MediaType> types = new HashSet<MediaType>();
			Transport transport = new Transport();
			TransportRtp trtp = new TransportRtp("localhost", 2323);
			com.kurento.mediaspec.Direction mode = com.kurento.mediaspec.Direction.SENDRECV;

			Payload pay = new Payload();
			PayloadRtp prtp = new PayloadRtp(96, "MP4V-ES", 90000);
			pay.setRtp(prtp);
			payloads.add(pay);

			types.add(MediaType.VIDEO);

			transport.setRtp(trtp);
			MediaSpec spec = new MediaSpec(payloads, types, transport, mode);
			medias.add(spec);
		}

		{
			List<Payload> payloads = new ArrayList<Payload>();
			Set<MediaType> types = new HashSet<MediaType>();
			Transport transport = new Transport();
			TransportRtp trtp = new TransportRtp("localhost", 3434);
			com.kurento.mediaspec.Direction mode = com.kurento.mediaspec.Direction.SENDRECV;
			{
				Payload pay = new Payload();
				PayloadRtp prtp = new PayloadRtp(14, "MPA", 90000);
				pay.setRtp(prtp);
				payloads.add(pay);
			}
			{
				Payload pay = new Payload();
				PayloadRtp prtp = new PayloadRtp(97, "AMR", 8000);
				prtp.putToExtraParams("octet-align", "1");
				pay.setRtp(prtp);
				payloads.add(pay);
			}
			types.add(MediaType.AUDIO);

			transport.setRtp(trtp);
			MediaSpec spec = new MediaSpec(payloads, types, transport, mode);
			medias.add(spec);
		}

		SessionSpec ssExpected = new SessionSpec(medias, "1234");

		String sdp = SdpConversor.sessionSpec2Sdp(ssExpected);
		log.info(sdp);
		assertEquals(sdpExpected, sdp);

		SessionSpec ss = SdpConversor.sdp2SessionSpec(sdp);
		log.info(ss.toString());
		assertEquals(ssExpected, ss);

		log.debug("--------------------------- finish test3 ---------------------------");
	}

}
