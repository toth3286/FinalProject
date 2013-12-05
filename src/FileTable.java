import java.util.Vector;

public class FileTable {

      private Vector<FileTableEntry> table;         // the actual entity of this file table
      private Directory dir;        // the root directory 

      public FileTable( Directory directory ) { // constructor
         table = new Vector<FileTableEntry>( );     // instantiate a file (structure) table
         dir = directory;           // receive a reference to the Director
      }                             // from the file system

      // major public methods
      public synchronized FileTableEntry falloc( String fname, String mode ) {
         // allocate a new file (structure) table entry for this file name
         // allocate/retrieve and register the corresponding inode using dir
         // increment this inode's count
         // immediately write back this inode to the disk
         // return a reference to this file (structure) table entry
    	  
    	short iNumber = -1;
      	Inode inode = null;
      	
      	while(true){
      		iNumber = fname.equals("/") ? 0 : dir.namei(fname);
      		if(iNumber >= 0){
      			inode = new Inode(iNumber);
      			if(mode.compareTo("r") == 0){
      				if (inode.flag != 0) {
      					try {
							wait();
						} catch (InterruptedException e) { }
      					break;
      				}
      			} else if(mode.compareTo("w") == 0){
      				if (inode.flag != 0) {
      					try {
      						wait();
      					} catch (InterruptedException e) { }
      					inode.flag=1;
      					break;
      				}
      			} else if (mode.compareTo("w+") == 0) {
      				if (inode.flag != 0) {
      					try {
      						wait();
      					} catch (InterruptedException e) { }
      					inode.flag=1;
      					break;
      				}
      			} else if (mode.compareTo("a") == 0) {
      				if (inode.flag != 0) {
      					try {
      						wait();
      					} catch (InterruptedException e) { }
      					break;
      					inode.flag=1;
      				}
      			} else {
      				
      			}
      		}
      	}
      	inode.count++;
      	inode.toDisk(iNumber);
      	FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
      	if (mode.equals("a")) {
      		e.seekPtr = inode.length;
      	}
      	table.addElement(e);
      	return e;
      }

      public synchronized boolean ffree( FileTableEntry e ) {
         // receive a file table entry reference
         // save the corresponding inode to the disk
         // free this file table entry.
         // return true if this file table entry found in my table
    	  if (table.contains(e)) {
    		  e.inode.toDisk(e.iNumber);
    		  table.remove(e);
    		  return true;
    	  } else {
    		  return false;
    	  }
      }

      public synchronized boolean fempty( ) {
         return table.isEmpty( );  // return if table is empty 
      }                            // should be called before starting a format   }