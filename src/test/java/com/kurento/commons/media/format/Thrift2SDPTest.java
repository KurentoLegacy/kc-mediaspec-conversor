package com.kurento.commons.media.format;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sdp.SdpException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.commons.media.format.conversor.Thrift2MediaSpec;
import com.kurento.commons.mediaspec.Direction;
import com.kurento.commons.mediaspec.MediaSpec;
import com.kurento.commons.mediaspec.MediaType;
import com.kurento.commons.mediaspec.Payload;
import com.kurento.commons.mediaspec.PayloadRtp;
import com.kurento.commons.mediaspec.SessionSpec;
import com.kurento.commons.mediaspec.Transport;
import com.kurento.commons.mediaspec.TransportRtp;

public class Thrift2SDPTest {

	private static SessionSpec spec;
	private static String resultSdp;

	private static final String SESSION_ID = "12345";

	@BeforeClass
	public static void initTest() {
		List<MediaSpec> medias = new ArrayList<MediaSpec>();
			
		{
			List<Payload> payloads = new ArrayList<Payload>();
			Set<MediaType> types = new HashSet<MediaType>();
			Transport transport = new Transport();
			TransportRtp trtp = new TransportRtp("193.147.51.16", 56618);
			Direction mode = Direction.SENDRECV;
			
			Payload pay = new Payload();
			PayloadRtp prtp = new PayloadRtp(96, "H264-1998", 90000);
			pay.setRtp(prtp);
			payloads.add(pay);
			prtp.setBitrate(64);

			types.add(MediaType.VIDEO);
			
			transport.setRtp(trtp);
			MediaSpec spec = new MediaSpec(payloads, types, transport, mode);
			medias.add(spec);
		}
		
		{
			List<Payload> payloads = new ArrayList<Payload>();
			Set<MediaType> types = new HashSet<MediaType>();
			Transport transport = new Transport();
			TransportRtp trtp = new TransportRtp("193.147.51.16", 45900);
			Direction mode = Direction.SENDRECV;

			Payload pay = new Payload();
			PayloadRtp prtp = new PayloadRtp(8, "PCMA", 90000);
			prtp.setBitrate(-1);
			pay.setRtp(prtp);
			payloads.add(pay);

			types.add(MediaType.AUDIO);

			transport.setRtp(trtp);
			MediaSpec spec = new MediaSpec(payloads, types, transport, mode);
			medias.add(spec);
		}

		spec = new SessionSpec(medias, SESSION_ID);
		
		resultSdp = 
				"v=0\r\n" + 
				"o=- 12345 12345 IN IP4 193.147.51.16\r\n" +
				"s=-\r\n" +
				"c=IN IP4 193.147.51.16\r\n" +
				"t=0 0\r\n" +
				"m=video 56618 RTP/AVP 96\r\n" +
				"a=rtpmap:96 H264-1998/90000\r\n" +
				"a=sendrecv\r\n" +
				"b=AS:64\r\n" +
				"m=audio 45900 RTP/AVP 8\r\n" +
				"a=rtpmap:8 PCMA/90000\r\n" +
				"a=sendrecv\r\n";
	}

	@Test
	public void Session2Sdp() throws SdpException {

		Assert.assertEquals(SdpConversor.sessionSpec2Sdp(Thrift2MediaSpec
				.thrift2SessionSpec(spec)), resultSdp);
	}
}
