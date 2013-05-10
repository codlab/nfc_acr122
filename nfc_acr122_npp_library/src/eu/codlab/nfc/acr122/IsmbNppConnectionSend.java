/*
 * IsmbNppConnection
 * 
 * Copyright (C) 2011  Antonio Lotito <lotito@ismb.it>
 * Modification by (C) 2013 codlab - Kevin Le Perf
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


package eu.codlab.nfc.acr122;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import eu.codlab.nfc.acr122.api.Acr122uAPI;
import eu.codlab.nfc.stack.NdefMessage;

@SuppressWarnings("restriction")
public class IsmbNppConnectionSend {

	private Acr122uAPI _api;
	
	private final static int BLOCK_SIZE = 250;

	public final static int INITIATOR = 0;
	public final static int TARGET = 1;

	private final static int RECEIVE = 0;
	private final static int SEND = 1;

	//USEFUL APDU COMMANDS 
	private final static byte TG_GET_DATA = (byte) 0x86;
	private final static byte TG_INIT_AS_TARGET = (byte) 0x8c;
	private final static byte TG_SET_DATA = (byte) 0x8e;
	private final static byte IN_RELEASE = (byte) 0x52;
	private final static byte RFCONFIGURATION = (byte)0x32;

	//Mode of LLCP connection (always target)
	private int mode=TARGET;
	private int transmissionMode = RECEIVE;;

	private byte[] receiveBuffer;
	private CardTerminal terminal;
	private CardChannel ch;

	//  Enable debugMode to print info about the communication
	private boolean debugMode = false;
	
	//message to send during this session
	private NdefMessage _message;

	/**
	 * Initialize NPP Connection
	 *
	 * @param t
	 *            a valid card terminal
	 *
	 * @throws IsmbNppException
	 *             if the terminal is incorrect
	 */
	public IsmbNppConnectionSend(CardTerminal t, NdefMessage message) throws IsmbNppException {
		if (t == null) {
			throw new IsmbNppException("invalid card terminal");
		}
		terminal = t;
		_message = message;
		Card card;
		try {
			if (terminal.waitForCardPresent(30000)) {
				card = terminal.connect("*");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ch = card.getBasicChannel();
				System.out.println(card.getProtocol());   
				_api= new Acr122uAPI(t, ch, true);
				_api.setBuzzerState(false);
				System.out.println("firmware = "+_api.getFirmwareVersion());
			} 
			else {
				throw new IsmbNppException("Device not supported, only ACS ACR122 is supported now");
			}
		} catch (CardException e) { throw new IsmbNppException("problem with connecting to reader");}       
	}

	//Set debug mode
	public void setDebugMode() {
		debugMode = true;
	}

	//Unset debug mode
	public void unsetDebugMode() {
		debugMode = false;
	}

	/**
	 * Send data to initiator 
	 *
	 * @param data
	 * @throws IsmbNppException
	 */
	public void send(byte[] data) throws IsmbNppException {
		if (transmissionMode != SEND)
			throw new IsmbNppException("expected receive");
		int dataLength = (data != null) ? data.length : 0;
		int blockSize = BLOCK_SIZE - 1;
		byte[] sendBuffer;
		int offset = 0;

		while (dataLength > blockSize) {
			sendBuffer = new byte[blockSize];
			System.arraycopy(data, offset, sendBuffer, 0, blockSize);
			/* send one block with moreData = true */
			sendData(sendBuffer, true);
			getData();
			offset += blockSize;
			dataLength -= blockSize;
		}
		sendBuffer = new byte[dataLength];
		if (dataLength > 0)
			System.arraycopy(data, offset, sendBuffer, 0, dataLength);
		/* send the last block */
		//byte[] result = sendData(sendBuffer, false);
		sendData(sendBuffer, false);
		transmissionMode = RECEIVE;
	}

	/**
	 * Low level sending function, the amount of data is limited
	 *  by BLOCK_SIZE and by hardware limit
	 *
	 * @param data
	 *            the block to send
	 * @param moreData
	 *           if other data block available (true =
	 *            yes, false = no) this sets the wrapping byte
	 * @return a byte indicating if the result consists of more than just this block
	 * 		   (0x00 = no, 0x01 = yes) the result is only returned when this method was
	 *         called with <em>moreData</em> being false
	 * @throws ISMB NPP Exception
	 *             when there is too much data for one block to send
	 */

	private byte[] sendData(byte[] data, boolean moreData)
			throws IsmbNppException {
		int dataLength = (data != null) ? data.length : 0;
		int blockSize = BLOCK_SIZE - 1;
		if (dataLength > blockSize)
			throw new IsmbNppException("too much data for one block");
		int dataOffset = 1;

		int sendBufferSize = dataOffset + dataLength;
		byte[] sendBuffer = new byte[sendBufferSize];

		if (dataLength > 0)
			System.arraycopy(data, 0, sendBuffer, dataOffset, dataLength);

		byte instr =TG_SET_DATA;
		//byte[] resultBuffer = transceive(instr, sendBuffer);
		transceive(instr, sendBuffer);
		return null;
	}

	/**
	 * Receive data from initiator
	 *
	 * @return the data
	 * @throws ISMB NPP Exception
	 */
	public byte[] receive() throws IsmbNppException {
		if (transmissionMode != RECEIVE){     	
			System.out.println("expected send");
			byte[] result = getData();
			String received_2=new String(result);
			System.out.println(received_2);
			if(result.length>0)
			{
				while (result[0] == 0x01) {
					receiveBuffer = Util.appendToByteArray(receiveBuffer, result,
							1, result.length - 1);
					sendData(null, false);
					result = getData();
				}
				receiveBuffer = Util.appendToByteArray(receiveBuffer, result, 1,
						result.length - 1);  
			}                        

		}
		if (mode == TARGET) {
			receiveBuffer = null;
			byte[] result = getData();

			if(result.length>0)
			{                        	
				while (result[0] == 0x01) {
					receiveBuffer = Util.appendToByteArray(receiveBuffer, result,
							1, result.length - 1);
					sendData(null, false);
					result = getData();
				}
				receiveBuffer = Util.appendToByteArray(receiveBuffer, result, 1,
						result.length - 1);
			}
		}
		transmissionMode = SEND;
		return receiveBuffer;
	}

	/**
	 * Requests data from an initiator
	 *
	 * @return the data
	 * @throws ISMB NPP PException
	 */
	private byte[] getData() throws IsmbNppException {
		byte[] resultBuffer = transceive(TG_GET_DATA, null);
		/* remove the status byte from the result */
		return Util.subByteArray(resultBuffer, 1, resultBuffer.length - 1);
	}

	/**
	 * Close the connection (release target)
	 *
	 * @throws ISMBNppException
	 */
	public void close() throws IsmbNppException {
		if (mode == INITIATOR) {
			transceive(IN_RELEASE, new byte[] { 0x01 });
		}
	}

	/**
	 * Sends and receives APDUs to and from the controller
	 *
	 * @param instr
	 *            Instruction
	 * @param param
	 *            Payload to send
	 *
	 * @return The response payload 
	 */
	private byte[] transceive(byte instr, byte[] payload) throws IsmbNppException {
		if (ch == null) {
			throw new IsmbNppException("channel not open");
		}
		int payloadLength = (payload != null) ? payload.length : 0;
		byte[] instruction = { (byte) 0xd4, instr };

		//ACR122 header
		byte[] header = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) (instruction.length + payloadLength) };

		/* construct the command */
		byte[] cmd = Util.appendToByteArray(header, instruction, 0,
				instruction.length);
		cmd = Util.appendToByteArray(cmd, payload);

		return _api.transceive(cmd);
	}


	public void rightProcedureTarget(){

		try{
			Thread.sleep(1000);
		}catch(Exception e){

		}
		_api.turnAntennaOn();
		
		
		try{
			Thread.sleep(1000);
		}catch(Exception e){

		}

		System.out.println("Called rightProcedureTarget..");
		try {	
			//TG_INIT_AS_TARGET
			byte[] targetPayload = { 	
					(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
					(byte) 0x00, (byte) 0x40, (byte) 0x01, (byte) 0xfe, (byte) 0x0f, 
					(byte) 0xbb, (byte) 0xba, (byte) 0xa6, (byte) 0xc9, (byte) 0x89,
					(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff,
					(byte) 0x01, (byte) 0xfe, (byte) 0x0f, (byte) 0xbb, (byte) 0xba, 
					(byte) 0xa6, (byte) 0xc9, (byte) 0x89, (byte) 0x00, (byte) 0x00,
					(byte) 0x06, (byte) 0x46, (byte) 0x66, (byte) 0x6D, (byte) 0x01,
					(byte) 0x01, (byte) 0x10, (byte) 0x00 
			};       			
			transceive(TG_INIT_AS_TARGET, targetPayload);
		} catch (IsmbNppException e) {e.printStackTrace();}

		try { 
			//GETDATA
			transceive(TG_GET_DATA, null);
		} catch (IsmbNppException e) {e.printStackTrace();}	

		try { //SETDATA
			byte[] targetPayload = { 
					(byte)0x05, (byte)0x20,
					(byte)0x06, (byte)0x0f, (byte)0x63, (byte)0x6f, (byte)0x6d,
					(byte)0x2e, (byte)0x61, (byte)0x6e, (byte)0x64, (byte)0x72,
					(byte)0x6f, (byte)0x69, (byte)0x64, (byte)0x2e, (byte)0x6e,
					(byte)0x70, (byte)0x70
			};	
			transceive(TG_SET_DATA, targetPayload);
		} catch (IsmbNppException e) {e.printStackTrace();}

		try { //GETDATA
			transceive(TG_GET_DATA, null);
		} catch (IsmbNppException e) {e.printStackTrace();}

		try { //SETDATA
			byte[] targetPayload = sendData();
			transceive(TG_SET_DATA, targetPayload);
		} catch (IsmbNppException e) {e.printStackTrace();}

		try{
			Thread.sleep(1000);
		}catch(Exception e){

		}

		_api.turnAntennaOff();
	}

	public byte [] sendData(){
		try { //SETDATA

			
			byte[] targetPayload = { 
					(byte)0x43, (byte)0x20,
					(byte)0x00, //SEQ NUMBER 
					(byte)0x01, //NPP PROTOCOL VERSION 
					(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, //NUMBER OF NDEF ENTRIES 
					(byte)0x01, //ACTION CODE
			};
			
			byte [] payload = _message.getData();
			byte [] ret = new byte[targetPayload.length + 4 + payload.length];
			
			for(int i=0;i<targetPayload.length;i++){
				ret[i] = targetPayload[i];
			}
			ret[targetPayload.length]  =  (byte) ((payload.length >> 24) & 0xff);
			ret[targetPayload.length+1] = (byte) ((payload.length >> 16) & 0xff);
			ret[targetPayload.length+2] = (byte) ((payload.length >> 8) & 0xff);
			ret[targetPayload.length+3] = (byte) ((payload.length) & 0xff);

			for(int i=0;i<payload.length;i++){
				ret[targetPayload.length+4+i] = payload[i];
			}
			
			return ret;
		}catch(Exception e){

		}
		return null;
	}


}
