/*
 * IsmbNppConnection
 * 
 * Copyright (C) 2011  Antonio Lotito <lotito@ismb.it>
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

@SuppressWarnings("restriction")
public class IsmbNppConnectionRead {
        
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
        private final static byte TG_GET_TARGET_STATUS = (byte)0x8A;
        
        //Mode of LLCP connection (always target)
        private int mode=TARGET;
        private int transmissionMode = RECEIVE;;
        
        private byte[] receiveBuffer;
        private CardTerminal terminal;
        private CardChannel ch;

        //  Enable debugMode to print info about the communication
        private boolean debugMode = false;

        /**
         * Initialize NPP Connection
         *
         * @param t
         *            a valid card terminal
         *
         * @throws IsmbNppException
         *             if the terminal is incorrect
         */
        public IsmbNppConnectionRead(CardTerminal t) throws IsmbNppException {
                if (t == null) {
                        throw new IsmbNppException("invalid card terminal");
                }
                terminal = t;
                Card card;
                try {
        			if (terminal.waitForCardPresent(30000)) {
                    	 card = terminal.connect("*");
                         ch = card.getBasicChannel();
                         System.out.println(card.getProtocol());   
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

                if (debugMode)
                        Util.debugAPDUs(cmd, null);

                try {
                        CommandAPDU c = new CommandAPDU(cmd);
                        ResponseAPDU r = ch.transmit(c);

                        byte[] ra = r.getBytes();

                        if (debugMode)
                                Util.debugAPDUs(null, ra);

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
        
             
        public void rightProcedureReceiver(){
        	System.out.println("Called rightProcedureReceiver..");        	
		    //TG_INIT_AS_TARGET
		    try {
			    byte[] targetPayload = { (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x01, (byte) 0xFE, (byte) 0x00, (byte) 0x00,
					  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xFE,
					  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					  (byte) 0x06, (byte) 0x46, (byte) 0x66, (byte) 0x6D,
					  (byte) 0x01, (byte) 0x01, (byte) 0x10, (byte) 0x00 };	                         	        	
			    byte[]array = transceive(TG_INIT_AS_TARGET, targetPayload);  				
			} catch (IsmbNppException e) {e.printStackTrace();}			
			//RECEIVE: 0xD5 0x8D 0x26 0x1E 0xD4 0x00 0x01 0xFE 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x32 0x46 0x66 0x6D 0x01 0x01 0x10 0x03 0x02 0x00 0x01 0x04 0x01 0x96 0x90 0x00 
			
			//TG_GET_DATA
			try {
		  	 	byte[]array = transceive(TG_GET_DATA, null); 	 			
		  	} catch (IsmbNppException e) {e.printStackTrace();}
		  	 //RECEIVE:0xD5 0x87 0x00 0x05 0x21 0x06 0x0F 0x63 0x6F 0x6D 0x2E 0x61 0x6E 0x64 0x72 0x6F 0x69 0x64 0x2E 0x6E 0x70 0x70 0x90 0x00 
							  	    
		  	//TG_SET_DATA  
	        try {
	        	  //0x85 0x81 -> DSAP 0x21 PTYPE 0110= connection complete SSAP=0x01
	        	  byte[] targetPayload = { (byte) 0x85, (byte) 0x81  };
				  byte[]array = transceive( TG_SET_DATA, targetPayload);					
			} catch (IsmbNppException e) {e.printStackTrace();} 	
		  	//RECEIVE:0xD5 0x8F 0x00 0x90 0x00 
				
			//TG_GET_TARGET_STATUS
			try {
		  	 	byte[]array = transceive((byte) TG_GET_TARGET_STATUS, null); 	 			
		  	} catch (IsmbNppException e) {e.printStackTrace();}
		  	//RECEIVE:	0xD5 0x8B 0x01 0x22 0x90 0x00 
		  	//TARGET ACTIVATED with speed initiator=speed target=424 kbps
			
		  	//TG_GET_DATA
			try {
		  	 	byte[]array = transceive(TG_GET_DATA, null); 	 			
		  	} catch (IsmbNppException e) {e.printStackTrace();}
		  	//RECEIVE:0xD5 0x87 0x00 0x07 0x21 0x00 0x01 0x00 0x00 0x00 0x01 0x01 0x00 0x00 0x00 0x27 0xD1 0x01 0x23 0x54 0x02 0x65 0x6E 0x4E 0x44 0x45 0x46 0x20 0x50 0x75 0x73 0x68 0x20 0x64 0x61 0x74 0x61 0x20 0x73 0x65 0x6E 0x74 0x20 0x66 0x72 0x6F 0x6D 0x20 0x4E 0x65 0x78 0x75 0x73 0x2D 0x53 0x90 0x00   
		  	//0x07 0x21 = SSAP 0x01 PTYPE=1100=Information DSAP=0x21
		  	//0x00 = Sequence number
		  	//0x01= NPP NDEF PROTOCOL VERSION
		  	//0x00 0x00 0x00 0x01= NUMBER OF NDEF ENTRIES
		  	//0x01= ACTION CODE is always 0x01
		  	//0x00 0x00 0x00 0x27= NDEF LENGTH
		  	//0xD1= Start End Short-Record  RTD
		  	//0x01=TYPE length
		  	//0x23=PAYLOAD LENGTH 
		  	//0x54=TYPE text 
		  	//PAYLOAD: 0x02 0x65 0x6E 0x4E 0x44 0x45 0x46 0x20 0x50 0x75 0x73 0x68 0x20 0x64 0x61 0x74 0x61 0x20 0x73 0x65 0x6E 0x74 0x20 0x66 0x72 0x6F 0x6D 0x20 0x4E 0x65 0x78 0x75 0x73 0x2D 0x53 0x90 0x00
		  	//0x02= UTF-8
		  	//0x65 0x6E= locale en
		  	//0x4E 0x44 0x45 0x46 0x20 0x50 0x75 0x73 0x68 0x20 0x64 0x61 0x74 0x61 0x20 0x73 0x65 0x6E 0x74 0x20 0x66 0x72 0x6F 0x6D 0x20 0x4E 0x65 0x78 0x75 0x73 0x2D 0x53
		  	//= NDEF Push data sent from Nexus-S
		  	
        }
        
   
}
