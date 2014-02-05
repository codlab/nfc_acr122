package org.nfctools.examples.hce;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.nfctools.api.TagType;
import org.nfctools.scio.TerminalStatus;
import org.nfctools.spi.acs.AbstractTerminalTagScanner;
import org.nfctools.spi.acs.ApduTagReaderWriter;

public class HostCardEmulationTagScanner extends AbstractTerminalTagScanner {
    private IOnData _listener;

	protected HostCardEmulationTagScanner(CardTerminal cardTerminal, IOnData listener) {
		super(cardTerminal);
        _listener = listener;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			notifyStatus(TerminalStatus.WAITING);
			try {
                _listener.onError("Waiting for card...");

                //cardTerminal.waitForCardPresent(30000);
                cardTerminal.waitForCardPresent(10000);
				Card card = null;
				try{
					card = cardTerminal.connect("direct");
				}catch(Exception e){
                    //e.printStackTrace();
					card = cardTerminal.connect("*");
				}
				ApduTagReaderWriter readerWriter = new ApduTagReaderWriter(new AcsDirectChannelTag(TagType.ISO_DEP,
						null, card));
				try {
					IsoDepTamaCommunicator tamaCommunicator = new IsoDepTamaCommunicator(readerWriter, readerWriter);
					tamaCommunicator.connectAsInitiator(_listener);
				}
				catch (Exception e1) {
					card.disconnect(true);
					//e1.printStackTrace();
					try {
						Thread.sleep(1000);
					}
					catch (InterruptedException e) {
						//break;
					}
				}
				finally {
					waitForCardAbsent();
				}
			}
			catch (CardException e) {
				e.printStackTrace();
				//break;
			}
		}
	}
}
