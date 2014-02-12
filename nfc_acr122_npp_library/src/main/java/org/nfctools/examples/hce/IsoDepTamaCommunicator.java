package org.nfctools.examples.hce;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.nfctools.io.ByteArrayReader;
import org.nfctools.io.ByteArrayWriter;
import org.nfctools.spi.tama.AbstractTamaCommunicator;
import org.nfctools.spi.tama.request.DataExchangeReq;
import org.nfctools.spi.tama.request.InListPassiveTargetReq;
import org.nfctools.spi.tama.request.InListPassiveTargetReqPoll;
import org.nfctools.spi.tama.response.DataExchangeResp;
import org.nfctools.spi.tama.response.InListPassiveTargetResp;
import org.nfctools.spi.tama.response.InListPassiveTargetRespPoll;
import org.nfctools.utils.NfcUtils;

import eu.codlab.nfc.acr122.api.Acr122uCardAPI;

public class IsoDepTamaCommunicator extends AbstractTamaCommunicator {
	private int messageCounter = 0;
	private static final byte[] CLA_INS_P1_P2 = { 0x00, (byte)0xA4, 0x04, 0x00 };
	private static final byte[] AID_ANDROID = { (byte)0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };

	public IsoDepTamaCommunicator(ByteArrayReader reader, ByteArrayWriter writer) {
		super(reader, writer);
	}

	private byte[] createSelectAidApdu(byte[] aid) {
		byte[] result = new byte[6 + aid.length];
		System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
		result[4] = (byte)aid.length;
		System.arraycopy(aid, 0, result, 5, aid.length);
		result[result.length - 1] = 0;
		return result;
	}

	public void connectAsInitiator(IOnData listener) throws IOException {
        boolean ok = false;
		while (!ok) {
            System.out.println("target2 ");

            //on some acr122u devices, the command needed here is
			//InListPassiveTargetResp inListPassiveTargetResp = sendMessage(new InListPassiveTargetReq((byte)1, (byte)0,
			//		new byte[0]));

            //here is the fix for the cjurrent acr122 >
            //TODO codlab, add switch for auto data !

            InListPassiveTargetResp in2 = sendMessage(new InListPassiveTargetReq((byte)1, (byte)0,
                    new byte[10]));
            InListPassiveTargetRespPoll inListPassiveTargetResp = sendMessage(new InListPassiveTargetReqPoll((byte)1, (byte)0,
                    new byte[10]));
            System.out.println("target2 "+inListPassiveTargetResp.getNumberOfTargets()+" "+in2.getNumberOfTargets());


            Acr122uCardAPI card = new Acr122uCardAPI(null,true);


			if (inListPassiveTargetResp.getNumberOfTargets() > 0 || in2.getNumberOfTargets() > 0) {
                System.out.println("Having UID "+inListPassiveTargetResp.getUID());
				log.info("TargetData: " + NfcUtils.convertBinToASCII(inListPassiveTargetResp.getTargetData()));
				if (inListPassiveTargetResp.isIsoDepSupported()) {
                    System.out.println("IsoDep Supported");
					byte[] selectAidApdu = createSelectAidApdu(AID_ANDROID);
					DataExchangeResp resp = sendMessage(new DataExchangeReq(inListPassiveTargetResp.getTargetId(),
							false, selectAidApdu, 0, selectAidApdu.length));
					String dataIn = new String(resp.getDataOut());
					System.out.println("Received: " + dataIn);
					if (dataIn.startsWith("Hello")) {
						String result = exchangeDataToken(inListPassiveTargetResp);
                        if(result != null){
                            listener.onUserToken(result);
                            ok=true;
                        }else{
                            listener.onError("Invalid response");
                            ok=true;
                        }
                        break;
					}
				}
				else {
                    //here start browser with value
                    listener.onUUId(inListPassiveTargetResp.getUID());
                    ok=true;
                    /*if(Desktop.isDesktopSupported()){
                        try {
                            Desktop.getDesktop().browse(new URI("http://codlab.eu/nfc/uid/"+inListPassiveTargetResp.getUID()));
                            break;
                        } catch (URISyntaxException e) {
                        }
                    }*/
                    System.out.println("IsoDep NOT Supported");
				}
				break;
			}
			else {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

            System.out.println("Quit");
		}
	}

    private void exchangeData(InListPassiveTargetResp inListPassiveTargetResp) throws IOException {
        DataExchangeResp resp;
        String dataIn;
        while (true) {
            byte[] dataOut = ("Message from desktop: " + messageCounter++).getBytes();
            resp = sendMessage(new DataExchangeReq(inListPassiveTargetResp.getTargetId(), false, dataOut, 0,
                    dataOut.length));
            dataIn = new String(resp.getDataOut());
            System.out.println("Received: " + dataIn);
        }
    }


    private void exchangeData(InListPassiveTargetRespPoll inListPassiveTargetResp) throws IOException {
        DataExchangeResp resp;
        String dataIn;
        while (true) {
            byte[] dataOut = ("Message from desktop: " + messageCounter++).getBytes();
            resp = sendMessage(new DataExchangeReq(inListPassiveTargetResp.getTargetId(), false, dataOut, 0,
                    dataOut.length));
            dataIn = new String(resp.getDataOut());

            //here start browser if found
            if(dataIn.indexOf("http://") == 0){
                if(Desktop.isDesktopSupported())
                    try {
                        Desktop.getDesktop().browse(new URI(dataIn));
                        break;
                    } catch (URISyntaxException e) {
                        System.out.println("Received: " + dataIn);
                    }
                else
                    System.out.println("Received: " + dataIn);
            }else{
                System.out.println("Received: " + dataIn);
            }
        }
        System.out.println("Quit reading");
    }

	private String exchangeDataToken(InListPassiveTargetRespPoll inListPassiveTargetResp) throws IOException {
		DataExchangeResp resp;
		String dataIn;
        int test = 10;
        boolean ok = false;
		while (test > 0 && !ok) {
			byte[] dataOut = ("Message from desktop: " + messageCounter++).getBytes();
			resp = sendMessage(new DataExchangeReq(inListPassiveTargetResp.getTargetId(), false, dataOut, 0,
					dataOut.length));
			dataIn = new String(resp.getDataOut());

            //here start browser if found
            if(dataIn.indexOf("http://") == 0){
                ok = true;
                return dataIn;
            }else{
                System.out.println("Received: " + dataIn);
            }
            test--;
		}
        System.out.println("Quit reading");
        return null;
	}
}
