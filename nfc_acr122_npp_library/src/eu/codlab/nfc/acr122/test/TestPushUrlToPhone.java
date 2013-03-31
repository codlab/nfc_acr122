/*
 * SendToPhone- Simple test of IsmbNppConnection
 * 
 * Copyright (C) 2011  Antonio Lotito <lotito@ismb.it>
 * Modified by (C) 2013 codlab - Kevin Le Perf <codlabtech@gmail.com>
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

package eu.codlab.nfc.acr122.test;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import com.sun.jndi.toolkit.url.Uri;

import eu.codlab.nfc.acr122.AppNfc;
import eu.codlab.nfc.acr122.IsmbNppConnectionSend;
import eu.codlab.nfc.acr122.IsmbNppException;
import eu.codlab.nfc.stack.NdefMessage;
import eu.codlab.nfc.stack.NdefRecord;

@SuppressWarnings("restriction")
public class TestPushUrlToPhone 
{
	CardTerminal terminal = null;	     
	IsmbNppConnectionSend n=null;
	String info="";
	String id_trans="";
	Timer timer;

	public static void main(String[] args) 
	{		
		try {
			System.out.println(AppNfc.createUri(new Uri("http://google.fr")).toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new TestPushUrlToPhone();				
	}

	public TestPushUrlToPhone()	
	{		
		TerminalFactory factory;
		List<CardTerminal> terminals;			
		//List all available terminals	 		 
		try {
			System.out.println("Get factory");
			factory = TerminalFactory.getDefault();
			System.out.println("Get terminals");
			terminals = factory.terminals().list();          
			if (terminals.size() == 0) {
				System.out.println("There are not terminals.");
				terminals = null;			 
			}
			else {
				terminal=terminals.get(0);
				System.out.println("Terminal name: "+terminal.getName());
			}	
		} 
		catch (CardException c) {
			System.out.print(c.getMessage());
			terminals = null;			
		}		 
		timer=new Timer();
		timer.schedule(new InitiatorTask(), 500);		 
	}

	public class InitiatorTask extends TimerTask 
	{			 						
		public void run() 
		{
			ThreadSender t=new ThreadSender();
			t.run();
		}
	}

	//THREAD
	public class ThreadSender implements Runnable 
	{			
		public void run() 
		{
			try 
			{                				
				if(n!=null)
					n.close();				 
				NdefMessage mess = new NdefMessage(new NdefRecord[]{AppNfc.createUri(new Uri("http://codlab.eu"))});

				n = new IsmbNppConnectionSend(terminal, mess);			
				n.setDebugMode();
				n.rightProcedureTarget();

				System.out.println("Finished");
			} 
			catch (Exception e) 
			{
				if (n != null) 
				{
					try{
						n.close();
					} catch (IsmbNppException e1) {System.out.println(e1);}	             
				}				 
				System.out.println(e.getMessage());	 		 		 
			}
			finally
			{
				try {
					if(n != null)
						n.close();
					n = null;
				} catch (IsmbNppException e) {e.printStackTrace();}				 			 
			}		 			 		
		}
	}	
}
