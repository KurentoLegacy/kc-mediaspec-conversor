package com.kurento.commons.media.format;

import javax.sdp.SdpException;

import junit.framework.TestCase;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.AutoExpandingBufferWriteTransport;

import com.kurento.commons.media.format.conversor.MediaSpec2Thrift;
import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.commons.media.format.conversor.Thrift2MediaSpec;
import com.kurento.commons.mediaspec.SessionSpec;

public class ThriftTest extends TestCase {

	private static String sdp = "v=0\r\n" +
			"o=- 123456 0 IN IP4 193.147.51.16\r\n" +
			"s=TestSession\r\n" +
			"c=IN IP4 193.147.51.16\r\n" +
			"t=0 0\r\n" +
			"m=video 32954 RTP/AVP asdd 96\r\n" +
			"a=rtpmap:96 MP4V-ES/90000\r\n" +
			"a=sendrecv\r\n" +
			"m=audio 47523 RTP/AVP 14\r\n" +
			"a=sendrecv\r\n";

	public void testInit() throws SdpException, TException {
		com.kurento.commons.media.format.SessionSpec spec = SdpConversor
				.sdp2SessionSpec(sdp);
		SessionSpec tspec = MediaSpec2Thrift.thrift2SessionSpec(spec);
		AutoExpandingBufferWriteTransport trans = new AutoExpandingBufferWriteTransport(
				10, 1.0);

		tspec.write(new TSimpleJSONProtocol(trans));
		System.out.println(new String(trans.getBuf().array()));

		spec = Thrift2MediaSpec.thrift2SessionSpec(tspec);
		System.out.println("Spec:\n" + spec);
	}
}
