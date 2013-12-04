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
      				
      			} else if(mode.compareTo("w") == 0){
      				
      			} else if (mode.compareTo("w+") == 0) {
      				
      			} else if (mode.compareTo("a") == 0) {
      				
      			} else {
      				
      			}
      		}
      	}
      	inode.count++;
      	inode.toDisk(iNumber);
      	FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
      	table.addElement(e);
      	return e;
      }

      public synchronized boolean ffree( FileTableEntry e ) {
         // receive a file table entry reference
         // save the corresponding inode to the disk
         // free this file table entry.
         // return true if this file table entry found in my table
      }

      public synchronized boolean fempty( ) {
         return table.isEmpty( );  // return if table is empty 
      }                            // should be called before starting a format
   }