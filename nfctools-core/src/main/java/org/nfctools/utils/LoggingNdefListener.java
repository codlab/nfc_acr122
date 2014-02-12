/**
 * Copyright 2011-2012 Adrian Stabiszewski, as@nfctools.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nfctools.utils;

import java.util.Collection;

import org.nfctools.ndef.NdefListener;
import org.nfctools.ndef.NdefOperations;
import org.nfctools.ndef.NdefOperationsListener;
import org.nfctools.ndef.Record;
import org.nfctools.snep.Sneplet;
public class LoggingNdefListener implements NdefListener, NdefOperationsListener, Sneplet {


	@Override
	public void onNdefMessages(Collection<Record> records) {
		for (Record record : records) {
		}
	}

	@Override
	public void onNdefOperations(NdefOperations ndefOperations) {
        System.out.println("Tag ID: " + NfcUtils.convertBinToASCII(ndefOperations.getTagInfo().getId()));
		if (ndefOperations.isFormatted()) {
			if (ndefOperations.hasNdefMessage())
				onNdefMessages(ndefOperations.readNdefMessage());
			else
                System.out.println("Empty formatted tag. Size: " + ndefOperations.getMaxSize() + " bytes");
		}
		else
            System.out.println("Empty tag. NOT formatted. Size: " + ndefOperations.getMaxSize() + " bytes");
	}

	@Override
	public Collection<Record> doGet(Collection<Record> requestRecords) {
        System.out.println("SNEP get");
		onNdefMessages(requestRecords);
		return null;
	}

	@Override
	public void doPut(Collection<Record> requestRecords) {
        System.out.println("SNEP put");
		onNdefMessages(requestRecords);
	}
}
