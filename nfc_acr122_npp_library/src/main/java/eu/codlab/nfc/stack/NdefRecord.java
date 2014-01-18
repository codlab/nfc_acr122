package eu.codlab.nfc.stack;

public class NdefRecord {
    /**
     * Indicates no type, id, or payload is associated with this NDEF Record.
     * <p>
     * Type, id and payload fields must all be empty to be a valid TNF_EMPTY
     * record.
     */
    public static final short TNF_EMPTY = 0x00;

    /**
     * Indicates the type field uses the RTD type name format.
     * <p>
     * Use this TNF with RTD types such as RTD_TEXT, RTD_URI.
     */
    public static final short TNF_WELL_KNOWN = 0x01;

    /**
     * Indicates the type field contains a value that follows the media-type BNF
     * construct defined by RFC 2046.
     */
    public static final short TNF_MIME_MEDIA = 0x02;

    /**
     * Indicates the type field contains a value that follows the absolute-URI
     * BNF construct defined by RFC 3986.
     */
    public static final short TNF_ABSOLUTE_URI = 0x03;

    /**
     * Indicates the type field contains a value that follows the RTD external
     * name specification.
     * <p>
     * Note this TNF should not be used with RTD_TEXT or RTD_URI constants.
     * Those are well known RTD constants, not external RTD constants.
     */
    public static final short TNF_EXTERNAL_TYPE = 0x04;

    /**
     * Indicates the payload type is unknown.
     * <p>
     * This is similar to the "application/octet-stream" MIME type. The payload
     * type is not explicitly encoded within the NDEF Message.
     * <p>
     * The type field must be empty to be a valid TNF_UNKNOWN record.
     */
    public static final short TNF_UNKNOWN = 0x05;

    /**
     * Indicates the payload is an intermediate or final chunk of a chunked
     * NDEF Record.
     * <p>
     * The payload type is specified in the first chunk, and subsequent chunks
     * must use TNF_UNCHANGED with an empty type field. TNF_UNCHANGED must not
     * be used in any other situation.
     */
    public static final short TNF_UNCHANGED = 0x06;

    /**
     * Reserved TNF type.
     * <p>
     * The NFC Forum NDEF Specification v1.0 suggests for NDEF parsers to treat this
     * value like TNF_UNKNOWN.
     * @hide
     */
    public static final short TNF_RESERVED = 0x07;

    /**
     * RTD Text type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_TEXT = {0x54};  // "T"

    /**
     * RTD URI type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_URI = {0x55};   // "U"

    /**
     * RTD Smart Poster type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_SMART_POSTER = {0x53, 0x70};  // "Sp"

    /**
     * RTD Alternative Carrier type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_ALTERNATIVE_CARRIER = {0x61, 0x63};  // "ac"

    /**
     * RTD Handover Carrier type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_HANDOVER_CARRIER = {0x48, 0x63};  // "Hc"

    /**
     * RTD Handover Request type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_HANDOVER_REQUEST = {0x48, 0x72};  // "Hr"

    /**
     * RTD Handover Select type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_HANDOVER_SELECT = {0x48, 0x73}; // "Hs"

    private static final byte FLAG_MB = (byte) 0x80;
    private static final byte FLAG_ME = (byte) 0x40;
    private static final byte FLAG_CF = (byte) 0x20;
    private static final byte FLAG_SR = (byte) 0x10;
    private static final byte FLAG_IL = (byte) 0x08;
	
	
	
	
	
	
	
	private byte [] data;

	
	public NdefRecord(short tnf_field, byte [] type, byte [] id, byte [] payload){
		if(type == null) throw new NullPointerException("seriously, type null?");

		int length = 1;
		short type_length_number = 0;
		if(type.length > 0)
			type_length_number+=1;
		if(((type.length>>8) & 0xff) > 0)
			type_length_number+=1;
		if(((type.length>>16) & 0xff) > 0)
			type_length_number+=1;
		if(((type.length>>24) & 0xff) > 0)
			type_length_number+=1;
		length += type_length_number;

		if(id != null && id.length >0){
			length+=id.length+1;
		}

		if(payload != null && payload.length > 0){
			length += payload.length;
			if(payload.length > 0)
				length+=1;
			if(((payload.length>>8) & 0xff) > 0)
				length+=1;
			if(((payload.length>>16) & 0xff) > 0)
				length+=1;
			if(((payload.length>>24) & 0xff) > 0)
				length+=1;
		}

		//length processed
		short index = 2;
		length++;
		
		data = new byte[length];

		data[0] = (byte) (NdefRecord.FLAG_MB | //begin? 1
				NdefRecord.FLAG_ME | //end? 1
				//(1<<5) | //id ? 1 else 0 >> after
				(1<<4) | //reserved
				(tnf_field & 7));
		
		data[1] = (byte) (type.length & 0xff);
		
		if(payload != null && payload.length > 0){
			length += payload.length;
			if(((payload.length>>24) & 0xff) > 0){
				data[2] = (byte) ((payload.length>>24) & 0xff);
				data[3] = (byte) ((payload.length>>16) & 0xff);
				data[4] = (byte) ((payload.length>>8) & 0xff);
				data[5] = (byte) ((payload.length) & 0xff);
				index = 6;
			}else if(((payload.length>>16) & 0xff) > 0){
				data[2] = (byte) ((payload.length>>16) & 0xff);
				data[3] = (byte) ((payload.length>>8) & 0xff);
				data[4] = (byte) ((payload.length) & 0xff);
				index = 5;
			}else if(((payload.length>>8) & 0xff) > 0){
				data[2] = (byte) ((payload.length>>8) & 0xff);
				data[3] = (byte) ((payload.length) & 0xff);
				index = 4;
			}else if(payload.length > 0){
				data[2] = (byte) ((payload.length) & 0xff);
				index = 3;
			}
		}
		
		if(id != null && id.length >0){
			data[0] |= NdefRecord.FLAG_IL; //id ? 1 else 0
			data[index] = (byte)(id.length & 0xff);
			index++;
		}
		
		if(type != null){
			System.arraycopy(type, 0, data, index, type.length);
			index+=type.length;
		}
		
		if(id != null){
			System.arraycopy(id, 0, data, index, id.length);
			index+=id.length;
		}
		
		if(payload != null){
			System.arraycopy(payload, 0, data, index, payload.length);
			index+=payload.length;
		}
		
		

	}

	public byte [] getRecord(){
		return data;
	}
	
	public String toString() {
		byte [] a = data;
		if (a == null)
			return "[null]";
		if (a.length == 0)
			return "[empty]";
		String result = "";
		String onebyte = null;
		for (int i = 0; i < a.length; i++) {
			onebyte = Integer.toHexString(a[i]);
			if (onebyte.length() == 1)
				onebyte = "0" + onebyte;
			else
				onebyte = onebyte.substring(onebyte.length() - 2);
			result = result + "0x" + onebyte.toUpperCase() + " ";
		}
		return result;
	}
}
