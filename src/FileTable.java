/*	Jay Hennen & Chris Rouse
 * 	SuperBlock.java
 * 	CSS 430 ~ Professor Fukuda
 */

import java.util.Vector;

public class FileTable {

      private Vector<FileTableEntry> table;         				// the actual entity of this file table
      private Directory dir;   // the root directory
      private Vector<Inode> inodes;

      public FileTable( Directory directory, int numInodes) { 					// constructor
         table = new Vector<FileTableEntry>( );     				// instantiate a file (structure) table
         dir = directory;          // receive a reference to the Director
         inodes = new Vector<Inode>();
         for (short i = 0; i < numInodes; i++) {
        	 inodes.add(new Inode(i));
         }
      }                             								// from the file system

      // allocate a new file (structure) table entry for this file name
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk
      // return a reference to this file (structure) table entry
      public synchronized FileTableEntry falloc( String fname, String mode ) {
             	  
    	short iNumber = -1;											//Initialize inumber/inode to empty values
      	Inode inode = null;
      	
      	while(true){
      		iNumber = fname.equals("/") ? 0 : dir.namei(fname);		//set inumber to either 0 or the namei of filename
      		if(iNumber >= 0){										//As long as iNumber isn't less than zero, continue
      			inode = inodes.get(iNumber);							//create a new Inode using out zero or higher number
      			if(mode.compareTo("r") == 0){						//Check to see if we are flagged for read
      				if (inode.flag > 0) {							//If the flag is in use, wait, otherwise break out of loop
      					try {
							wait();
						} catch (InterruptedException e) { }
      				}
      				break;
      			} else if(mode.compareTo("w") == 0){				//Check to see if the flag is set to write
      				if (inode.flag > 0) {							//If in use, wait, otherwise set flag to used and break
      					try {										//so that other writers can't enter
      						wait();
      					} catch (InterruptedException e) { }
      				}
      				inode.flag=1;
      				break;
      			} else if (mode.compareTo("w+") == 0) {				//Check to see if the flag is set to read/write
      				if (inode.flag > 0) {							//If in use, wait, otherwise set flag to used and break
      					try {										//so that other writers can't enter
      						wait();
      					} catch (InterruptedException e) { }
      				}
      				inode.flag=1;
      				break;
      			} else if (mode.compareTo("a") == 0) {				//Check to see if the flag is set to append
      				if (inode.flag > 0) {							//If in use, wait, otherwise set flag to used and break
      					try {										//so that other writers can't enter
      						wait();
      					} catch (InterruptedException e) { }
      				}
      				inode.flag=1;
      				break;
      			} else {
      				throw new IllegalArgumentException();
      			}
      		}
      	}
      	inode.count++;												//Increment the inode count
      	inode.toDisk(iNumber);										//Use the toDisk command to copy the inode to disk
      	FileTableEntry e = new FileTableEntry(inode, iNumber, mode);//Create a new Filetableentry
      	if (mode.equals("a")) {										//If set to a, we need to set the seek pointer as well
      		e.seekPtr = inode.length;
      	}
      	table.addElement(e);										//Add this new entry to the table
      	return e;
      }

      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
      public synchronized boolean ffree( FileTableEntry e ) {
    	  boolean retVal = table.removeElement(e);
    	  if (retVal ==  true) {									//If the table contains the entered Filetableentry
    		  e.inode.flag=0;										//Remove it from the table
    		  if(e.inode.count != 0)
    			  e.inode.count--;
//    		  SysLib.cerr(" " + e.inode.count);
    		  e.inode.toDisk(e.iNumber);							//Write it back to the disk
    		  e = null;
    		  notifyAll();
    		  return true;											//Return that it sucessfully completed
    	  } else {
    		  System.err.println("freeing nonexistent FTE");
    		  return false;
    	  }
      }

      public synchronized boolean fempty( ) {
         return table.isEmpty( );  // return if table is empty 
      }                            // should be called before starting a format   
    }