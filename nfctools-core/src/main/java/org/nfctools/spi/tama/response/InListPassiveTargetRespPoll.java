package org.nfctools.spi.tama.response;

import org.nfctools.utils.NfcUtils;

public class InListPassiveTargetRespPoll {

	private int numberOfTargets;
	private byte[] targetData;

	public InListPassiveTargetRespPoll(int numberOfTargets, byte[] targetData) {
		this.numberOfTargets = numberOfTargets;
		this.targetData = targetData;
	}

	public int getNumberOfTargets() {
		return numberOfTargets;
	}

	public byte[] getTargetData() {
		return targetData;
	}

	public byte getTargetId() {
		return targetData[2];
	}

	public byte getSelectResponse() {
		return targetData[5];
	}

	public boolean isIsoDepSupported() {
		return (getSelectResponse() & 0x20) == 0x20;
	}

    public boolean isMifare(){
        return (getSelectResponse() & 0x10) == 0x10;
    }

    public String getUID(){
        if(targetData.length > 7){
            //having valid data
            byte [] uid = new byte[targetData[6]];
            for(int i=0;i<uid.length && i<targetData.length;i++){
                uid[i] = targetData[7+i];
            }
            return NfcUtils.convertBinToASCII(uid);
        }
        return "";
    }
}
