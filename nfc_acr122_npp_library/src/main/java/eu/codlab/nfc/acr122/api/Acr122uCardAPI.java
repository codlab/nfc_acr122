package eu.codlab.nfc.acr122.api;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import eu.codlab.nfc.acr122.IsmbNppException;
import eu.codlab.nfc.acr122.Util;

public class Acr122uCardAPI {
	private CardChannel _ch;
	private boolean _debugMode;

	public Acr122uCardAPI(CardChannel ch, boolean debugMode){
		_debugMode = debugMode;
		_ch = ch;
	}



	public byte[] transceive_add_header(byte instr, byte[] payload) throws IsmbNppException {
		int payloadLength = (payload != null) ? payload.length : 0;
		byte[] instruction = { (byte) 0xd4, instr };

		//ACR122 header
		byte[] header = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff,
				(byte) (instruction.length + payloadLength) };

		/* construct the command */
		byte[] cmd = Util.appendToByteArray(header, instruction, 0,
				instruction.length);
		cmd = Util.appendToByteArray(cmd, payload);

		return transceive(cmd);
	}

	public byte[] transceive(byte [] cmd) throws IsmbNppException{
		if (_ch == null) {
			throw new IsmbNppException("channel not open");
		}


		try {

			ResponseAPDU r = transceive_apdu(cmd);

			byte[] ra = r.getBytes();


			/* check whether APDU command was accepted by the Controller */
			if (r.getSW1() == 0x63 && r.getSW2() == 0x27) {
				throw new CardException(
						"wrong checksum from contactless response");
			} else if (r.getSW1() == 0x63 && r.getSW2() == 0x7f) {
				throw new CardException("wrong PN53x command");
			} else if (r.getSW1() != 0x90 && r.getSW2() != 0x00) {
				throw new CardException("unknown error");
			}
			return Util.subByteArray(ra, 2, ra.length - 4);
		} catch (CardException e) {
			throw new IsmbNppException("problem with transmitting data");
		}
	}

	private ResponseAPDU transceive_apdu(byte [] cmd) throws CardException{
		CommandAPDU c = new CommandAPDU(cmd);
		if (_debugMode){
			Util.debugAPDUs(cmd, null);
			ResponseAPDU res = _ch.transmit(c);
			Util.debugAPDUs(null, res.getBytes());
			return res;
		}else{
			return _ch.transmit(c);
		}
	}


	/**
	 * LED PART
	 */
	public final static int FINAL_RED_LED_STATE_ON = 1;
	public final static int FINAL_RED_LED_STATE_OFF = 0;
	public final static int FINAL_GREEN_LED_STATE_ON = 1<<1;
	public final static int FINAL_GREEN_LED_STATE_OFF = 0<<1;
	public final static int RED_LED_STATE_MASK_UPDATE = 1<<2;
	public final static int RED_LED_STATE_MASK_NO_CHANGE = 0<<2;
	public final static int GREEN_LED_STATE_MASK_UPDATE = 1<<3;
	public final static int GREEN_LED_STATE_MASK_NO_CHANGE = 0<<3;
	public final static int INITIAL_RED_LED_BLINKING_STATE_ON = 1<<4;
	public final static int INITIAL_RED_LED_BLINKING_STATE_OFF = 0<<4;
	public final static int INITIAL_GREEN_LED_BLINKING_STATE_ON = 1<<5;
	public final static int INITIAL_GREEN_LED_BLINKING_STATE_OFF = 0<<5;
	public final static int RED_LED_BLINKING_MASK_BLINK = 1<<6;
	public final static int RED_LED_BLINKING_MASK_NOT_BLINK = 0<<6;
	public final static int GREEN_LED_BLINKING_MASK_BLINK = 1<<7;
	public final static int GREEN_LED_BLINKING_MASK_NOT_BLINK = 0<<7;

	public final static byte BUZZER_NOT_TURN_ON = 0x00;
	public final static byte BUZZER_WIL_TURN_ON_DURING_T1 = 0x01;
	public final static byte BUZZER_WIL_TURN_ON_DURING_T2 = 0x02;
	public final static byte BUZZER_WIL_TURN_ON_DURING_T1_T2 = 0x03;

	/**
	 * 
	 * @param led_state_control_mask
	 * @param duration_initial_bliking
	 * @param duration_toggle_blinking
	 * @param number_repetition
	 * @param link_to_buzzer
	 * @return
	 */
	public byte [] setLED(int led_state_control_mask,
			byte duration_initial_bliking,
			byte duration_toggle_blinking,
			byte number_repetition,
			byte link_to_buzzer){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x40, (byte) (led_state_control_mask % 256),
				(byte)0x04,
				(byte)duration_initial_bliking,
				(byte)duration_toggle_blinking,
				(byte)number_repetition,
				(byte)link_to_buzzer};
		return cmd;
	}

	public String getFirmwareVersion(){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x48, (byte) 0x00, (byte) 0x0A};
		/* construct the command */
		byte [] res = new byte[10];
		try {
			res = transceive_apdu(cmd).getData();
		} catch (CardException e) {
			e.printStackTrace();
		}
		String str = new String(res);
		return str;
	}
	/**
	 * @deprecated
	 * @param response
	 * @return
	 */
	public boolean isSuccess(ResponseAPDU response){
		return response.getSW1() == 0x61;
	}

	public int getResponseLength(ResponseAPDU response){
		return response.getSW2();
	}


	public void turnAntennaOn(){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x04, (byte) 0xD4, (byte) 0x32, (byte) 0x01, (byte) 0x01};
		try {
			transceive_apdu(cmd).getData();
		} catch (CardException e) {
			e.printStackTrace();
		}
	}

	public void turnAntennaOff(){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x04, (byte) 0xD4, (byte) 0x32, (byte) 0x01, (byte) 0x00};
		try {
			transceive_apdu(cmd).getData();
		} catch (CardException e) {
			e.printStackTrace();
		}
	}
	
	public byte getPICCOperatingMode(){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x50, (byte) 0x00, (byte) 0x00};
		byte r=0x00;
		try {
			r = transceive_apdu(cmd).getData()[0];
		} catch (CardException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	/**
	 * 
	 * @param auto_picc_polling true = enable
	 * @param auto_ats_generation true = enable
	 * @param polling_interval true = 250ms false = 500ms
	 * @param felica_424k true = detect / false = skip
	 * @param felica_212k true = detect / false = skip
	 * @param topaz true = detect / false = skip
	 * @param iso_14443_typeB true = detect / false = skip
	 * @param iso_14443_typeA true = detect / false = skip
	 */
	public byte setPICCOperatingMode(boolean auto_picc_polling, 
			boolean auto_ats_generation,
			boolean polling_interval,
			boolean felica_424k,
			boolean felica_212k,
			boolean topaz,
			boolean iso_14443_typeB,
			boolean iso_14443_typeA){
		byte r = 0x00;
		r |= auto_picc_polling ? 0x01 : 0x00;
		r |= auto_ats_generation ? 1<<1 : 0;
		r |= polling_interval ? 1<<2 : 0;
		r |= felica_424k ? 1<<3 : 0;
		r |= felica_212k ? 1<<4 : 0;
		r |= topaz ? 1<<5 : 0;
		r |= iso_14443_typeB ? 1<<6 : 0;
		r |= iso_14443_typeA ? 1<<7 : 0;
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x51, r, (byte) 0x00};
		try {
			r = transceive_apdu(cmd).getData()[0];
		} catch (CardException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	/**
	 * 
	 * if value = 0x00 > no check
	 * if value = 0xFF > wait until having a response from card
	 * else timeout = 5 * value in seconds
	 * 
	 * @param value
	 * @return
	 */
	public boolean setTimeout(byte value){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x41, value, (byte) 0x00};
		try {
			ResponseAPDU response = transceive_apdu(cmd);
			if(response.getSW1() == 0x90 && response.getSW2() == 0x00)
				return true;
		} catch (CardException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public byte[] setBuzzerState(boolean activate){
		byte[] cmd = { (byte) 0xff, (byte) 0x00, (byte) 0x52, (byte) (activate ? 0xFF : 0x00), (byte) 0x00};
		return cmd;
	}
}
