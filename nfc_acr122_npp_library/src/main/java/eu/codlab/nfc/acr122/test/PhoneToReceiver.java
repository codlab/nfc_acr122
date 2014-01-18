/*
 * SendetToPhone- Simple test of IsmbNppConnection
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
package eu.codlab.nfc.acr122.test;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import eu.codlab.nfc.acr122.IsmbNppConnectionRead;
import eu.codlab.nfc.acr122.IsmbNppException;

@SuppressWarnings("restriction")
public class PhoneToReceiver 
{
	CardTerminal terminal = null;	     
	IsmbNppConnectionRead n=null;
	String info="";
	String id_trans="";
	Timer timer;
	
	public static void main(String[] args) 
	{		 		 	
		new PhoneToReceiver();				
	}
	
	public PhoneToReceiver()	
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
				 n = new IsmbNppConnectionRead(terminal);			
				 n.setDebugMode();
				 n.rightProcedureReceiver();
				  
				 System.out.println("Finished");
			 } 
			 catch (IsmbNppException e) 
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
					 n.close();
				     } catch (IsmbNppException e) {e.printStackTrace();}				 			 
			 }		 			 		
		}
	}	
}
