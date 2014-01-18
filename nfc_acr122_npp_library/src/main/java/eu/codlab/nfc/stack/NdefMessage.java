package eu.codlab.nfc.stack;

import java.util.ArrayList;

public class NdefMessage {
	public ArrayList<NdefRecord> _records;
	
	public NdefMessage(NdefRecord [] records){
		_records = new ArrayList<NdefRecord>();
		for(int i=0;records != null && i < records.length;i++){
			records[0].getRecord()[0] &= 0x3F;//0011 1111 > to set not begin/not end

			if(i == 0){
				records[0].getRecord()[0] |= 0x80;//1000 0000 > to add begin
			}
			if(i == records.length-1){
				records[i].getRecord()[0] |= 0x40;//0100 0000 > to set as finish
			}
			
			_records.add(records[i]);
		}
	}
	
	public NdefRecord getRecord(int i){
		if(i < _records.size()){
			return _records.get(i);
		}else{
			return null;
		}
	}
	
	public byte [] getData(){
		int index = 0;
		int length = 0;
		byte [] data = null;
		byte [] tmp = null;
		
		for(int i=0;i<_records.size();i++){
			length+=_records.get(i).getRecord().length;
		}
		
		data = new byte[length];

		for(int i=0;i<_records.size();i++){
			tmp = _records.get(i).getRecord();
			System.arraycopy(tmp, 0, data, index, tmp.length);
			length+=tmp.length;
		}
		return data;
	}

}
